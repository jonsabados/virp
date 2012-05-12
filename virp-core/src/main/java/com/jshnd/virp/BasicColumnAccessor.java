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

public class BasicColumnAccessor<T, V> implements ColumnAccessor<T, V> {

	private StaticValueAccessor<T> columnIdentifier;

	private ValueAccessor<Integer> timeToLive;

	private ValueManipulator<V> valueAccessor;

	public BasicColumnAccessor(StaticValueAccessor<T> columnIdentifier, ValueManipulator<V> valueAccessor,
							   ValueAccessor<Integer> timeToLive) {
		this.columnIdentifier = columnIdentifier;
		this.valueAccessor = valueAccessor;
		this.timeToLive = timeToLive;
	}

	@Override
	public StaticValueAccessor<T> getColumnIdentifier() {
		return columnIdentifier;
	}

	@Override
	public ValueAccessor<Integer> getTimeToLive() {
		return timeToLive;
	}

	@Override
	public ValueManipulator<V> getValueManipulator() {
		return valueAccessor;
	}

}
