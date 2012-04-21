package com.jshnd.virp;

public abstract class BaseValueAccessor<T> implements  SessionFactoryDataHolder<T> {

	private Object sessionFactoryData;

	@Override
	public Object getSessionFactoryData() {
		return sessionFactoryData;
	}

	@Override
	public void setSessionFactoryData(Object sessionFactoryData) {
		this.sessionFactoryData = sessionFactoryData;
	}
}
