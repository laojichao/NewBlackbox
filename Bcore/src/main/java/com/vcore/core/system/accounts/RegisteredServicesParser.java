package com.vcore.core.system.accounts;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;

import com.vcore.core.system.pm.PackageManagerCompat;

/**
 * Parses account authenticator metadata XML resources from service declarations.
 * <p>
 * Used by {@link BAccountManagerService} to discover and load authenticator
 * metadata (account type, labels, icons, etc.) from APK resources.
 */
public class RegisteredServicesParser {

    /**
     * Returns an XmlResourceParser for the authenticator metadata XML resource
     * declared by the given service.
     *
     * @param context     the application context for resource resolution
     * @param serviceInfo the service info containing the metadata declaration
     * @param name        the metadata key name (e.g., "android.accounts.AccountAuthenticator")
     * @return the XmlResourceParser for the metadata, or null if not found or invalid
     */
    public XmlResourceParser getParser(Context context, ServiceInfo serviceInfo, String name) {
        Bundle meta = serviceInfo.metaData;
        if (meta != null) {
            int xmlId = meta.getInt(name);
            if (xmlId != 0) {
                try {
                    Resources resources = getResources(context, serviceInfo.applicationInfo);
                    if (resources == null) {
                        return null;
                    }
                    return resources.getXml(xmlId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Returns the Resources instance for the given application info, allowing
     * access to the declaring package's resources.
     *
     * @param context the application context
     * @param appInfo the application info of the declaring package
     * @return the Resources for the application
     */
    public Resources getResources(Context context, ApplicationInfo appInfo) {
        return PackageManagerCompat.getResources(context, appInfo);
    }
}
