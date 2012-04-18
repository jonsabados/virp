package com.jshnd.virp;

public interface ValueManipulator<T> extends ValueAccessor<T> {

	public void setValue(Object sourceObject, T value);

}
