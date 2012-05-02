package com.jshnd.virp;

import java.lang.reflect.Method;

public interface ValueManipulator<T> extends ValueAccessor<T> {

	public void setValue(Object sourceObject, T value);

	public Method getSetter();

}
