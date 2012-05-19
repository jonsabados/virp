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

import com.jshnd.virp.*;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.exception.VirpAnnotationException;
import com.jshnd.virp.exception.VirpException;
import com.jshnd.virp.reflection.ReflectionMethodValueManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnnotationDrivenRowMapperMetaDataReader implements RowMapperMetaDataReader {

	private static final Logger log = LoggerFactory.getLogger(AnnotationDrivenRowMapperMetaDataReader.class);

	private boolean readProperties = true;

	private boolean readMethods = true;

	@Override
	public <T> RowMapperMetaData<T> readClass(Class<T> clazz) {
		RowMapperMetaData<T> ret = new RowMapperMetaData<T>(clazz);
		RowMapper mapperAnnotation = clazz.getAnnotation(RowMapper.class);
		if (mapperAnnotation == null) {
			throw new VirpAnnotationException(clazz.getCanonicalName() +
					" missing required annotation " + RowMapper.class.getCanonicalName());
		}
		TimeToLive defaultTimeToLive = mapperAnnotation.defaultTimeToLive();
		ret.setColumnFamily(mapperAnnotation.columnFamily());
		Set<ColumnAccessor<?, ?>> getters = new HashSet<ColumnAccessor<?, ?>>();
		Map<String, ValueAccessor<Integer>> dynamicTimeToLives = new HashMap<String, ValueAccessor<Integer>>();
		if (readMethods) {
			processMethodDynamicTtls(clazz, dynamicTimeToLives);
		}
		if (readProperties) {
			processPropertyDynamicTtls(clazz, dynamicTimeToLives);
		}
		if (readMethods) {
			generateMethodGetters(clazz, ret, getters, defaultTimeToLive, dynamicTimeToLives);
		}
		if (readProperties) {
			generatePropertyGetters(clazz, ret, getters, defaultTimeToLive, dynamicTimeToLives);
		}
		if (ret.getKeyValueManipulator() == null) {
			throw new VirpAnnotationException(clazz.getCanonicalName() +
					" missing required annotation " + Key.class.getCanonicalName());
		}
		ret.setColumnAccessors(getters);
		return ret;
	}

	private void processGetter(AnnotationUtil annotationUtil, RowMapperMetaData<?> meta,
							   Set<ColumnAccessor<?, ?>> valueAccessors, TimeToLive defaultTimeToLive,
							   Map<String, ValueAccessor<Integer>> dynamicTimeToLives) {
		Key key = annotationUtil.getAnnotation(Key.class);
		NamedColumn namedColumn = annotationUtil.getAnnotation(NamedColumn.class);
		TimeToLive ttl = annotationUtil.getAnnotation(TimeToLive.class);
		HasDynamicTimeToLive dynamicTtlMarker = annotationUtil.getAnnotation(HasDynamicTimeToLive.class);
		Set<Annotation> numberedColumns = getNumberColumns(annotationUtil);
		if (key != null || namedColumn != null || numberedColumns.size() > 0) {
			Method getter = annotationUtil.getGetMethod();
			Method setter = annotationUtil.getSetMethod();
			ReflectionMethodValueManipulator<Object> manipulator =
					new ReflectionMethodValueManipulator<Object>(getter, setter);
			if (key != null) {
				enforceSingleKeyColumn(meta);
				meta.setKeyValueManipulator(manipulator);
			}
			ValueAccessor<Integer> ttlGetter = getTtl(defaultTimeToLive, ttl, dynamicTtlMarker, dynamicTimeToLives);
			if (namedColumn != null) {
				valueAccessors.add(new BasicColumnAccessor<String, Object>(
						new StaticValueAccessor<String>(namedColumn.name(), String.class), manipulator, ttlGetter));
			}
			for(Annotation numberedColumn : numberedColumns) {
				addNumberedColumn(numberedColumn, valueAccessors, manipulator, ttlGetter);
			}
		}
	}

	private ValueAccessor<Integer> getTtl(TimeToLive defaultTimeToLive,
										  TimeToLive staticTimeToLive,
										  HasDynamicTimeToLive dynamicTimeToLiveMarker,
										  Map<String, ValueAccessor<Integer>> dynamicTimeToLives) {
		if(dynamicTimeToLiveMarker != null && staticTimeToLive != null) {
			throw new VirpAnnotationException("Fields may only have static or dynamic ttl's - not both");
		}
		ValueAccessor<Integer> ret;
		if(dynamicTimeToLiveMarker != null) {
			final ValueAccessor<Integer> accessor = dynamicTimeToLives.get(dynamicTimeToLiveMarker.identifier());
			if(accessor == null) {
				throw new VirpAnnotationException("Dynamic ttl for marked property: "
						+ dynamicTimeToLiveMarker.identifier() + " not found");
			}
			ret = new BaseValueAccessor<Integer>() {
				@Override
				public Integer getValue(Object sourceObject) {
					return accessor.getValue(sourceObject);
				}

				@Override
				public Class<Integer> getValueType() {
					return Integer.class;
				}
			};
		} else if (staticTimeToLive != null) {
			ret = new HardCodedValueAccessor<Integer>(Integer.valueOf(staticTimeToLive.seconds()));
		} else {
		   ret = new HardCodedValueAccessor<Integer>(Integer.valueOf(defaultTimeToLive.seconds()));
		}
		return ret;
	}

	private Set<Annotation> getNumberColumns(AnnotationUtil annotationUtil) {
		Set<Annotation> ret = new HashSet<Annotation>();
		for(Annotation annotation : annotationUtil.getAnnotations()) {
			if(annotation.annotationType().getAnnotation(NumberedColumnMarker.class) != null) {
				ret.add(annotation);
			}
		}
		return ret;
	}

	private void addNumberedColumn(Annotation annotation, Set<ColumnAccessor<?, ?>> valueAccessors,
								   ReflectionMethodValueManipulator<Object> manipulator,
								   ValueAccessor<Integer> ttlGetter) {
		try {
			NumberedColumnMarker type = annotation.annotationType().getAnnotation(NumberedColumnMarker.class);
			Method numberMethod = annotation.annotationType().getMethod("number");
			StaticValueAccessor<? extends Number> identifierAccessor =
					new StaticValueAccessor(numberMethod.invoke(annotation), type.type());
			valueAccessors.add(new BasicColumnAccessor(identifierAccessor, manipulator, ttlGetter));
		} catch (NoSuchMethodException e) {
			throw new VirpAnnotationException("@NumberedColumns must have a number method!");
		} catch (InvocationTargetException e) {
			throw new VirpAnnotationException(e);
		} catch (IllegalAccessException e) {
			throw new VirpAnnotationException(e);
		}
	}

	private void generateMethodGetters(Class<?> clazz, RowMapperMetaData<?> meta,
									   Set<ColumnAccessor<?, ?>> valueAccessors,
									   TimeToLive defaultTimeToLive,
									   Map<String, ValueAccessor<Integer>> dynamicTtls) {
		Method[] methods = clazz.getMethods();
		log.info("Inspecting " + methods.length + " for annotation " + NamedColumn.class.getCanonicalName());
		for (Method method : methods) {
			if(method.getName().startsWith("get") || method.getName().startsWith("is")) {
				processGetter(new MethodAnnotationUtil(method, clazz), meta, valueAccessors,
						defaultTimeToLive, dynamicTtls);
			}
		}
	}

	private void generatePropertyGetters(Class<?> clazz, RowMapperMetaData<?> meta,
										 Set<ColumnAccessor<?, ?>> valueAccessors,
										 TimeToLive defaultTimeToLive,
										 Map<String, ValueAccessor<Integer>> dynamicTtls) {
		Field[] fields = clazz.getDeclaredFields();
		log.info("Inspecting " + fields.length + " for annotation " + NamedColumn.class.getCanonicalName());
		for (Field field : fields) {
			processGetter(new FieldAnnotationUtil(field, clazz), meta, valueAccessors, defaultTimeToLive, dynamicTtls);
		}
	}

	private void processDynamicTtl(AnnotationUtil util, Map<String, ValueAccessor<Integer>> dynamicTtls) {
		DynamicTimeToLive annotation = util.getAnnotation(DynamicTimeToLive.class);
		if(annotation != null) {
			if(dynamicTtls.containsKey(annotation.forIdentifier())) {
				throw new VirpAnnotationException("Columns may only have one source for ttl's");
			}
			Class<?> returnType = util.getType();
			if(!returnType.equals(Integer.class) && !returnType.equals(int.class)) {
				throw new VirpAnnotationException("DynamicTimeToLive members must be of Integer type");
			}
			dynamicTtls.put(annotation.forIdentifier(), new ReflectionMethodValueManipulator<Integer>(util.getGetMethod(),
					util.getSetMethod()));
		}
	}

	private void processMethodDynamicTtls(Class<?> clazz, Map<String, ValueAccessor<Integer>> dynamicTtls) {
		Method[] methods = clazz.getMethods();
		log.info("Inspecting " + methods.length + " for annotation " + DynamicTimeToLive.class.getCanonicalName());
		for (Method method : methods) {
			if(method.getName().startsWith("get") || method.getName().startsWith("is")) {
				processDynamicTtl(new MethodAnnotationUtil(method, clazz), dynamicTtls);
			}
		}
	}

	private void processPropertyDynamicTtls(Class<?> clazz, Map<String, ValueAccessor<Integer>> dynamicTtls) {
		Field[] fields = clazz.getDeclaredFields();
		log.info("Inspecting " + fields.length + " for annotation " + NamedColumn.class.getCanonicalName());
		for (Field field : fields) {
			processDynamicTtl(new FieldAnnotationUtil(field, clazz), dynamicTtls);
		}
	}


	private void enforceSingleKeyColumn(RowMapperMetaData<?> meta) {
		if (meta.getKeyValueManipulator() != null) {
			throw new VirpException("Classes may only have a single key column");
		}
	}

	public boolean isReadProperties() {
		return readProperties;
	}

	public void setReadProperties(boolean readProperties) {
		this.readProperties = readProperties;
	}

	public boolean isReadMethods() {
		return readMethods;
	}

	public void setReadMethods(boolean readMethods) {
		this.readMethods = readMethods;
	}

}
