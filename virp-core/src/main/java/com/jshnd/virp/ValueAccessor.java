package com.jshnd.virp;

public interface ValueAccessor<T> {

	public T getValue(Object sourceObject);

	public Class<T> getValueType();

	public Object getMeta();

	public void setMeta(Object meta);

}
