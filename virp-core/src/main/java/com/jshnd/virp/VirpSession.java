package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;

public abstract class VirpSession<T> {

	private boolean open = true;

	protected RowMapperMetaData rowMeta;

	public VirpSession(RowMapperMetaData<T> rowMeta) {
		this.rowMeta = rowMeta;
	}

	public void save(T row) {
		sanityCheck();
		doSave(row);
	}

	private void sanityCheck() {
		if(!open) {
			throw new VirpOperationException("Session has been closed");
		}
	}

	public T get(Object key) {
		sanityCheck();
		return doGet(key);
	}

	public VirpActionResult close() {
		sanityCheck();
		open = false;
		return doClose();
	}

	protected abstract void doSave(Object row);

	protected abstract T doGet(Object key);

	protected abstract VirpActionResult doClose();

}
