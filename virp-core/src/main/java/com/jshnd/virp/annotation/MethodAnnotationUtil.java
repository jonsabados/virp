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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

class MethodAnnotationUtil implements AnnotationUtil {

	private Method method;

	private Class<?> classFor;

	MethodAnnotationUtil(Method method, Class<?> classFor) {
		this.method = method;
		this.classFor = classFor;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		return method.getAnnotation(annotation);
	}

	@Override
	public Annotation[] getAnnotations() {
		return method.getAnnotations();
	}

	@Override
	public Method getGetMethod() {
		return method;
	}

	@Override
	public Method getSetMethod() {
		try {
			return ReflectionUtil.getSetter(classFor, method);
		} catch (NoSuchMethodException e) {
			throw new VirpAnnotationException("setter for getter " + method.getName() + " not found on class "
					+ classFor.getCanonicalName());
		}
	}

	@Override
	public Class<?> getType() {
		return method.getReturnType();
	}
}
