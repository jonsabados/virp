package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;

public abstract class VirpSession {

	private boolean open = true;

	protected RowMapperMetaData rowMeta;

	public VirpSession(RowMapperMetaData rowMeta) {
		this.rowMeta = rowMeta;
	}

	public void save(Object row) {
		if(!row.getClass().isAssignableFrom(rowMeta.getRowMapperClass())) {
			throw new VirpOperationException("May only operate on objects of type " +
					rowMeta.getRowMapperClass().getCanonicalName());
		}
		if(!open) {
			throw new VirpOperationException("Session has been closed");
		}
		doSave(row);
	}

	public VirpActionResult close() {
		if(!open) {
			throw new VirpOperationException("Session has been closed");
		}
		open = false;
		return doClose();
	}

	protected abstract void doSave(Object row);

	protected abstract VirpActionResult doClose();

}
