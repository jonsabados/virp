/*
 * Copyright 2012 Jonathan Sabados
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;
import com.jshnd.virp.query.ByExampleQuery;
import com.jshnd.virp.query.Query;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class VirpSession {

	protected VirpConfig config;

	protected VirpSessionSpec sessionSpec;
	
	private boolean open = true;

	private class SessionProxy<T> implements MethodInterceptor, VirpProxy<T> {

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
			if(method.getDeclaringClass().equals(VirpProxy.class)) {
				return wrappedMethod(method, args);
			} else {
				return delegate(method, args);
			}
		}

		private Object wrappedMethod(Method method, Object[] args) 
				throws IllegalAccessException, InvocationTargetException {
			return method.invoke(this, args);
		}
		
		private Object delegate(Method method, Object[] args)
				throws IllegalAccessException, InvocationTargetException {
			Object ret = method.invoke(wrapped, args);
			if(args.length == 1) {
				if(method.equals(keySetter)) {
					throw new VirpOperationException("Attempt to modify key for "
							+ wrapped.getClass().getCanonicalName() + ", key value: "
							+ meta.getKeyValueManipulator().getValue(wrapped));
				}
				ColumnAccessor<?, ?> accessor = columnAccessors.get(method);
				if(accessor != null) {
					if(!open) {
						throw new VirpOperationException("Attempt to modify detached instance");
					}
					doChange(meta, wrapped, accessor);
				}
			}
			return ret;
		}

		@Override
		public T getInstanceVirpWrapped() {
			return wrapped;
		}
	}

	public VirpSession(VirpConfig config, VirpSessionSpec sessionSpec) {
		this.config = config;
		this.sessionSpec = sessionSpec;
	}

	public <T> void save(T row) {
		sanityCheck();
		doSave(getMetaForInstance(row), row);
	}
	
	public <T> void delete(T row) {
		sanityCheck();
		doDelete(getMetaForInstance(row), row);
	}
	
	public <T, K> T get(Class<T> rowClass, K key) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMetaForClass(rowClass);
		T ret = doGet(meta, key);
		if(sessionSpec.getSessionAttachmentMode().isAttach()) {
			ret = proxyForSession(ret, meta);
		}
		return ret;
	}

	public <T, K> List<T> get(Class<T> rowClass, K... keys) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMetaForClass(rowClass);
		List<T> objects = getWithMeta(meta, keys);
		return listForSession(objects, meta);
	}

	public <T> List<T> find(Query<T> query) {
		sanityCheck();
		RowMapperMetaData<T> meta = query.getMeta();
		List<T> objects = doFind(query, meta);
		return listForSession(objects, meta);
	}

	public <T> Query<T> createByExampleQuery(T theExample) {
		sanityCheck();
		RowMapperMetaData<T> meta = getMetaForInstance(theExample);
		return new ByExampleQuery<T>(meta, theExample);
	}

	private <T> List<T> listForSession(List<T> objects, RowMapperMetaData<T> meta) {
		List<T> ret;
		if(sessionSpec.getSessionAttachmentMode().isAttach()) {
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
		RowMapperMetaData<T> meta = getMetaForClass(rowClass);
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
		doClose();
		if(sessionSpec.getSessionAttachmentMode().isAutoFlush()) {
			return flush();
		} else {
			return VirpActionResult.NONE;
		}
	}

	public VirpActionResult flush() {
		return doFlush();
	}
	
	@SuppressWarnings("unchecked")
	public <T, V extends T> T detachedInstance(V potentiallyAttachedInstance) {
		T ret;
		if(potentiallyAttachedInstance instanceof VirpProxy) {
			ret = ((VirpProxy<T>)potentiallyAttachedInstance).getInstanceVirpWrapped();
		} else {
			ret = potentiallyAttachedInstance;
		}
		return ret;
	}
	
	private <T, V extends T> RowMapperMetaData<T> getMetaForInstance(V forInstance) {
		return getMetaForClass(detachedInstance(forInstance).getClass());
	}
	
	private <T> RowMapperMetaData<T> getMetaForClass(Class<?> type) {
		@SuppressWarnings("unchecked")
		RowMapperMetaData<T> ret = (RowMapperMetaData<T>) config.getMetaData(type);
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
		if(object == null) {
			return null;
		}
		return (T) Enhancer.create(object.getClass(),
									new Class[] { VirpProxy.class }, 
									new SessionProxy<T>(object, meta));
	}

	public VirpSessionSpec getSessionSpec() {
		return sessionSpec;
	}

	protected abstract <T> void doSave(RowMapperMetaData<T> type, T row);
	
	protected abstract <T> void doDelete(RowMapperMetaData<T> type, T row);

	protected abstract <T> void doChange(RowMapperMetaData<T> type, T row, ColumnAccessor<?, ?> changeAccessor);

	protected abstract <T, K> T doGet(RowMapperMetaData<T> type, K key);

	protected abstract <T, K> List<T> doGet(RowMapperMetaData<T> type, K... keys);

	protected abstract <T> List<T> doFind(Query<T> query, RowMapperMetaData<T> meta);
	
	protected abstract void doClose();
	
	protected abstract VirpActionResult doFlush();

}
