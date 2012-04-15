package com.jshnd.virp;

public interface ColumnAccessor<T, V> {

	public ValueAccessor<T> getColumnIdentifier();

	public ValueAccessor<V> getValueAccessor();

	public T getColumnIdentifier();

	public Class<T> getColumnIdentifierType();

	public Object getActionFactoryMeta();

	public void setActionFactoryMeta(Object meta);

}
