package com.jshnd.virp.reflection;

import com.jshnd.virp.exception.VirpAnnotationException;

import java.lang.reflect.Method;

public final class ReflectionUtil {

	private ReflectionUtil() {

	}

	public static Method getSetter(Class<?> clazz, Method getter) throws NoSuchMethodException {
		String name = getter.getName();
		if(!name.startsWith("get") && !name.startsWith("is")) {
			throw new VirpAnnotationException(name + " for " + clazz.getCanonicalName() + " is not a getter");
		}
		String setterName;
		if(name.startsWith("get")) {
			setterName = name.replaceFirst("get", "set");
		} else {
			setterName = name.replaceFirst("is", "set");
		}

		return clazz.getDeclaredMethod(setterName, getter.getReturnType());
	}

}
