package com.vcore.fake.service.base;

import java.lang.reflect.Method;

import com.vcore.fake.hook.MethodHook;
import com.vcore.utils.MethodParameterUtils;

/**
 * A reusable method hook that replaces the first package name argument in method calls with the virtual app package name. Used extensively across service proxies to intercept system service methods that accept a package name as the first parameter.
 */
public class PkgMethodProxy extends MethodHook {
	final String mName;

	public PkgMethodProxy(String name) {
		this.mName = name;
	}


	/**
	 * Returns the name of the system service method to hook.
	 * @return the method name
	 */
	@Override
	protected String getMethodName() {
		return mName;
	}


	/**
	 * Replaces the first package name argument with the virtual app package name and invokes the original method.
	 * @param who    the original object being hooked
	 * @param method the original method being intercepted
	 * @param args   the method arguments
	 * @return the result of the original method invocation
	 * @throws Throwable if the underlying method invocation fails
	 */
	@Override
	protected Object hook(Object who, Method method, Object[] args) throws Throwable {
		MethodParameterUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
