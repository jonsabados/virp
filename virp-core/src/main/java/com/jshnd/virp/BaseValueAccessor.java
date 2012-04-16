package com.jshnd.virp;

public abstract class BaseValueAccessor<T> implements  ValueAccessor<T> {

	private Object meta;

	@Override
	public Object getActionFactoryMeta() {
		return meta;
	}

	@Override
	public void setActionFactoryMeta(Object meta) {
		this.meta = meta;
	}
}
