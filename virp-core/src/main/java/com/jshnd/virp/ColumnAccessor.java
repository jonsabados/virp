package com.jshnd.virp;

public interface ColumnAccessor<T, V> {

	public StaticValueAccessor<T> getColumnIdentifier();

	public ValueManipulator<V> getValueManipulator();

}
