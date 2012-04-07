package com.jshnd.virp.annotation;

import com.jshnd.virp.*;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.reflection.ReflectionFieldValueAccessor;
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
	public RowMapperMetaData readClass(Class<?> clazz) {
		RowMapperMetaData ret = new RowMapperMetaData(clazz);
		RowMapper mapperAnnotation = clazz.getAnnotation(RowMapper.class);
		if (null == mapperAnnotation) {
			throw new VirpAnnotationException(clazz.getCanonicalName() +
					" missing required annotation: " + RowMapper.class.getCanonicalName());
		}
		ret.setColumnFamily(mapperAnnotation.columnFamily());
		Set<ColumnAccessor> getters = new HashSet<ColumnAccessor>();
		if (readMethods) {
			generateMethodGetters(clazz, ret, getters);
		}
		if (readProperties) {
			generatePropertyGetters(clazz, ret, getters);
		}
		ret.setColumnAccessors(getters);
		return ret;
	}

	private void generateMethodGetters(Class<?> clazz, RowMapperMetaData meta, Set<ColumnAccessor> valueAccessors) {
		Method[] methods = clazz.getDeclaredMethods();
		log.info("Inspecting " + methods.length + " for annotation " + Column.class.getCanonicalName());
		for (Method method : methods) {
			Column column = method.getAnnotation(Column.class);
			if (column != null) {
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				ReflectionMethodValueAccessor accessor = new ReflectionMethodValueAccessor();
				accessor.setValueType(TypeUtils.getType(method.getReturnType()));
				accessor.setGetterMethod(method);
				valueAccessors.add(new BasicColumnAccessor(column.name(), accessor));
			}
			if (method.getAnnotation(KeyColumn.class) != null) {
				enforceSingleKeyColumn(meta);
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				ReflectionMethodValueAccessor accessor = new ReflectionMethodValueAccessor();
				accessor.setGetterMethod(method);
				accessor.setValueType(TypeUtils.getType(method.getReturnType()));
				meta.setKeyValueAccessor(accessor);
			}
		}
	}

	private void generatePropertyGetters(Class<?> clazz, RowMapperMetaData meta, Set<ColumnAccessor> valueAccessors) {
		Field[] fields = clazz.getDeclaredFields();
		log.info("Inspecting " + fields.length + " for annotation " + Column.class.getCanonicalName());
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (column != null) {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				ReflectionFieldValueAccessor accessor = new ReflectionFieldValueAccessor();
				accessor.setValueType(TypeUtils.getType(field.getType()));
				accessor.setField(field);
				valueAccessors.add(new BasicColumnAccessor(column.name(), accessor));
			}
			if (field.getAnnotation(KeyColumn.class) != null) {
				enforceSingleKeyColumn(meta);
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				ReflectionFieldValueAccessor getter = new ReflectionFieldValueAccessor();
				getter.setField(field);
				getter.setValueType(TypeUtils.getType(field.getType()));
				meta.setKeyValueAccessor(getter);
			}
		}
	}

	private void enforceSingleKeyColumn(RowMapperMetaData meta) {
		if (meta.getKeyValueAccessor() != null) {
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
