package com.jshnd.virp.reflection;

import com.jshnd.virp.BaseValueAccessor;
import com.jshnd.virp.exception.VirpException;

import java.lang.reflect.Method;

public class ReflectionMethodValueAccessor<T> extends BaseValueAccessor<T> {

	private Method getterMethod;

	public ReflectionMethodValueAccessor(Method getterMethod) {
		this.getterMethod = getterMethod;
	}

	public T getValue(Object sourceObject) {
		try {
			return (T) getterMethod.invoke(sourceObject);
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	@Override
	public Class<T> getValueType() {
		return (Class<T>) getterMethod.getReturnType();
	}
}
