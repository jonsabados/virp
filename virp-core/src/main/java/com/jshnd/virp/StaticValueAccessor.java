package com.jshnd.virp;

public class StaticValueAccessor<T> extends BaseValueAccessor<T> implements ValueTypeHolder<T> {

	private T value;

	private Class<T> type;

	public StaticValueAccessor(T value, Class<T> type) {
		this.value = value;
		this.type = type;
	}

	public T getValue() {
		return value;
	}

	public Class<T> getValueType() {
		return type;
	}

}
