package com.jshnd.casrom.reflection;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

import com.jshnd.casrom.annotation.SomeBean;
import com.jshnd.casrom.reflection.MethodGetter;

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
