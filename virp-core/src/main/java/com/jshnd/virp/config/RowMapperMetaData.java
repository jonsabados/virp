package com.jshnd.virp.config;

import com.jshnd.virp.ColumnGetter;

import java.util.Collections;
import java.util.Set;

public class RowMapperMetaData {

	private Class<?> rowMapperClass;

	private String columnFamily;

	private ColumnGetter keyColumnGetter;

	private Set<ColumnGetter> columnGetters;

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

	public String getColumnFamily() {
		return columnFamily;
	}

	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}

	public Set<ColumnGetter> getColumnGetters() {
		return columnGetters;
	}

	public void setColumnGetters(Set<ColumnGetter> columnGetters) {
		this.columnGetters = Collections.unmodifiableSet(columnGetters);
	}

	public ColumnGetter getKeyColumnGetter() {
		return keyColumnGetter;
	}

	public void setKeyColumnGetter(ColumnGetter keyColumnGetter) {
		this.keyColumnGetter = keyColumnGetter;
	}
}
