package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;

public interface VirpSessionFactory {

	public <T> VirpSession<T> newSession(RowMapperMetaData<T> forType);

	public void setupClass(RowMapperMetaData type);

}
