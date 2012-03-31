package com.jshnd.casrom.reflection;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;


public class PropertyGetterTest {

	@Test
	public void testGetColumnValue() throws SecurityException, NoSuchFieldException {
		Field field = SomeBean.class.getDeclaredField("someProperty");
		field.setAccessible(true);
		PropertyGetter getter = new PropertyGetter();
		getter.setField(field);
		SomeBean bean = new SomeBean();
		bean.setSomeProperty("foo");
		assertEquals("foo", getter.getColumnValue(bean));
	}

}
