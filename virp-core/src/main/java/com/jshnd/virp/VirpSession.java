package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;

public abstract class VirpSession {

	private boolean open = true;

	protected VirpConfig config;

	public VirpSession(VirpConfig config) {
		this.config = config;
	}

	public void save(Object row) {
		sanityCheck();
		doSave(getMeta(row.getClass()), row);
	}

	private void sanityCheck() {
		if(!open) {
			throw new VirpOperationException("Session has been closed");
		}
	}

	public <T> T get(Class<T> rowClass, Object key) {
		sanityCheck();
		return doGet((RowMapperMetaData<T>) config.getMetaData(rowClass), key);
	}

	public VirpActionResult close() {
		sanityCheck();
		open = false;
		return doClose();
	}

	private RowMapperMetaData getMeta(Class<?> type) {
		RowMapperMetaData ret = config.getMetaData(type);
		if(ret == null) {
			throw new VirpOperationException("Unconfigured class " + type.getName());
		}
		return ret;
	}

	protected abstract <T> void doSave(RowMapperMetaData<T> type, T row);

	protected abstract <T> T doGet(RowMapperMetaData<T> type, Object key);

	protected abstract VirpActionResult doClose();

}
