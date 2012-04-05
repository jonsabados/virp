package com.jshnd.virp.config;

import com.google.common.collect.Iterables;
import com.jshnd.virp.ColumnGetter;
import com.jshnd.virp.VirpException;
import com.jshnd.virp.annotation.Column;
import com.jshnd.virp.annotation.KeyColumn;
import com.jshnd.virp.reflection.SomeBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnnotationDrivenRowMapperMetaDataReaderTest {

	private AnnotationDrivenRowMapperMetaDataReader testObj;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static class OkPropertyKeyTester {

		@KeyColumn
		@Column(name = "foo")
		@SuppressWarnings("unused") // reflection
		private String key;

	}

	private static class OkMethodKeyTester {

		private String key;

		@KeyColumn
		@Column(name = "foo")
		@SuppressWarnings("unused")  // reflection
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}

	private static class BadKeyTester {

		@KeyColumn
		@SuppressWarnings("unused")  // reflection
		private String wrench;

		@SuppressWarnings("unused") // meh.
		private String key;

		@KeyColumn
		@SuppressWarnings("unused")  // reflection
		public String getKey() {
			return key;
		}

	}

	@Before
	public void setup() {
		testObj = new AnnotationDrivenRowMapperMetaDataReader();
	}

	@Test
	public void testDuplicateKeyColumn() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage("Classes may only have a single key column");
		testObj.readClass(BadKeyTester.class);
	}

	@Test
	public void testPropertyKeyKeyColumn() {
		RowMapperMetaData meta = testObj.readClass(OkPropertyKeyTester.class);
		assertNotNull(meta.getKeyColumnGetter());
		OkPropertyKeyTester bean = new OkPropertyKeyTester();
		bean.key = "fooBar";
		assertEquals("fooBar", meta.getKeyColumnGetter().getColumnValue(bean));
		assertEquals(1, meta.getColumnGetters().size());
		ColumnGetter getter = Iterables.getFirst(meta.getColumnGetters(), null);
		assertEquals("foo", getter.getColumnName());
		assertEquals("fooBar", getter.getColumnValue(bean));
	}

	@Test
	public void testMethodKeyKeyColumn() {
		RowMapperMetaData meta = testObj.readClass(OkMethodKeyTester.class);
		assertNotNull(meta.getKeyColumnGetter());
		OkMethodKeyTester bean = new OkMethodKeyTester();
		bean.setKey("fooBar");
		assertEquals("fooBar", meta.getKeyColumnGetter().getColumnValue(bean));
		ColumnGetter getter = Iterables.getFirst(meta.getColumnGetters(), null);
		assertEquals("foo", getter.getColumnName());
		assertEquals("fooBar", getter.getColumnValue(bean));
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
