package com.jshnd.virp.reflection;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class ReflectionMethodValueAccessorTest {

	@Test
	public void testGetColumnValue() throws SecurityException, NoSuchMethodException {
		Method method = SomeBean.class.getMethod("getSomeProperty");
		Method setter = SomeBean.class.getMethod("setSomeProperty", String.class);
		ReflectionMethodValueAccessor valueAccessorReflection = new ReflectionMethodValueAccessor(method, setter);
		SomeBean bean = new SomeBean();
		bean.setSomeProperty("foo");
		assertEquals("foo", valueAccessorReflection.getValue(bean));
		valueAccessorReflection.setValue(bean, "foo2");
		assertEquals("foo2", valueAccessorReflection.getValue(bean));
	}

}
