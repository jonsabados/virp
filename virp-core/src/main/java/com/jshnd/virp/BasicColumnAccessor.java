package com.jshnd.virp;

public class BasicColumnAccessor<T, V> implements ColumnAccessor<T, V> {

	private StaticValueAccessor<T> columnIdentifier;

	private ValueManipulator<V> valueAccessor;

	public BasicColumnAccessor(StaticValueAccessor<T> columnIdentifier, ValueManipulator<V> valueAccessor) {
		this.columnIdentifier = columnIdentifier;
		this.valueAccessor = valueAccessor;
	}

	@Override
	public StaticValueAccessor<T> getColumnIdentifier() {
		return columnIdentifier;
	}

	@Override
	public ValueManipulator<V> getValueManipulator() {
		return valueAccessor;
	}

}
