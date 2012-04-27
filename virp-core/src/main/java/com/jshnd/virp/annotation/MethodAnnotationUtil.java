package com.jshnd.virp.annotation;

import com.jshnd.virp.exception.VirpAnnotationException;
import com.jshnd.virp.reflection.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MethodAnnotationUtil implements AnnotationUtil {

	private Method method;

	private Class<?> classFor;

	public MethodAnnotationUtil(Method method, Class<?> classFor) {
		this.method = method;
		this.classFor = classFor;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		return method.getAnnotation(annotation);
	}

	@Override
	public Method getGetMethod() {
		return method;
	}

	@Override
	public Method getSetMethod() {
		try {
			return ReflectionUtil.getSetter(classFor, method);
		} catch (Exception e) {
			throw new VirpAnnotationException("setter for getter " + method.getName() + " not found on class "
					+ method.getClass().getCanonicalName());
		}
	}
}
