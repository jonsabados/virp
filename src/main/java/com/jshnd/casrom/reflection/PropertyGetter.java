package com.jshnd.casrom.reflection;

import java.lang.reflect.Field;

import com.jshnd.casrom.CasromException;
import com.jshnd.casrom.ColumnGetter;

public class PropertyGetter implements ColumnGetter {

	private Field field;
	
	private String columnName;

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	public Object getColumnValue(Object sourceObject) {
		try {
			return field.get(sourceObject);
		} catch(Exception e) {
			throw new CasromException(e);
		}
	}

	public void setField(Field field) {
		this.field = field;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
}
