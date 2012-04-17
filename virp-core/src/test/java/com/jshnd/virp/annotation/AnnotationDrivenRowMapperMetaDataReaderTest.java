package com.jshnd.virp.annotation;

import com.google.common.collect.Iterables;
import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.exception.VirpAnnotationException;
import com.jshnd.virp.exception.VirpException;
import com.jshnd.virp.config.RowMapperMetaData;
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

	@Before
	public void setup() {
		testObj = new AnnotationDrivenRowMapperMetaDataReader();
	}

	@RowMapper(columnFamily = "dontCare")
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

	@Test
	public void testDuplicateKeyColumn() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage("Classes may only have a single key column");
		testObj.readClass(BadKeyTester.class);
	}

	@RowMapper(columnFamily = "dontCare")
	private static class OkPropertyKeyTester {

		@KeyColumn
		@NamedColumn(name = "foo")
		@SuppressWarnings("unused") // reflection
		private String key;

	}

	@Test
	public void testPropertyKeyKeyColumn() {
		RowMapperMetaData<OkPropertyKeyTester> meta = testObj.readClass(OkPropertyKeyTester.class);
		assertNotNull(meta.getKeyValueAccessor());
		OkPropertyKeyTester bean = new OkPropertyKeyTester();
		bean.key = "fooBar";
		assertEquals("fooBar", meta.getKeyValueAccessor().getValue(bean));
		assertEquals(String.class, meta.getKeyValueAccessor().getValueType());
		assertEquals(1, meta.getColumnAccessors().size());
		ColumnAccessor getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue(bean));
		assertEquals("fooBar", getter.getValueAccessor().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	private static class NumberedColumnPropertyTester {

		@NumberedColumn(number = 10)
		@SuppressWarnings("unused") // reflection
		private String key;

	}

	@Test
	public void testNumberedProperty() {
		RowMapperMetaData<NumberedColumnPropertyTester> meta = testObj.readClass(NumberedColumnPropertyTester.class);
		NumberedColumnPropertyTester bean = new NumberedColumnPropertyTester();
		bean.key = "fooBar";
		assertEquals(1, meta.getColumnAccessors().size());
		ColumnAccessor getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals(Long.valueOf(10), getter.getColumnIdentifier().getValue(bean));
		assertEquals(Long.class, getter.getColumnIdentifier().getValueType());
		assertEquals("fooBar", getter.getValueAccessor().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	private static class NumberedColumnMethodTester {

		private String key;

		@NumberedColumn(number = 10)
		@SuppressWarnings("unused") // reflection
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}

	@Test
	public void testNumberedMethod() {
		RowMapperMetaData<NumberedColumnMethodTester> meta = testObj.readClass(NumberedColumnMethodTester.class);
		NumberedColumnMethodTester bean = new NumberedColumnMethodTester();
		bean.setKey("fooBar");
		assertEquals(1, meta.getColumnAccessors().size());
		ColumnAccessor getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals(Long.valueOf(10), getter.getColumnIdentifier().getValue(bean));
		assertEquals(Long.class, getter.getColumnIdentifier().getValueType());
		assertEquals("fooBar", getter.getValueAccessor().getValue(bean));
	}


	@RowMapper(columnFamily = "dontCare")
	private static class OkMethodKeyTester {

		private String key;

		@KeyColumn
		@NamedColumn(name = "foo")
		@SuppressWarnings("unused")  // reflection
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}

	@Test
	public void testMethodKeyKeyColumn() {
		RowMapperMetaData<OkMethodKeyTester> meta = testObj.readClass(OkMethodKeyTester.class);
		assertNotNull(meta.getKeyValueAccessor());
		OkMethodKeyTester bean = new OkMethodKeyTester();
		bean.setKey("fooBar");
		assertEquals("fooBar", meta.getKeyValueAccessor().getValue(bean));
		assertEquals(String.class, meta.getKeyValueAccessor().getValueType());
		ColumnAccessor getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue(bean));
		assertEquals("fooBar", getter.getValueAccessor().getValue(bean));
	}

	@RowMapper(columnFamily = "testIng")
	public static class ColumnFamilyTester {

	}

	@Test
	public void testColumnFamily() {
		RowMapperMetaData meta = testObj.readClass(ColumnFamilyTester.class);
		assertEquals("testIng", meta.getColumnFamily());
	}

	public static class MissingAnnotationTester {

	}

	@Test
	public void testColumnFamilyMissing() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage(MissingAnnotationTester.class.getCanonicalName() +
				" missing required annotation: " + RowMapper.class.getCanonicalName());
		testObj.readClass(MissingAnnotationTester.class);
	}

	@Test
	public void testReadClassMethodsOnly() {
		testObj.setReadProperties(false);
		RowMapperMetaData meta = testObj.readClass(SomeBean.class);
		assertEquals(SomeBean.class, meta.getRowMapperClass());
		Set<ColumnAccessor<?,?>> valueAccessors = meta.getColumnAccessors();
		assertEquals(1, valueAccessors.size());
		ColumnAccessor getter = Iterables.getFirst(valueAccessors, null);
		assertNotNull(getter);

		SomeBean source = new SomeBean();
		source.setSomeProperty("notme");
		source.setMethodProperty("fooBar!");

		assertEquals("bar", getter.getColumnIdentifier().getValue(source));
		assertEquals(String.class, getter.getValueAccessor().getValueType());
		assertEquals("fooBar!", getter.getValueAccessor().getValue(source));
	}

	@Test
	public void testReadClassPropertiesOnly() {
		testObj.setReadMethods(false);
		RowMapperMetaData meta = testObj.readClass(SomeBean.class);
		assertEquals(SomeBean.class, meta.getRowMapperClass());
		Set<ColumnAccessor<?, ?>> valueAccessors = meta.getColumnAccessors();
		assertEquals(1, valueAccessors.size());
		ColumnAccessor getter = Iterables.getFirst(valueAccessors, null);
		assertNotNull(getter);

		SomeBean source = new SomeBean();
		source.setMethodProperty("notme");
		source.setColumnProperty("fooBar!");

		assertEquals("foo", getter.getColumnIdentifier().getValue(source));
		assertEquals(String.class, getter.getValueAccessor().getValueType());
		assertEquals("fooBar!", getter.getValueAccessor().getValue(source));
	}

}
