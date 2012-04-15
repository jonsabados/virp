package com.jshnd.virp;

public abstract class BaseValueAccessor<T> implements  ValueAccessor<T> {

	private Object meta;

	@Override
	public Object getMeta() {
		return meta;
	}

	@Override
	public void setMeta(Object meta) {
		this.meta = meta;
	}
}
