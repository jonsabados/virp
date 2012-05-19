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
import com.jshnd.virp.config.RowMapperMetaData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ByExampleQuery<T> implements Query<T> {

	private Set<QueryParameter<?, ?>> parameters = new HashSet<QueryParameter<?, ?>>();

	private RowMapperMetaData<T> meta;

	@SuppressWarnings("unchecked")
	public ByExampleQuery(RowMapperMetaData<T> classMeta, T example) {
		this.meta = classMeta;
		for(ColumnAccessor<?, ?> accessor : classMeta.getColumnAccessors()) {
			if(accessor.getValueManipulator().getValue(example) != null) {
				parameters.add(new ColumnAccessorQueryParameter(accessor, example, Criteria.EQUAL));
			}
		}
	}

	@Override
	public Collection<QueryParameter<?, ?>> getParameters() {
		return Collections.unmodifiableSet(parameters);
	}

	@Override
	public RowMapperMetaData<T> getMeta() {
		return meta;
	}
}
