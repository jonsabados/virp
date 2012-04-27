package com.jshnd.virp.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface AnnotationUtil {

	public <T extends Annotation> T getAnnotation(Class<T> annotation);

	public Method getGetMethod();

	public Method getSetMethod();
}
