package com.jshnd.virp.reflection;

import com.jshnd.virp.BaseValueManipulator;
import com.jshnd.virp.exception.VirpException;

import java.lang.reflect.Field;

public class ReflectionFieldValueAccessor<T> extends BaseValueManipulator<T> {

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
	public void setValue(Object sourceObject, T value) {
		try {
			field.set(sourceObject, value);
		} catch(Exception e) {
			throw new VirpException(e);
		}
	}

	@Override
	public Class<T> getValueType() {
		return (Class<T>) field.getType();
	}
}
