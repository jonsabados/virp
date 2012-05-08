package com.jshnd.virp.reflection;

import com.jshnd.virp.BaseValueManipulator;
import com.jshnd.virp.exception.VirpException;

import java.lang.reflect.Method;

public class ReflectionMethodValueManipulator<T> extends BaseValueManipulator<T> {

	private Method getterMethod;

	private Method setterMethod;

	public ReflectionMethodValueManipulator(Method getterMethod, Method setterMethod) {
		this.getterMethod = getterMethod;
		this.setterMethod = setterMethod;
	}

	@Override
	public T getValue(Object sourceObject) {
		try {
			return (T) getterMethod.invoke(sourceObject);
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	@Override
	public void setValue(Object sourceObject, T value) {
		try {
			setterMethod.invoke(sourceObject, value);
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	@Override
	public Class<T> getValueType() {
		return (Class<T>) getterMethod.getReturnType();
	}

	@Override
	public Method getSetter() {
		return setterMethod;
	}

}
