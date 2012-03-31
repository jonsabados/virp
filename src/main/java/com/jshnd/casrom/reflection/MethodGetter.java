package com.jshnd.casrom.reflection;

import java.lang.reflect.Method;

import com.jshnd.casrom.CasromException;
import com.jshnd.casrom.ColumnGetter;

public class MethodGetter implements ColumnGetter {

	private Method getterMethod;

	private String columnName;

	public Object getColumnValue(Object sourceObject) {
		try {
			return getterMethod.invoke(sourceObject);
		} catch (Exception e) {
			throw new CasromException(e);
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
