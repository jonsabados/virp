package com.jshnd.virp;

public class StaticValueAccessor<T> extends BaseValueAccessor<T> {

	private T value;

	private Class<T> type;

	public StaticValueAccessor(T value, Class<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public T getValue(Object sourceObject) {
		return value;
	}

	@Override
	public Class<T> getValueType() {
		return type;
	}
}
