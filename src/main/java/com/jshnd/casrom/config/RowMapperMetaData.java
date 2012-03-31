package com.jshnd.casrom.config;

public class RowMapperMetaData {

	private Class<?> rowMapperClass;

	public RowMapperMetaData(Class<?> rowMapperClass) {
		super();
		this.rowMapperClass = rowMapperClass;
	}

	public Class<?> getRowMapperClass() {
		return rowMapperClass;
	}

	public void setRowMapperClass(Class<?> rowMapperClass) {
		this.rowMapperClass = rowMapperClass;
	}
	
}
