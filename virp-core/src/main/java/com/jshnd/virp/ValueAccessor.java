package com.jshnd.virp;

public interface ValueAccessor<T> extends SessionFactoryDataHolder<T> {

	public T getValue(Object sourceObject);

}
