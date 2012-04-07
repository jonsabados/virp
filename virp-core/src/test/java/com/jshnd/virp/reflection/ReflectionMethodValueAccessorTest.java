package com.jshnd.virp.reflection;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class ReflectionMethodValueAccessorTest {

	@Test
	public void testGetColumnValue() throws SecurityException, NoSuchMethodException {
		Method method = SomeBean.class.getMethod("getSomeProperty");
		ReflectionMethodValueAccessor valueAccessorReflection = new ReflectionMethodValueAccessor();
		valueAccessorReflection.setGetterMethod(method);
		SomeBean bean = new SomeBean();
		bean.setSomeProperty("foo");
		assertEquals("foo", valueAccessorReflection.getValue(bean));
	}

}
