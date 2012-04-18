package com.jshnd.virp;

public class BasicColumnAccessor<T, V> implements ColumnAccessor<T, V> {

	private ValueAccessor<T> columnIdentifier;

	private ValueManipulator<V> valueAccessor;

	public BasicColumnAccessor(ValueAccessor<T> columnIdentifier, ValueManipulator<V> valueAccessor) {
		this.columnIdentifier = columnIdentifier;
		this.valueAccessor = valueAccessor;
	}

	@Override
	public ValueAccessor<T> getColumnIdentifier() {
		return columnIdentifier;
	}

	@Override
	public ValueManipulator<V> getValueManipulator() {
		return valueAccessor;
	}

}
