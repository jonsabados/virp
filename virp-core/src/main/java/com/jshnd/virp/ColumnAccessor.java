package com.jshnd.virp;

public interface ColumnAccessor<T, V> {

	public ValueAccessor<T> getColumnIdentifier();

	public ValueManipulator<V> getValueManipulator();

}
