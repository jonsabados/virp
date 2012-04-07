package com.jshnd.virp.reflection;

import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.ValueType;
import com.jshnd.virp.VirpException;

import java.lang.reflect.Field;

public class ReflectionFieldValueAccessor implements ValueAccessor {

	private Field field;

	private ValueType valueType;

	@Override
	public Object getValue(Object sourceObject) {
		try {
			return field.get(sourceObject);
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	public void setField(Field field) {
		this.field = field;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	@Override
	public ValueType getValueType() {
		return valueType;
	}
}
