package com.vcore.utils;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;

import java.util.Objects;

import com.vcore.app.BActivityThread;

/**
 * Utility class for working with Android {@link ComponentName}, {@link Intent}, and
 * {@link ComponentInfo} objects. Provides methods to determine intent identity, resolve
 * task affinity for virtual activities, extract process names, and check whether an intent
 * targets the currently virtualized application.
 */
public class ComponentUtils {
    /**
     * Checks whether the given intent represents a request to install an APK package.
     *
     * @param intent the intent to check
     * @return {@code true} if the intent's MIME type is
     *         {@code "application/vnd.android.package-archive"}
     */
    public static boolean isRequestInstall(Intent intent) {
        return "application/vnd.android.package-archive".equals(intent.getType());
    }

    /**
     * Checks whether the given intent targets a component belonging to the currently
     * virtualized application (the app running inside the virtual environment).
     *
     * @param intent the intent to check
     * @return {@code true} if the intent's component package matches the virtual app's package name
     */
    public static boolean isSelf(Intent intent) {
        ComponentName component = intent.getComponent();
        if (component == null || BActivityThread.getAppPackageName() == null) {
            return false;
        }
        return component.getPackageName().equals(BActivityThread.getAppPackageName());
    }

    /**
     * Checks whether all intents in the given array target the currently virtualized application.
     *
     * @param intent the array of intents to check
     * @return {@code true} if every intent in the array targets the virtual app,
     *         {@code false} if any intent does not
     */
    public static boolean isSelf(Intent[] intent) {
        for (Intent intent1 : intent) {
            if (!isSelf(intent1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resolves the effective task affinity for an activity. Single-instance activities receive
     * a unique synthetic affinity to prevent task sharing. Activities with no explicit affinity
     * fall back to the application-level affinity or the package name.
     *
     * @param info the activity info to resolve task affinity for
     * @return the resolved task affinity string
     */
    public static String getTaskAffinity(ActivityInfo info) {
        if (info.launchMode == LAUNCH_SINGLE_INSTANCE) {
            return "-SingleInstance-" + info.packageName + "/" + info.name;
        } else if (info.taskAffinity == null && info.applicationInfo.taskAffinity == null) {
            return info.packageName;
        } else if (info.taskAffinity != null) {
            return info.taskAffinity;
        }
        return info.applicationInfo.taskAffinity;
    }

    /**
     * Compares two intents for filter equality, matching on action, data, type, package,
     * component, and categories. This replicates the logic of
     * {@link Intent#filterEquals(Intent)} but also resolves the package name from the
     * component when the explicit package is not set.
     *
     * @param a the first intent to compare; may be {@code null}
     * @param b the second intent to compare; may be {@code null}
     * @return {@code true} if both intents have equivalent filter fields, or if both are
     *         {@code null}; returns {@code true} when only one is {@code null} (matching
     *         Android framework behavior where null intent matches any)
     */
    public static boolean intentFilterEquals(Intent a, Intent b) {
        if (a != null && b != null) {
            if (!Objects.equals(a.getAction(), b.getAction())) {
                return false;
            }

            if (!Objects.equals(a.getData(), b.getData())) {
                return false;
            }

            if (!Objects.equals(a.getType(), b.getType())) {
                return false;
            }

            Object pkgA = a.getPackage();
            if (pkgA == null && a.getComponent() != null) {
                pkgA = a.getComponent().getPackageName();
            }

            String pkgB = b.getPackage();
            if (pkgB == null && b.getComponent() != null) {
                pkgB = b.getComponent().getPackageName();
            }

            if (!Objects.equals(pkgA, pkgB)) {
                return false;
            }

            if (!Objects.equals(a.getComponent(), b.getComponent())) {
                return false;
            }
            return Objects.equals(a.getCategories(), b.getCategories());
        }
        return true;
    }

    /**
     * Retrieves the process name for the given component. If no explicit process name is set,
     * falls back to the component's package name and caches the result on the component info.
     *
     * @param componentInfo the component info to extract the process name from
     * @return the resolved process name
     */
    public static String getProcessName(ComponentInfo componentInfo) {
        String processName = componentInfo.processName;
        if (processName == null) {
            processName = componentInfo.packageName;
            componentInfo.processName = processName;
        }
        return processName;
    }

    /**
     * Creates a {@link ComponentName} from the given component info's package and class names.
     *
     * @param componentInfo the component info to convert
     * @return a new {@link ComponentName} representing this component
     */
    public static ComponentName toComponentName(ComponentInfo componentInfo) {
        return new ComponentName(componentInfo.packageName, componentInfo.name);
    }
}
