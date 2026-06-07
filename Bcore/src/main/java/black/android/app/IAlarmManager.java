package black.android.app;

import android.os.IBinder;
import android.os.IInterface;

import black.Reflector;

/**
 * Reflection wrapper for the hidden {@code android.app.IAlarmManager$Stub} class.
 * Provides access to the alarm manager system service via its AIDL Stub.
 */
public class IAlarmManager {
    /**
     * Reflection wrapper for {@code android.app.IAlarmManager$Stub}.
     */
    public static class Stub {
        public static final Reflector REF = Reflector.on("android.app.IAlarmManager$Stub");

        /**
         * Converts an {@link IBinder} into an {@link IInterface} proxy for the alarm manager service.
         */
        public static Reflector.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
