package com.jshnd.virp.query;

import com.jshnd.virp.SessionFactoryDataHolder;
import com.jshnd.virp.StaticValueAccessor;

public interface QueryParameter<T, V> {

	public StaticValueAccessor<T> getColumnIdentifier();

	public SessionFactoryDataHolder<V> getSessionFactoryData();

	public V getArgument();

	public Criteria getCriteria();

}
