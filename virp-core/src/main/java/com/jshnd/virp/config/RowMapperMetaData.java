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

package com.jshnd.virp.config;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueManipulator;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RowMapperMetaData<T> {

	private Class<T> rowMapperClass;

	private String columnFamily;

	private ValueManipulator<?> keyValueManipulator;

	private Set<ColumnAccessor<?, ?>> columnAccessors;

	private Map<Method, ColumnAccessor<?, ?>> accessorMap;

	private Method keyGetter;

	public RowMapperMetaData(Class<T> rowMapperClass) {
		super();
		this.rowMapperClass = rowMapperClass;
	}

	public Class<T> getRowMapperClass() {
		return rowMapperClass;
	}

	public String getColumnFamily() {
		return columnFamily;
	}

	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}

	public Set<ColumnAccessor<?,?>> getColumnAccessors() {
		return columnAccessors;
	}

	public void setColumnAccessors(Set<ColumnAccessor<?,?>> columnAccessors) {
		this.columnAccessors = Collections.unmodifiableSet(columnAccessors);
		Map<Method, ColumnAccessor<?,?>> accessorMap =
				new HashMap<Method, ColumnAccessor<?, ?>>(columnAccessors.size());
		for(ColumnAccessor<?,?> accessor : columnAccessors) {
			accessorMap.put(accessor.getValueManipulator().getSetter(), accessor);
		}
		this.accessorMap = Collections.unmodifiableMap(accessorMap);
	}

	public Map<Method, ColumnAccessor<?, ?>> getAccessorMap() {
		return accessorMap;
	}

	public <T> ValueManipulator<T> getKeyValueManipulator() {
		return (ValueManipulator<T>) keyValueManipulator;
	}

	public Method getKeyGetter() {
		return keyGetter;
	}

	public void setKeyValueManipulator(ValueManipulator<?> keyValueManipulator) {
		this.keyValueManipulator = keyValueManipulator;
		this.keyGetter = keyValueManipulator.getSetter();
	}
}
