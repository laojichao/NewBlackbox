package com.vcore.fake.delegate;

import android.net.Uri;
import android.os.Build;
import android.os.IInterface;
import android.util.ArrayMap;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import black.android.app.ActivityThread;
import black.android.app.IActivityManager;
import black.android.content.ContentProviderHolderOreo;
import black.android.providers.Settings;
import com.vcore.BlackBoxCore;
import com.vcore.fake.service.context.providers.ContentProviderStub;
import com.vcore.fake.service.context.providers.SystemProviderStub;
import com.vcore.utils.compat.BuildCompat;

/**
 * Delegate class responsible for wrapping and managing ContentProvider instances
 * within the virtual environment.
 *
 * <p>This class intercepts ContentProvider interactions to ensure that providers
 * (including system providers like media, telephony, and settings) are properly
 * wrapped with virtual environment proxies. It handles version-specific differences
 * in how ContentProviderHolder works across Android API levels.</p>
 *
 * @see ContentProviderStub
 * @see SystemProviderStub
 */
public class ContentProviderDelegate {
    public static final String TAG = "ContentProviderDelegate";

    /** Set of provider names that have already been injected to avoid duplicate injection. */
    private static final Set<String> sInjected = new HashSet<>();

    /**
     * Updates a ContentProviderHolder's internal provider reference with a wrapped proxy.
     * For system providers (media, telephony, settings), a {@link SystemProviderStub} is used;
     * for other providers, a {@link ContentProviderStub} is used.
     *
     * @param holder the ContentProviderHolder object (API-level dependent type)
     * @param auth   the authority of the content provider
     */
    public static void update(Object holder, String auth) {
        IInterface iInterface;
        if (BuildCompat.isOreo()) {
            iInterface = ContentProviderHolderOreo.provider.get(holder);
        } else {
            iInterface = IActivityManager.ContentProviderHolder.provider.get(holder);
        }

        if (iInterface instanceof Proxy) {
            return;
        }

        IInterface bContentProvider;
        switch (auth) {
            case "media":
            case "telephony":
            case "settings":
                bContentProvider = new SystemProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg());
                break;
            default:
                bContentProvider = new ContentProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg());
                break;
        }

        if (BuildCompat.isOreo()) {
            ContentProviderHolderOreo.provider.set(holder, bContentProvider);
        } else {
            IActivityManager.ContentProviderHolder.provider.set(holder, bContentProvider);
        }
    }

    /**
     * Initializes all ContentProviders in the current activity thread by wrapping
     * them with virtual environment proxies. Clears cached settings providers first
     * to force fresh resolution.
     */
    public static void init() {
        clearSettingProvider();

        BlackBoxCore.getContext().getContentResolver().call(Uri.parse("content://settings"), "", null, null);
        Object activityThread = BlackBoxCore.mainThread();
        ArrayMap<Object, Object> map = (ArrayMap<Object, Object>) ActivityThread.mProviderMap.get(activityThread);

        for (Object value : map.values()) {
            String[] mNames = ActivityThread.ProviderClientRecordP.mNames.get(value);
            if (mNames == null || mNames.length <= 0) {
                continue;
            }

            String providerName = mNames[0];
            if (!sInjected.contains(providerName)) {
                sInjected.add(providerName);
                IInterface iInterface = ActivityThread.ProviderClientRecordP.mProvider.get(value);
                ActivityThread.ProviderClientRecordP.mProvider.set(value, new ContentProviderStub().wrapper(iInterface, BlackBoxCore.getHostPkg()));
                ActivityThread.ProviderClientRecordP.mNames.set(value, new String[]{providerName});
            }
        }
    }

    /**
     * Clears cached ContentProvider references in the Settings system, secure, and global
     * name-value caches to force fresh provider resolution.
     */
    public static void clearSettingProvider() {
        Object cache;
        cache = Settings.System.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }

        cache = Settings.Secure.sNameValueCache.get();
        if (cache != null) {
            clearContentProvider(cache);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            cache = Settings.Global.sNameValueCache.get();
            if (cache != null) {
                clearContentProvider(cache);
            }
        }
    }

    /**
     * Clears the cached ContentProvider reference within a Settings NameValueCache object.
     * Handles API-level differences between pre-Oreo and Oreo+ implementations.
     *
     * @param cache the NameValueCache object to clear
     */
    private static void clearContentProvider(Object cache) {
        if (BuildCompat.isOreo()) {
            Object holder = Settings.NameValueCacheOreo.mProviderHolder.get(cache);
            if (holder != null) {
                Settings.ContentProviderHolder.mContentProvider.set(holder, null);
            }
        } else {
            Settings.NameValueCache.mContentProvider.set(cache, null);
        }
    }
}
