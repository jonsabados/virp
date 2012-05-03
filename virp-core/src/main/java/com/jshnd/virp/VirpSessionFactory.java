package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.SessionAttachmentMode;

public interface VirpSessionFactory {

	public VirpSession newSession(VirpConfig config, SessionAttachmentMode attachmentMode);

	public void setupClass(RowMapperMetaData<?> type);

}
