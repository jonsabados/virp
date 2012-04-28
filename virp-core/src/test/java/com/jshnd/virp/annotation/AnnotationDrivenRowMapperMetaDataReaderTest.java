package com.jshnd.virp.annotation;

import com.google.common.collect.Iterables;
import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpAnnotationException;
import com.jshnd.virp.exception.VirpException;
import com.jshnd.virp.reflection.SomeBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("unused")
public class AnnotationDrivenRowMapperMetaDataReaderTest {

	private AnnotationDrivenRowMapperMetaDataReader testObj;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		testObj = new AnnotationDrivenRowMapperMetaDataReader();
	}

	@RowMapper(columnFamily = "dontCare")
	public static class BadKeyTester {

		@KeyColumn
		private String wrench;

		private String key;

		@KeyColumn
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getWrench() {
			return wrench;
		}

		public void setWrench(String wrench) {
			this.wrench = wrench;
		}
	}

	@Test
	public void testDuplicateKeyColumn() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage("Classes may only have a single key column");
		testObj.readClass(BadKeyTester.class);
	}

	@RowMapper(columnFamily = "dontCare")
	public static class OkPropertyKeyTester {

		@KeyColumn
		@NamedColumn(name = "foo")
		private String key;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}

	@Test
	public void testPropertyKeyKeyColumn() {
		RowMapperMetaData<OkPropertyKeyTester> meta = testObj.readClass(OkPropertyKeyTester.class);
		assertNotNull(meta.getKeyValueManipulator());
		OkPropertyKeyTester bean = new OkPropertyKeyTester();
		bean.key = "fooBar";
		assertEquals("fooBar", meta.getKeyValueManipulator().getValue(bean));
		assertEquals(String.class, meta.getKeyValueManipulator().getValueType());
		assertEquals(1, meta.getColumnAccessors().size());
		ColumnAccessor getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue());
		assertEquals("fooBar", getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	public static class NumberedColumnPropertyTester {

		@KeyColumn
		@NumberedColumn(number = 10)
		private String key;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}

	@Test
	public void testNumberedProperty() {
		RowMapperMetaData<NumberedColumnPropertyTester> meta = testObj.readClass(NumberedColumnPropertyTester.class);
		NumberedColumnPropertyTester bean = new NumberedColumnPropertyTester();
		bean.key = "fooBar";
		assertEquals(1, meta.getColumnAccessors().size());
		ColumnAccessor getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals(Long.valueOf(10), getter.getColumnIdentifier().getValue());
		assertEquals(Long.class, getter.getColumnIdentifier().getValueType());
		assertEquals("fooBar", getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	public static class NumberedColumnMethodTester {

		private String key;

		@KeyColumn
		@NumberedColumn(number = 10)
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
		assertEquals(Long.valueOf(10), getter.getColumnIdentifier().getValue());
		assertEquals(Long.class, getter.getColumnIdentifier().getValueType());
		assertEquals("fooBar", getter.getValueManipulator().getValue(bean));
	}


	@RowMapper(columnFamily = "dontCare")
	public static class OkMethodKeyTester {

		private String key;

		@KeyColumn
		@NamedColumn(name = "foo")
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
		assertNotNull(meta.getKeyValueManipulator());
		OkMethodKeyTester bean = new OkMethodKeyTester();
		bean.setKey("fooBar");
		assertEquals("fooBar", meta.getKeyValueManipulator().getValue(bean));
		assertEquals(String.class, meta.getKeyValueManipulator().getValueType());
		ColumnAccessor getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue());
		assertEquals("fooBar", getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "testIng")
	public static class ColumnFamilyTester {

		@KeyColumn
		private String key;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}
	}

	@Test
	public void testColumnFamily() {
		RowMapperMetaData meta = testObj.readClass(ColumnFamilyTester.class);
		assertEquals("testIng", meta.getColumnFamily());
	}

	public static class MissingAnnotationTester {

	}

	@Test
	public void testRowMapperMissing() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage(MissingAnnotationTester.class.getCanonicalName() +
				" missing required annotation " + RowMapper.class.getCanonicalName());
		testObj.readClass(MissingAnnotationTester.class);
	}

	@RowMapper(columnFamily = "SomeBean")
	public static class MethodOnlyReadingTester {

		private String someProperty;

		private String columnProperty;

		private String methodProperty;

		public String getSomeProperty() {
			return someProperty;
		}

		public void setSomeProperty(String someProperty) {
			this.someProperty = someProperty;
		}

		@KeyColumn
		@NamedColumn(name = "bar")
		public String getMethodProperty() {
			return methodProperty;
		}

		public void setMethodProperty(String methodProperty) {
			this.methodProperty = methodProperty;
		}

		public String getColumnProperty() {
			return columnProperty;
		}

		public void setColumnProperty(String columnProperty) {
			this.columnProperty = columnProperty;
		}

	}

	@Test
	public void testReadClassMethodsOnly() {
		testObj.setReadProperties(false);
		RowMapperMetaData meta = testObj.readClass(MethodOnlyReadingTester.class);
		assertEquals(MethodOnlyReadingTester.class, meta.getRowMapperClass());
		Set<ColumnAccessor<?,?>> valueAccessors = meta.getColumnAccessors();
		assertEquals(1, valueAccessors.size());
		ColumnAccessor getter = Iterables.getFirst(valueAccessors, null);
		assertNotNull(getter);

		MethodOnlyReadingTester source = new MethodOnlyReadingTester();
		source.setSomeProperty("notme");
		source.setMethodProperty("fooBar!");

		assertEquals("bar", getter.getColumnIdentifier().getValue());
		assertEquals(String.class, getter.getValueManipulator().getValueType());
		assertEquals("fooBar!", getter.getValueManipulator().getValue(source));
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

		assertEquals("foo", getter.getColumnIdentifier().getValue());
		assertEquals(String.class, getter.getValueManipulator().getValueType());
		assertEquals("fooBar!", getter.getValueManipulator().getValue(source));
	}

	@RowMapper(columnFamily = "foo")
	public static class MissingKeyColumnAnnotationTester {

		private String key;

		private String column;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		@NamedColumn(name = "foo")
		public String getColumn() {
			return column;
		}

		public void setColumn(String column) {
			this.column = column;
		}
	}

	@Test
	public void testMissingKeColumnAnnotation() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage(MissingKeyColumnAnnotationTester.class.getCanonicalName()
				+ " missing required annotation " + KeyColumn.class.getCanonicalName());
		testObj.readClass(MissingKeyColumnAnnotationTester.class);
	}

	@RowMapper(columnFamily = "foo")
	public static class GetterWithoutSetterMethodTester {

		private String key;

		private String column;

		@KeyColumn
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		@NamedColumn(name = "foo")
		public String getColumn() {
			return column;
		}
	}

	@Test
	public void testMissingGetterOnMethodAnnotation() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("setter for getter getColumn not found on class "
				+ GetterWithoutSetterMethodTester.class.getCanonicalName());
		testObj.readClass(GetterWithoutSetterMethodTester.class);
	}

	@RowMapper(columnFamily = "foo")
	public static class GetterWithoutAccessibleSetterMethodTester {

		private String key;

		private String column;

		@KeyColumn
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		@NamedColumn(name = "foo")
		public String getColumn() {
			return column;
		}

		private void setColumn(String column) {
			this.column = column;
		}

	}

	@Test
	public void testInacessibleGetterOnMethodAnnotation() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("setter for getter getColumn not found on class "
				+ GetterWithoutAccessibleSetterMethodTester.class.getCanonicalName());
		testObj.readClass(GetterWithoutAccessibleSetterMethodTester.class);
	}

	@RowMapper(columnFamily = "foo")
	public static class PropertyWithoutAccessibleGetterMethodTester {

		@KeyColumn
		private String key;

		@NamedColumn(name = "foo")
		private String column;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public void setColumn(String column) {
			this.column = column;
		}

	}

	@Test
	public void testMissingGetterOnFieldAnnotation() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("Getter for field column not found");
		testObj.readClass(PropertyWithoutAccessibleGetterMethodTester.class);
	}

	@RowMapper(columnFamily = "foo")
	public static class PropertyWithoutAccessibleSetterMethodTester {

		@KeyColumn
		private String key;

		@NamedColumn(name = "foo")
		private String column;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getColumn() {
			return column;
		}

	}

	@Test
	public void testMissingSetterOnFieldAnnotation() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("Setter for field column not found");
		testObj.readClass(PropertyWithoutAccessibleSetterMethodTester.class);
	}

}
