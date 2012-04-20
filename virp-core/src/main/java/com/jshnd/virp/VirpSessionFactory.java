package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;

public interface VirpSessionFactory {

	public VirpSession newSession(VirpConfig config);

	public void setupClass(RowMapperMetaData<?> type);

}
