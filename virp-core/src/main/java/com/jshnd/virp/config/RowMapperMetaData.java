package com.jshnd.virp.config;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueManipulator;

import java.util.Collections;
import java.util.Set;

public class RowMapperMetaData<T> {

	private Class<T> rowMapperClass;

	private String columnFamily;

	private ValueManipulator<?> keyValueManipulator;

	private Set<ColumnAccessor<?, ?>> columnAccessors;

	public RowMapperMetaData(Class<T> rowMapperClass) {
		super();
		this.rowMapperClass = rowMapperClass;
	}

	public Class<T> getRowMapperClass() {
		return rowMapperClass;
	}

	public String getColumnFamily() {
		return columnFamily;
	}

	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}

	public Set<ColumnAccessor<?,?>> getColumnAccessors() {
		return columnAccessors;
	}

	public void setColumnAccessors(Set<ColumnAccessor<?,?>> columnAccessors) {
		this.columnAccessors = Collections.unmodifiableSet(columnAccessors);
	}

	public ValueManipulator<?> getKeyValueManipulator() {
		return keyValueManipulator;
	}

	public void setKeyValueManipulator(ValueManipulator<?> keyValueManipulator) {
		this.keyValueManipulator = keyValueManipulator;
	}
}
