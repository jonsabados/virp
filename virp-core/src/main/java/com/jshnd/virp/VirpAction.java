package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;

public interface VirpAction {

	public void writeRow(Object row, RowMapperMetaData rowMeta);

	public VirpActionResult complete() throws VirpException;

}