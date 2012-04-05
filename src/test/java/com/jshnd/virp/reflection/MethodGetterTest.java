package com.jshnd.virp.reflection;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class MethodGetterTest {

	@Test
	public void testGetColumnValue() throws SecurityException, NoSuchMethodException {
		Method method = SomeBean.class.getMethod("getSomeProperty");
		MethodGetter getter = new MethodGetter();
		getter.setGetterMethod(method);
		SomeBean bean = new SomeBean();
		bean.setSomeProperty("foo");
		assertEquals("foo", getter.getColumnValue(bean));
	}

}
