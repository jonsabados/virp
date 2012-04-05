package com.jshnd.virp.config;

import java.util.Collections;
import java.util.Set;

import com.jshnd.virp.ColumnGetter;

public class RowMapperMetaData {

	private Class<?> rowMapperClass;

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
