package com.jshnd.virp.reflection;

import com.jshnd.virp.ColumnGetter;
import com.jshnd.virp.VirpException;

import java.lang.reflect.Field;

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
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	public void setField(Field field) {
		this.field = field;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

}
