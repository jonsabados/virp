package com.jshnd.virp.annotation;

import com.jshnd.virp.BasicColumnAccessor;
import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.StaticValueAccessor;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.exception.VirpAnnotationException;
import com.jshnd.virp.exception.VirpException;
import com.jshnd.virp.reflection.ReflectionMethodValueAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
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
		ret.setColumnFamily(mapperAnnotation.columnFamily());
		Set<ColumnAccessor<?, ?>> getters = new HashSet<ColumnAccessor<?, ?>>();
		if (readMethods) {
			generateMethodGetters(clazz, ret, getters);
		}
		if (readProperties) {
			generatePropertyGetters(clazz, ret, getters);
		}
		if (ret.getKeyValueManipulator() == null) {
			throw new VirpAnnotationException(clazz.getCanonicalName() +
					" missing required annotation " + KeyColumn.class.getCanonicalName());
		}
		ret.setColumnAccessors(getters);
		return ret;
	}

	private void processGetter(AnnotationUtil annotationUtil, RowMapperMetaData meta,
							   Set<ColumnAccessor<?, ?>> valueAccessors) {
		NamedColumn namedColumn = annotationUtil.getAnnotation(NamedColumn.class);
		NumberedColumn numberedColumn = annotationUtil.getAnnotation(NumberedColumn.class);
		KeyColumn keyColumn = annotationUtil.getAnnotation(KeyColumn.class);
		if (namedColumn != null || numberedColumn != null || keyColumn != null) {
			Method getter = annotationUtil.getGetMethod();
			Method setter = annotationUtil.getSetMethod();
			ReflectionMethodValueAccessor<Object> accessor =
					new ReflectionMethodValueAccessor<Object>(getter, setter);
			if (namedColumn != null) {
				valueAccessors.add(new BasicColumnAccessor<String, Object>(
						new StaticValueAccessor<String>(namedColumn.name(), String.class), accessor));
			}
			if (numberedColumn != null) {
				valueAccessors.add(new BasicColumnAccessor<Long, Object>(
						new StaticValueAccessor<Long>(Long.valueOf(numberedColumn.number()), Long.class), accessor));
			}
			if (keyColumn != null) {
				enforceSingleKeyColumn(meta);
				meta.setKeyValueManipulator(accessor);
			}
		}
	}

	private void generateMethodGetters(Class<?> clazz, RowMapperMetaData meta,
									   Set<ColumnAccessor<?, ?>> valueAccessors) {
		Method[] methods = clazz.getMethods();
		log.info("Inspecting " + methods.length + " for annotation " + NamedColumn.class.getCanonicalName());
		for (Method method : methods) {
			if(method.getName().startsWith("get") || method.getName().startsWith("is")) {
				processGetter(new MethodAnnotationUtil(method, clazz), meta, valueAccessors);
			}
		}
	}

	private void generatePropertyGetters(Class<?> clazz, RowMapperMetaData meta,
										 Set<ColumnAccessor<?, ?>> valueAccessors) {
		Field[] fields = clazz.getDeclaredFields();
		log.info("Inspecting " + fields.length + " for annotation " + NamedColumn.class.getCanonicalName());
		for (Field field : fields) {
			processGetter(new FieldAnnotationUtil(field, clazz), meta, valueAccessors);
		}
	}

	private void enforceSingleKeyColumn(RowMapperMetaData meta) {
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
