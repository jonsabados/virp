package com.jshnd.virp.query;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.config.RowMapperMetaData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ByExampleQuery<T> implements Query {

	private Set<QueryParameter<?, ?>> parameters = new HashSet<QueryParameter<?, ?>>();

	private RowMapperMetaData<T> meta;

	@SuppressWarnings("unchecked")
	public ByExampleQuery(RowMapperMetaData<T> classMeta, T example) {
		this.meta = classMeta;
		for(ColumnAccessor accessor : classMeta.getColumnAccessors()) {
			if(accessor.getValueManipulator().getValue(example) != null) {
				parameters.add(new ColumnAccessorQueryParameter(accessor, example, Criteria.EQUAL));
			}
		}
	}

	@Override
	public Collection<QueryParameter<?, ?>> getParameters() {
		return Collections.unmodifiableSet(parameters);
	}

	@Override
	public RowMapperMetaData getMeta() {
		return meta;
	}
}
