package com.jshnd.virp;

public interface ColumnAccessor<T, V> {

	public StaticValueAccessor<T> getColumnIdentifier();

	public ValueAccessor<Integer> getTimeToLive();

	public ValueManipulator<V> getValueManipulator();

}
