package com.jshnd.casrom.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.jshnd.casrom.ColumnGetter;
import com.jshnd.casrom.reflection.SomeBean;

public class AnnotationDrivenRowMapperMetaDataReaderTest {

	private AnnotationDrivenRowMapperMetaDataReader testObj;
	
	@Before
	public void setup() {
		testObj = new AnnotationDrivenRowMapperMetaDataReader();
	}
	
	@Test
	public void testReadClassMethodsOnly() {
		testObj.setReadProperties(false);
		RowMapperMetaData meta = testObj.readClass(SomeBean.class);
		assertEquals(SomeBean.class, meta.getRowMapperClass());
		Set<ColumnGetter> columnGetters = meta.getColumnGetters();
		assertEquals(1, columnGetters.size());
		ColumnGetter getter = Iterables.getFirst(columnGetters, null);
		assertNotNull(getter);
		assertEquals("bar", getter.getColumnName());
		
		SomeBean source = new SomeBean();
		source.setSomeProperty("notme");
		source.setMethodProperty("fooBar!");
		
		assertEquals("fooBar!", getter.getColumnValue(source));
	}
	
	@Test
	public void testReadClassPropertiesOnly() {
		testObj.setReadMethods(false);
		RowMapperMetaData meta = testObj.readClass(SomeBean.class);
		assertEquals(SomeBean.class, meta.getRowMapperClass());
		Set<ColumnGetter> columnGetters = meta.getColumnGetters();
		assertEquals(1, columnGetters.size());
		ColumnGetter getter = Iterables.getFirst(columnGetters, null);
		assertNotNull(getter);
		assertEquals("foo", getter.getColumnName());
		
		SomeBean source = new SomeBean();
		source.setMethodProperty("notme");
		source.setColumnProperty("fooBar!");
		
		assertEquals("fooBar!", getter.getColumnValue(source));
	}

}
