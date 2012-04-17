package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;

public interface VirpSessionFactory {

	public VirpSession newSession(RowMapperMetaData forType);

	public void setupClass(RowMapperMetaData type);

}
