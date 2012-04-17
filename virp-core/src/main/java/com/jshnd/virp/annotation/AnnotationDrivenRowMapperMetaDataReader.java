package com.jshnd.virp.annotation;

import com.jshnd.virp.BasicColumnAccessor;
import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.StaticValueAccessor;
import com.jshnd.virp.exception.VirpAnnotationException;
import com.jshnd.virp.exception.VirpException;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.reflection.ReflectionFieldValueAccessor;
import com.jshnd.virp.reflection.ReflectionMethodValueAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AccessibleObject;
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
		Set<ColumnAccessor<?, ?>> getters = new HashSet<ColumnAccessor<?, ?>>();
		if (readMethods) {
			generateMethodGetters(clazz, ret, getters);
		}
		if (readProperties) {
			generatePropertyGetters(clazz, ret, getters);
		}
		ret.setColumnAccessors(getters);
		return ret;
	}

	private void generateMethodGetters(Class<?> clazz, RowMapperMetaData meta,
									   Set<ColumnAccessor<?, ?>> valueAccessors) {
		Method[] methods = clazz.getDeclaredMethods();
		log.info("Inspecting " + methods.length + " for annotation " + NamedColumn.class.getCanonicalName());
		for (Method method : methods) {
			NamedColumn namedColumn = method.getAnnotation(NamedColumn.class);
			if (namedColumn != null) {
				makeAccessibleIfNot(method);
				ReflectionMethodValueAccessor<Object> accessor = new ReflectionMethodValueAccessor<Object>(method);
				valueAccessors.add(new BasicColumnAccessor<String, Object>(
						new StaticValueAccessor<String>(namedColumn.name(), String.class), accessor));
			}
			NumberedColumn numberedColumn = method.getAnnotation(NumberedColumn.class);
			if (numberedColumn != null) {
				makeAccessibleIfNot(method);
				ReflectionMethodValueAccessor<Object> accessor = new ReflectionMethodValueAccessor<Object>(method);
				valueAccessors.add(new BasicColumnAccessor<Long, Object>(
						new StaticValueAccessor<Long>(Long.valueOf(numberedColumn.number()), Long.class), accessor));
			}
			if (method.getAnnotation(KeyColumn.class) != null) {
				enforceSingleKeyColumn(meta);
				makeAccessibleIfNot(method);
				ReflectionMethodValueAccessor accessor = new ReflectionMethodValueAccessor(method);
				meta.setKeyValueAccessor(accessor);
			}
		}
	}

	private void makeAccessibleIfNot(AccessibleObject accessibleObject) {
		if (!accessibleObject.isAccessible()) {
			accessibleObject.setAccessible(true);
		}
	}

	private void generatePropertyGetters(Class<?> clazz, RowMapperMetaData meta,
										 Set<ColumnAccessor<?, ?>> valueAccessors) {
		Field[] fields = clazz.getDeclaredFields();
		log.info("Inspecting " + fields.length + " for annotation " + NamedColumn.class.getCanonicalName());
		for (Field field : fields) {
			NamedColumn column = field.getAnnotation(NamedColumn.class);
			if (column != null) {
				makeAccessibleIfNot(field);
				ReflectionFieldValueAccessor<Object> accessor = new ReflectionFieldValueAccessor<Object>(field);
				valueAccessors.add(new BasicColumnAccessor<String, Object>(
						new StaticValueAccessor<String>(column.name(), String.class), accessor));
			}
			NumberedColumn numberedColumn = field.getAnnotation(NumberedColumn.class);
			if (numberedColumn != null) {
				makeAccessibleIfNot(field);
				ReflectionFieldValueAccessor<Object> accessor = new ReflectionFieldValueAccessor<Object>(field);
				valueAccessors.add(new BasicColumnAccessor<Long, Object>(
						new StaticValueAccessor<Long>(Long.valueOf(numberedColumn.number()), Long.class), accessor));
			}
			if (field.getAnnotation(KeyColumn.class) != null) {
				enforceSingleKeyColumn(meta);
				makeAccessibleIfNot(field);
				ReflectionFieldValueAccessor<Object> getter = new ReflectionFieldValueAccessor<Object>(field);
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
