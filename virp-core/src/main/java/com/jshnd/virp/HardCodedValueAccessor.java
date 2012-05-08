package com.jshnd.virp;

public class HardCodedValueAccessor<T> extends BaseValueAccessor<T> {

	private T value;

	public HardCodedValueAccessor(T value) {
		this.value = value;
	}

	@Override
	public T getValue(Object sourceObject) {
		return value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getValueType() {
		return (Class<T>) value.getClass();
	}


}
