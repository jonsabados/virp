package com.jshnd.virp;

public interface ColumnAccessor<T, V> extends ValueAccessor<V> {

	public T getColumnIdentifier();

	public Class<T> getColumnIdentifierType();

}
