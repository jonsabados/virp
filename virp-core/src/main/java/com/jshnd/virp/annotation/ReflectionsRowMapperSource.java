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

import com.jshnd.virp.annotation.RowMapper;
import com.jshnd.virp.config.RowMapperSource;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.Collection;

public class ReflectionsRowMapperSource implements RowMapperSource {

	private String basePackage;

	public Collection<Class<?>> getRowMapperClasses() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setUrls(ClasspathHelper.forJavaClassPath());
		builder.filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(basePackage + ".")));
		Reflections reflections = new Reflections(builder);
		return reflections.getTypesAnnotatedWith(RowMapper.class);
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

}
