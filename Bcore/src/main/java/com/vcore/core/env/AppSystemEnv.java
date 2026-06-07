package com.vcore.core.env;

import android.content.ComponentName;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import com.vcore.BlackBoxCore;

/**
 * Maintains lists of system, root, Xposed, and pre-install package names used for environment
 * detection and control within the BlackBox virtual environment.
 * <p>
 * This class serves two primary purposes:
 * <ul>
 *   <li><b>Open packages:</b> System-level packages (webview, camera, input methods, OEM-specific apps)
 *       that are allowed to execute directly on the host without being virtualized.</li>
 *   <li><b>Black packages:</b> Root management and Xposed installer packages that should be hidden
 *       from virtual apps when root/Xposed hiding is enabled.</li>
 * </ul>
 */
public class AppSystemEnv {
    /** List of system package names that should bypass virtualization (execute on the host). */
    private static final List<String> sSystemPackages = new ArrayList<>();
    /** List of known root management (SuperSU/Superuser) package names. */
    private static final List<String> sSuPackages = new ArrayList<>();
    /** List of known Xposed framework package names. */
    private static final List<String> sXposedPackages = new ArrayList<>();
    /** List of package names that should be pre-installed in the virtual environment. */
    private static final List<String> sPreInstallPackages = new ArrayList<>();

    static {
        sSystemPackages.add("android");
        sSystemPackages.add("com.google.android.webview");
        sSystemPackages.add("com.google.android.webview.dev");
        sSystemPackages.add("com.google.android.webview.beta");
        sSystemPackages.add("com.google.android.webview.canary");
        sSystemPackages.add("com.android.webview");
        sSystemPackages.add("com.android.camera");
        sSystemPackages.add("com.android.talkback");
        sSystemPackages.add("com.miui.gallery");

        // Google Gboard
        sSystemPackages.add("com.google.android.inputmethod.latin");
        // sSystemPackages.add(BlackBoxCore.getHostPkg());

        // 华为
        sSystemPackages.add("com.huawei.webview");

        // MIUI
        sSystemPackages.add("com.miui.contentcatcher");
        sSystemPackages.add("com.miui.catcherpatch");

        // Oppo
        sSystemPackages.add("com.coloros.safecenter");

        // Su
        sSuPackages.add("com.noshufou.android.su");
        sSuPackages.add("com.noshufou.android.su.elite");
        sSuPackages.add("eu.chainfire.supersu");
        sSuPackages.add("com.koushikdutta.superuser");
        sSuPackages.add("com.thirdparty.superuser");
        sSuPackages.add("com.yellowes.su");

        sXposedPackages.add("de.robv.android.xposed.installer");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < 29){
            //解决Android 9三星浏览器闪退问题
        }else{

        }
    }

    /**
     * Checks whether the given package name is a system package that should bypass virtualization.
     *
     * @param packageName the package name to check
     * @return {@code true} if the package is an open (system) package, {@code false} otherwise
     */
    public static boolean isOpenPackage(String packageName) {
        return sSystemPackages.contains(packageName);
    }

    /**
     * Checks whether the given component belongs to a system package that should bypass virtualization.
     *
     * @param componentName the component name to check
     * @return {@code true} if the component's package is an open package, {@code false} otherwise
     */
    public static boolean isOpenPackage(ComponentName componentName) {
        return componentName != null && isOpenPackage(componentName.getPackageName());
    }

    /**
     * Checks whether the given package should be hidden (blocked) from virtual applications.
     * <p>
     * A package is "black" if it is a known root management package and root hiding is enabled,
     * or if it is a known Xposed package and Xposed hiding is enabled.
     *
     * @param packageName the package name to check
     * @return {@code true} if the package should be hidden, {@code false} otherwise
     */
    public static boolean isBlackPackage(String packageName) {
        if (BlackBoxCore.get().isHideRoot() && sSuPackages.contains(packageName)) {
            return true;
        }
        return BlackBoxCore.get().isHideXposed() && sXposedPackages.contains(packageName);
    }

    /**
     * Returns the list of package names that should be pre-installed in the virtual environment.
     *
     * @return the pre-install package name list
     */
    public static List<String> getPreInstallPackages() {
        return sPreInstallPackages;
    }
}
