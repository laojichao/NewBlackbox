package com.vcore.jnihook;

/**
 * Provides JNI-level native method offset tracking for the virtual framework's
 * native hook mechanism.
 * <p>
 * Contains native methods and offset constants used to locate and patch native
 * function pointers at runtime. The offset values are populated by native code
 * during initialization.
 * </p>
 */
public final class JniHook {
    /** Native offset value used for the first native function pointer patch point. */
    public static final int NATIVE_OFFSET = 0;

    /** Native offset value used for the second native function pointer patch point. */
    public static final int NATIVE_OFFSET_2 = 0;

    /**
     * Native method that registers or resolves the first native offset.
     * Called from native code to set up the hook infrastructure.
     */
    public static native void nativeOffset();

    /**
     * Native method that registers or resolves the second native offset.
     * Called from native code to set up an additional hook point.
     */
    public static native void nativeOffset2();
}
