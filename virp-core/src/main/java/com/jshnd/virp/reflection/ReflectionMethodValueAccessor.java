package com.jshnd.virp.reflection;

import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.ValueType;
import com.jshnd.virp.VirpException;

import java.lang.reflect.Method;

public class ReflectionMethodValueAccessor implements ValueAccessor {

	private Method getterMethod;

	private ValueType valueType;

	public Object getValue(Object sourceObject) {
		try {
			return getterMethod.invoke(sourceObject);
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	public void setGetterMethod(Method getterMethod) {
		this.getterMethod = getterMethod;
	}

	@Override
	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

}
