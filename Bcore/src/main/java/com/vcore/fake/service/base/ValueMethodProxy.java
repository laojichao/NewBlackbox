package com.vcore.fake.service.base;

import java.lang.reflect.Method;

import com.vcore.fake.hook.MethodHook;

/**
 * A reusable method hook that intercepts a named method and returns a fixed stub value without calling the original method. Used to no-op or stub out system service methods that should not execute in the virtual environment.
 */
public class ValueMethodProxy extends MethodHook {
	final Object mValue;
	final String mName;

	public ValueMethodProxy(String name, Object value) {
		this.mValue = value;
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
	 * Returns the configured stub value without invoking the original method.
	 * @param who    the original object being hooked
	 * @param method the original method being intercepted
	 * @param args   the method arguments (ignored)
	 * @return the configured stub value
	 */
	@Override
	protected Object hook(Object who, Method method, Object[] args) throws Throwable {
		return mValue;
	}
}
