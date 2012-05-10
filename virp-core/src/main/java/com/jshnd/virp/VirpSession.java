package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.SessionAttachmentMode;
import com.jshnd.virp.exception.VirpOperationException;
import com.jshnd.virp.query.ByExampleQuery;
import com.jshnd.virp.query.Query;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.*;

public abstract class VirpSession {

	protected VirpConfig config;

	private SessionAttachmentMode attachmentMode;

	private boolean open = true;

	private class SessionProxy<T> implements MethodInterceptor {

		private T wrapped;

		private RowMapperMetaData<T> meta;

		private Method keySetter;

		private Map<Method, ColumnAccessor<?, ?>> columnAccessors;

		SessionProxy(T wrapped, RowMapperMetaData<T> meta) {
			this.wrapped = wrapped;
			this.meta = meta;
			this.columnAccessors = meta.getAccessorMap();
			this.keySetter = meta.getKeyGetter();
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object ret = method.invoke(wrapped, args);
			if(args.length == 1) {
				if(method.equals(keySetter)) {
					throw new VirpOperationException("Attempt to modify key for "
							+ wrapped.getClass().getCanonicalName() + ", key value: "
							+ meta.getKeyValueManipulator().getValue(wrapped));
				}
				ColumnAccessor<?, ?> accessor = columnAccessors.get(method);
				if(accessor != null) {
					doChange(meta, wrapped, accessor);
				}
			}
			return ret;
		}
	}

	public VirpSession(VirpConfig config, SessionAttachmentMode attachmentMode) {
		this.config = config;
		this.attachmentMode = attachmentMode;
	}

	@SuppressWarnings("unchecked")
	public <T> void save(T row) {
		sanityCheck();
		doSave(getMeta((Class<T>) row.getClass()), row);
	}

	public <T, K> T get(Class<T> rowClass, K key) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMeta(rowClass);
		T ret = doGet(meta, key);
		if(attachmentMode.isAttach()) {
			ret = proxyForSession(ret, meta);
		}
		return ret;
	}

	public <T, K> List<T> get(Class<T> rowClass, K... keys) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMeta(rowClass);
		List<T> objects = getWithMeta(meta, keys);
		return listForSession(objects, meta);
	}

	public <T> List<T> find(Query<T> query) {
		sanityCheck();
		RowMapperMetaData<T> meta = query.getMeta();
		List<T> objects = doFind(query, meta);
		return listForSession(objects, meta);
	}

	@SuppressWarnings("unchecked")
	public <T> Query<T> createByExampleQuery(T theExample) {
		return createByExampleQuery((Class<T>) theExample.getClass(), theExample);
	}

	public <T> Query<T> createByExampleQuery(Class<T> forClass, T theExample) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMeta(forClass);
		return new ByExampleQuery<T>(meta, theExample);
	}

	private <T> List<T> listForSession(List<T> objects, RowMapperMetaData<T> meta) {
		List<T> ret;
		if(attachmentMode.isAttach()) {
			ret = new ArrayList<T>();
			for(T t : objects) {
				ret.add(proxyForSession(t, meta));
			}
		} else {
			ret = objects;
		}
		return ret;
	}

	private <T, K> List<T> getWithMeta(RowMapperMetaData<T> meta, K... keys) {
		return doGet(meta, keys);
	}

	public <T, K> Map<K, T> getAsMap(Class<T> rowClass, K... keys) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMeta(rowClass);
		Collection<T> values = getWithMeta(meta, keys);
		ValueAccessor<K> accessor = meta.getKeyValueManipulator();
		Map<K, T> ret = new HashMap<K, T>();
		for(T row : values) {
			ret.put(accessor.getValue(row), row);
		}
		return ret;
	}

	public VirpActionResult close() {
		sanityCheck();
		open = false;
		return flush();
	}

	public VirpActionResult flush() {
		return doFlush();
	}

	private <T> RowMapperMetaData<T> getMeta(Class<T> type) {
		RowMapperMetaData<T> ret = config.getMetaData(type);
		if(ret == null) {
			throw new VirpOperationException("Unconfigured class " + type.getName());
		}
		return ret;
	}

	private void sanityCheck() {
		if(!open) {
			throw new VirpOperationException("Session has been closed");
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T proxyForSession(T object, RowMapperMetaData<T> meta) {
		return (T) Enhancer.create(object.getClass(), new SessionProxy(object, meta));
	}

	public SessionAttachmentMode getAttachmentMode() {
		return attachmentMode;
	}

	protected abstract <T> void doSave(RowMapperMetaData<T> type, T row);

	protected abstract <T> void doChange(RowMapperMetaData<T> type, T row, ColumnAccessor<?, ?> changeAccessor);

	protected abstract <T, K> T doGet(RowMapperMetaData<T> type, K key);

	protected abstract <T, K> List<T> doGet(RowMapperMetaData<T> type, K... keys);

	protected abstract <T> List<T> doFind(Query<T> query, RowMapperMetaData<T> meta);

	protected abstract VirpActionResult doFlush();

}
