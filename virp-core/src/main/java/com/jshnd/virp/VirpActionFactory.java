package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;

public interface VirpActionFactory {

	public VirpAction newAction(RowMapperMetaData forType);

}
