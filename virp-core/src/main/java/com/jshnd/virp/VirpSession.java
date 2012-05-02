package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.*;

public abstract class VirpSession {

	protected VirpConfig config;

	private boolean open = true;

	private Map<Object, Set<ColumnAccessor<?, ?>>> objectModifications;

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
			if(args.length == 1) {
				if(method.equals(keySetter)) {
					throw new VirpOperationException("Attempt to modify key for "
							+ wrapped.getClass().getCanonicalName() + ", key value: "
							+ meta.getKeyValueManipulator().getValue(wrapped));
				}
				ColumnAccessor<?, ?> accessor = columnAccessors.get(method);
				if(accessor != null) {
					if(!objectModifications.containsKey(wrapped)) {
						objectModifications.put(wrapped, new HashSet<ColumnAccessor<?, ?>>());
					}
					objectModifications.get(wrapped).add(accessor);
				}
			}
			return method.invoke(wrapped, args);
		}
	}

	public VirpSession(VirpConfig config) {
		this.config = config;
		if(config.isSessionAttachmentOn()) {
			objectModifications = new HashMap<Object, Set<ColumnAccessor<?, ?>>>();
		}
	}

	public <T> void save(T row) {
		sanityCheck();
		doSave(getMeta((Class<T>) row.getClass()), row);
	}

	public <T, K> T get(Class<T> rowClass, K key) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMeta(rowClass);
		T ret = doGet(meta, key);
		if(config.isSessionAttachmentOn()) {
			ret = proxyForSession(ret, meta);
		}
		return ret;
	}

	public <T, K> List<T> get(Class<T> rowClass, K... keys) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMeta(rowClass);
		List<T> objects = getWithMeta(meta, keys);
		List<T> ret;
		if(config.isSessionAttachmentOn()) {
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
		return doClose();
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
		Class clazz = object.getClass();
		return (T) Enhancer.create(object.getClass(), new SessionProxy(object, meta));
	}

	protected Map<Object, Set<ColumnAccessor<?, ?>>> getObjectModifications() {
		return objectModifications;
	}

	protected abstract <T> void doSave(RowMapperMetaData<T> type, T row);

	protected abstract <T, K> T doGet(RowMapperMetaData<T> type, K key);

	protected abstract <T, K> List<T> doGet(RowMapperMetaData<T> type, K... keys);

	protected abstract VirpActionResult doClose();

}
