package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class VirpSession {

	private boolean open = true;

	protected VirpConfig config;

	public VirpSession(VirpConfig config) {
		this.config = config;
	}

	public <T> void save(T row) {
		sanityCheck();
		doSave(getMeta((Class<T>) row.getClass()), row);
	}

	private void sanityCheck() {
		if(!open) {
			throw new VirpOperationException("Session has been closed");
		}
	}

	public <T, K> T get(Class<T> rowClass, K key) {
		sanityCheck();
		return doGet(getMeta(rowClass), key);
	}

	public <T, K> List<T> get(Class<T> rowClass, K... keys) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMeta(rowClass);
		return doGet(meta, keys);
	}

	public <T, K> Map<K, T> getMapped(Class<T> rowClass, K... keys) {
		RowMapperMetaData<T> meta = getMeta(rowClass);
		Collection<T> values = get(rowClass, keys);
		ValueAccessor<K> accessor = (ValueAccessor<K>) meta.getKeyValueManipulator();
		Map<K, T> ret = new HashMap<K, T>();
		for(T row : values) {
			ret.put(accessor.getValue(row), row);
		}
		return ret;
	}

	public VirpActionResult close() {
		sanityCheck();
		open = false;
		return doClose();
	}

	private <T> RowMapperMetaData<T> getMeta(Class<T> type) {
		RowMapperMetaData<T> ret = config.getMetaData(type);
		if(ret == null) {
			throw new VirpOperationException("Unconfigured class " + type.getName());
		}
		return ret;
	}

	protected abstract <T> void doSave(RowMapperMetaData<T> type, T row);

	protected abstract <T, K> T doGet(RowMapperMetaData<T> type, K key);

	protected abstract <T, K> List<T> doGet(RowMapperMetaData<T> type, K... keys);

	protected abstract VirpActionResult doClose();

}
