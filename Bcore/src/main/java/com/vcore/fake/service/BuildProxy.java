package com.vcore.fake.service;

import black.android.os.Build;
import com.vcore.fake.hook.ClassInvocationStub;

/**
 * Proxy for android.os.Build that spoofs device build information (manufacturer, model, brand, etc.) to present a Xiaomi Mi 10 device identity within the virtual environment.
 */
public class BuildProxy extends ClassInvocationStub {


    /**
     * Returns the target Build class reference to be proxied.
     * @return the Build class reflection object
     */
    @Override
    protected Object getWho() {
        return Build.REF;
    }


    /**
     * Injects spoofed build properties (model, brand, manufacturer, etc.) into the Build class fields.
     * @param baseInvocation    the original invocation object
     * @param proxyInvocation   the proxy invocation object
     */
    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        Build.BOARD.set("umi");
        Build.BRAND.set("Xiaomi");
        Build.DEVICE.set("umi");
        Build.DISPLAY.set("QKQ1.191117.002 test-keys");
        Build.HOST.set("c5-miui-ota-bd074.bj");
        Build.ID.set("QKQ1.191117.002");
        Build.MANUFACTURER.set("Xiaomi");
        Build.MODEL.set("Mi 10");
        Build.PRODUCT.set("umi");
        Build.TAGS.set("release-keys");
        Build.TYPE.set("user");
        Build.USER.set("builder");
    }


    /**
     * Checks if the hook environment is compromised.
     * @return always returns false for BuildProxy
     */
    @Override
    public boolean isBadEnv() {
        return false;
    }
}
