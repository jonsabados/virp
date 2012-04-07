package com.jshnd.virp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class TypeUtils {

	private static final Map<Class, ValueType> typeMap;

	static  {
		Map<Class, ValueType> tmp = new HashMap<Class, ValueType>();
		tmp.put(String.class, ValueType.STRING);
		tmp.put(Integer.class, ValueType.INT);
		tmp.put(int.class, ValueType.INT);
		typeMap = Collections.unmodifiableMap(tmp);
	}

	public static ValueType getType(Class theClass) {
		return typeMap.get(theClass);
	}

}
