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

import java.lang.reflect.Method;

public class TransformingValueManipulator<C, O> extends BaseValueManipulator<C> {

	private ValueManipulator<O> wrappedManipulator;

	private Transformer<C, O> transformer;

	public TransformingValueManipulator(ValueManipulator<O> wrappedManipulator,
										Transformer<C, O> transformer) {
		this.wrappedManipulator = wrappedManipulator;
		this.transformer = transformer;
	}

	@Override
	public void setValue(Object sourceObject, C value) {
		O transformed = transformer.valueForObject(value);
		wrappedManipulator.setValue(sourceObject, transformed);
	}

	@Override
	public Method getSetter() {
		return wrappedManipulator.getSetter();
	}

	@Override
	public C getValue(Object sourceObject) {
		O objectVal = wrappedManipulator.getValue(sourceObject);
		return transformer.valueForCassandra(objectVal);
	}

	@Override
	public Class<? extends C> getValueType() {
		return transformer.cassandraValueClass();
	}

}
