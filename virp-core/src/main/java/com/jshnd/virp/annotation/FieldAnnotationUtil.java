package com.jshnd.virp.annotation;

import com.jshnd.virp.exception.VirpAnnotationException;
import com.jshnd.virp.reflection.ReflectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class FieldAnnotationUtil implements AnnotationUtil {

	private Field field;

	private Class<?> classFor;

	FieldAnnotationUtil(Field field, Class<?> classFor) {
		this.field = field;
		this.classFor = classFor;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		return field.getAnnotation(annotation);
	}

	@Override
	public Annotation[] getAnnotations() {
		return field.getAnnotations();
	}

	@Override
	public Method getGetMethod() {
		String methodName = "get" + StringUtils.capitalize(field.getName());
		try {
			return classFor.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			throw new VirpAnnotationException("Getter for field " + field.getName() + " not found");
		}
	}

	@Override
	public Method getSetMethod() {
		try {
			return ReflectionUtil.getSetter(classFor, getGetMethod());
		} catch(NoSuchMethodException e) {
			throw new VirpAnnotationException("Setter for field " + field.getName() + " not found");
		}
	}
}
