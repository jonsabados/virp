package com.jshnd.virp.reflection;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;


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
