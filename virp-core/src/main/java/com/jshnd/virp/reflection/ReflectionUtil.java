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

		return clazz.getMethod(setterName, getter.getReturnType());
	}

}
