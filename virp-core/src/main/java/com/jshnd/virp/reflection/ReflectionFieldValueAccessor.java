package com.jshnd.virp.reflection;

import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.VirpException;

import java.lang.reflect.Field;

public class ReflectionFieldValueAccessor<T> implements ValueAccessor<T> {

	private Field field;

	public ReflectionFieldValueAccessor(Field field) {
		this.field = field;
	}

	@Override
	public T getValue(Object sourceObject) {
		try {
			return (T) field.get(sourceObject);
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	@Override
	public Class<T> getValueType() {
		return (Class<T>) field.getType();
	}
}
