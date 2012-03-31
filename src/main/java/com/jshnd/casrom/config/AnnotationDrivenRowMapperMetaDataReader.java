package com.jshnd.casrom.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jshnd.casrom.ColumnGetter;
import com.jshnd.casrom.annotation.Column;
import com.jshnd.casrom.reflection.MethodGetter;
import com.jshnd.casrom.reflection.PropertyGetter;

public class AnnotationDrivenRowMapperMetaDataReader implements RowMapperMetaDataReader {

	private static final Logger log = LoggerFactory.getLogger(AnnotationDrivenRowMapperMetaDataReader.class);
	
	private boolean readProperties = true;
	
	private boolean readMethods = true;
	
	@Override
	public RowMapperMetaData readClass(Class<?> clazz) {
		RowMapperMetaData ret = new RowMapperMetaData(clazz);
		Set<ColumnGetter> getters = new HashSet<ColumnGetter>();
		if(readMethods) {
			generateMethodGetters(clazz, getters);
		}
		if(readProperties) {
			generatePropertyGetters(clazz, getters);
		}
		ret.setColumnGetters(getters);
		return ret;
	}

	private void generateMethodGetters(Class<?> clazz, Set<ColumnGetter> columnGetters) {
		Method[] methods = clazz.getDeclaredMethods();
		log.info("Inspecting " + methods.length + " for annotation " + Column.class.getCanonicalName());
		for(Method method : methods) {
			Column column = method.getAnnotation(Column.class);
			if(column != null) {
				if(!method.isAccessible()) {
					method.setAccessible(true);
				}
				MethodGetter getter = new MethodGetter();
				getter.setColumnName(column.name());
				getter.setGetterMethod(method);
				columnGetters.add(getter);
			}
		}
	}
	
	private void generatePropertyGetters(Class<?> clazz, Set<ColumnGetter> columnGetters) {
		Field[] fields = clazz.getDeclaredFields();
		log.info("Inspecting " + fields.length + " for annotation " + Column.class.getCanonicalName());
		for(Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column != null) {
				if(!field.isAccessible()) {
					field.setAccessible(true);
				}
				PropertyGetter getter = new PropertyGetter();
				getter.setColumnName(column.name());
				getter.setField(field);
				columnGetters.add(getter);
			}
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
