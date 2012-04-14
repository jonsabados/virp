package com.jshnd.virp;

public class BasicColumnAccessor<T, V> implements ColumnAccessor<T, V> {

	private T columnIdentifier;

	private Class<T> columnIdentifierType;

	private ValueAccessor<V> valueAccessor;

	public BasicColumnAccessor(T columnIdentifier, Class<T> columnIdentifierType, ValueAccessor<V> valueAccessor) {
		this.columnIdentifier = columnIdentifier;
		this.columnIdentifierType = columnIdentifierType;
		this.valueAccessor = valueAccessor;
	}

	@Override
	public T getColumnIdentifier() {
		return columnIdentifier;
	}

	@Override
	public V getValue(Object sourceObject) {
		return valueAccessor.getValue(sourceObject);
	}

	@Override
	public Class<T> getColumnIdentifierType() {
		return columnIdentifierType;
	}

	@Override
	public Class<V> getValueType() {
		return valueAccessor.getValueType();
	}
}
