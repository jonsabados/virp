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
