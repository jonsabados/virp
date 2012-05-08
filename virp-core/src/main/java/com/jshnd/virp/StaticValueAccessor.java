package com.jshnd.virp;

public class StaticValueAccessor<T> implements ValueTypeHolder<T>, SessionFactoryDataHolder<T> {

	private T value;

	private Class<T> type;

	private Object sessionFactoryData;

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

	@Override
	public Object getSessionFactoryData() {
		return sessionFactoryData;
	}

	@Override
	public void setSessionFactoryData(Object sessionFactoryData) {
		this.sessionFactoryData = sessionFactoryData;
	}
}
