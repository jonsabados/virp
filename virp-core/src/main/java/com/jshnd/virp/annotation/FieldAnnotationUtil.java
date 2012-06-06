/*
 * Copyright 2012 Jonathan Sabados
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	private Method getter;

	private Method setter;

	FieldAnnotationUtil(Field field, Class<?> classFor) {
		this.field = field;
		this.classFor = classFor;
		String methodName = "get" + StringUtils.capitalize(field.getName());
		try {
			getter = classFor.getMethod(methodName);
		} catch(NoSuchMethodException e) {
			methodName = "is" + StringUtils.capitalize(field.getName());
			try {
				getter = classFor.getMethod(methodName);
			} catch (NoSuchMethodException e2) {
				throw new VirpAnnotationException("Getter for field " + field.getName() + " not found");
			}
		}
		try {
			setter = ReflectionUtil.getSetter(classFor, getGetMethod());
		} catch (NoSuchMethodException e) {
			throw new VirpAnnotationException("Setter for field " + field.getName() + " not found");
		}
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
		return  getter;
	}

	@Override
	public Method getSetMethod() {
		return  setter;
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}
}
