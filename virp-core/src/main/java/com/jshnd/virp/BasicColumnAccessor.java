package com.jshnd.virp;

public class BasicColumnAccessor implements ColumnAccessor {

	private String columnName;

	private ValueAccessor valueAccessor;

	public BasicColumnAccessor(String columnName, ValueAccessor valueAccessor) {
		this.columnName = columnName;
		this.valueAccessor = valueAccessor;
	}

	@Override
	public String getColumnName() {
		return columnName;
	}

	@Override
	public Object getValue(Object sourceObject) {
		return valueAccessor.getValue(sourceObject);
	}

	@Override
	public ValueType getValueType() {
		return valueAccessor.getValueType();
	}
}
