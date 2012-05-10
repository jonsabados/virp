package com.jshnd.virp.query;

import com.jshnd.virp.config.RowMapperMetaData;

import java.util.Collection;

public interface Query<T> {

	public Collection<QueryParameter<?, ?>> getParameters();

	public RowMapperMetaData<T> getMeta();

}
