package com.jshnd.virp.query;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.SessionFactoryDataHolder;
import com.jshnd.virp.StaticValueAccessor;
import com.jshnd.virp.ValueManipulator;

public class ColumnAccessorQueryParameter<T, V> implements QueryParameter<T, V> {

	private StaticValueAccessor<T> columnIdentifier;

	private ValueManipulator<V> sessionFactoryData;

	private V argument;

	private Criteria criteria;

	public ColumnAccessorQueryParameter(ColumnAccessor<T, V> accessor, Object argSource, Criteria criteria) {
		this.columnIdentifier = accessor.getColumnIdentifier();
		this.sessionFactoryData = accessor.getValueManipulator();
		this.argument = sessionFactoryData.getValue(argSource);
		this.criteria = criteria;
	}

	@Override
	public StaticValueAccessor<T> getColumnIdentifier() {
		return columnIdentifier;
	}

	@Override
	public SessionFactoryDataHolder<V> getSessionFactoryData() {
		return sessionFactoryData;
	}

	@Override
	public V getArgument() {
		return argument;
	}

	@Override
	public Criteria getCriteria() {
		return criteria;
	}
}
