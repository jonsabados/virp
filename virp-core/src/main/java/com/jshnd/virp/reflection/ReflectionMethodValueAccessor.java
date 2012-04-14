package com.jshnd.virp.reflection;

import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.VirpException;

import java.lang.reflect.Method;

public class ReflectionMethodValueAccessor<T> implements ValueAccessor<T> {

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
