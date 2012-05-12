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

package com.jshnd.virp.query;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.SessionFactoryDataHolder;
import com.jshnd.virp.StaticValueAccessor;
import com.jshnd.virp.ValueManipulator;

public class ColumnAccessorQueryParameter<T, V> implements QueryParameter<T, V> {

	private StaticValueAccessor<T> columnIdentifier;

	private ValueManipulator<V> sessionFactoryData;

	private V argument;

	private Criteria criteria;

	public ColumnAccessorQueryParameter(ColumnAccessor<T, V> accessor, Object argSource, Criteria criteria) {
		this.columnIdentifier = accessor.getColumnIdentifier();
		this.sessionFactoryData = accessor.getValueManipulator();
		this.argument = sessionFactoryData.getValue(argSource);
		this.criteria = criteria;
	}

	@Override
	public StaticValueAccessor<T> getColumnIdentifier() {
		return columnIdentifier;
	}

	@Override
	public SessionFactoryDataHolder<V> getSessionFactoryData() {
		return sessionFactoryData;
	}

	@Override
	public V getArgument() {
		return argument;
	}

	@Override
	public Criteria getCriteria() {
		return criteria;
	}
}
