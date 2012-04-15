package com.jshnd.virp;

public class BasicColumnAccessor<T, V> implements ColumnAccessor<T, V> {

	private ValueAccessor<T> columnIdentifier;

	private ValueAccessor<V> valueAccessor;

	public BasicColumnAccessor(ValueAccessor<T> columnIdentifier, ValueAccessor<V> valueAccessor) {
		this.columnIdentifier = columnIdentifier;
		this.valueAccessor = valueAccessor;
	}

	@Override
	public ValueAccessor<T> getColumnIdentifier() {
		return columnIdentifier;
	}

	@Override
	public ValueAccessor<V> getValueAccessor() {
		return valueAccessor;
	}

}
