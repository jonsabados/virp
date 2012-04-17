package com.jshnd.virp.config;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueAccessor;

import java.util.Collections;
import java.util.Set;

public class RowMapperMetaData<T> {

	private Class<T> rowMapperClass;

	private String columnFamily;

	private ValueAccessor<?> keyValueAccessor;

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

	public ValueAccessor<?> getKeyValueAccessor() {
		return keyValueAccessor;
	}

	public void setKeyValueAccessor(ValueAccessor<?> keyValueAccessor) {
		this.keyValueAccessor = keyValueAccessor;
	}
}
