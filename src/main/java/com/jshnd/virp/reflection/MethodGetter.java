package com.jshnd.virp.reflection;

import java.lang.reflect.Method;

import com.jshnd.virp.VirpException;
import com.jshnd.virp.ColumnGetter;

public class MethodGetter implements ColumnGetter {

	private Method getterMethod;

	private String columnName;

	public Object getColumnValue(Object sourceObject) {
		try {
			return getterMethod.invoke(sourceObject);
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	public void setGetterMethod(Method getterMethod) {
		this.getterMethod = getterMethod;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

}
