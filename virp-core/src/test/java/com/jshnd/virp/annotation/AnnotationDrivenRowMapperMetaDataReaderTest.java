package com.jshnd.virp.annotation;

import com.google.common.collect.Iterables;
import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.StaticValueAccessor;
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
import static org.junit.Assert.assertTrue;

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

		@Key
		private String wrench;

		private String key;

		@Key
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
	public static class MissingDynamicTtlTester {

		@Key
		@HasDynamicTimeToLive(identifier = "bar")
		private String pipe;

		@DynamicTimeToLive(forIdentifier = "notbar")
		private int notAWrench;

		public String getPipe() {
			return pipe;
		}

		public void setPipe(String pipe) {
			this.pipe = pipe;
		}

		public int getNotAWrench() {
			return notAWrench;
		}

		public void setNotAWrench(int notAWrench) {
			this.notAWrench = notAWrench;
		}
	}

	@Test
	public void testMissingDynamicTtlColumn() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("Dynamic ttl for marked property: bar not found");
		testObj.readClass(MissingDynamicTtlTester.class);
	}

	@RowMapper(columnFamily = "dontCare")
	public static class StaticAndDynamicTtlTester {

		@Key
		@HasDynamicTimeToLive(identifier = "bar")
		@TimeToLive(seconds = 30)
		private String pipe;

		@DynamicTimeToLive(forIdentifier = "bar")
		private int wrench;

		public String getPipe() {
			return pipe;
		}

		public void setPipe(String pipe) {
			this.pipe = pipe;
		}

		public int getWrench() {
			return wrench;
		}

		public void setWrench(int wrench) {
			this.wrench = wrench;
		}
	}

	@Test
	public void testStaticAndDynamicTtl() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("Fields may only have static or dynamic ttl's - not both");
		testObj.readClass(StaticAndDynamicTtlTester.class);
	}

	@RowMapper(columnFamily = "dontCare")
	public static class MultipleDynamicTtlTester {

		@Key
		@HasDynamicTimeToLive(identifier = "bar")
		private String pipe;

		@DynamicTimeToLive(forIdentifier = "bar")
		private int wrench;

		@DynamicTimeToLive(forIdentifier = "bar")
		private Integer otherWrench;

		public String getPipe() {
			return pipe;
		}

		public void setPipe(String pipe) {
			this.pipe = pipe;
		}

		public int getWrench() {
			return wrench;
		}

		public void setWrench(int wrench) {
			this.wrench = wrench;
		}

		public Integer getOtherWrench() {
			return otherWrench;
		}

		public void setOtherWrench(Integer otherWrench) {
			this.otherWrench = otherWrench;
		}
	}

	@Test
	public void testMultipleDynamicTtls() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("Columns may only have one source for ttl's");
		testObj.readClass(MultipleDynamicTtlTester.class);
	}

	@RowMapper(columnFamily = "dontCare")
	public static class NonIntDynamicTtlTester {

		@Key
		@HasDynamicTimeToLive(identifier = "bar")
		private String pipe;

		@DynamicTimeToLive(forIdentifier = "bar")
		private String wrench;

		public String getPipe() {
			return pipe;
		}

		public void setPipe(String pipe) {
			this.pipe = pipe;
		}

		public String getWrench() {
			return wrench;
		}

		public void setWrench(String wrench) {
			this.wrench = wrench;
		}
	}

	@Test
	public void testNonIntDynamicTtl() {
		expectedException.expect(VirpAnnotationException.class);
		expectedException.expectMessage("DynamicTimeToLive members must be of Integer type");
		testObj.readClass(NonIntDynamicTtlTester.class);
	}

	@RowMapper(columnFamily = "dontCare", defaultTimeToLive = @TimeToLive(seconds = 10))
	public static class TtlTester {

		@Key
		@NamedColumn(name = "foo")
		@TimeToLive(seconds = 20)
		private String key;

		@NamedColumn(name = "bar")
		@HasDynamicTimeToLive(identifier = "bar")
		private String otherColumn;

		@NamedColumn(name = "bob")
		private String defaultedColumn;

		@DynamicTimeToLive(forIdentifier = "bar")
		private int barsTtl;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getOtherColumn() {
			return otherColumn;
		}

		public void setOtherColumn(String otherColumn) {
			this.otherColumn = otherColumn;
		}

		public int getBarsTtl() {
			return barsTtl;
		}

		public void setBarsTtl(int barsTtl) {
			this.barsTtl = barsTtl;
		}

		public String getDefaultedColumn() {
			return defaultedColumn;
		}

		public void setDefaultedColumn(String defaultedColumn) {
			this.defaultedColumn = defaultedColumn;
		}
	}

	@Test
	public void testTtls() {
		RowMapperMetaData<TtlTester> meta = testObj.readClass(TtlTester.class);
		TtlTester tester = new TtlTester();
		tester.setKey("key");
		tester.setOtherColumn("bar");
		tester.setDefaultedColumn("default");
		tester.setBarsTtl(25);
		boolean keyHit = false;
		boolean barHit = false;
		boolean defaultHit = false;
		assertEquals(3, meta.getColumnAccessors().size());
		for(ColumnAccessor<?, ?> accessor : meta.getColumnAccessors()) {
			Object value = accessor.getValueManipulator().getValue(tester);
			if("key".equals(value)) {
				assertEquals(Integer.valueOf(20), accessor.getTimeToLive().getValue(tester));
				keyHit = true;
			} else if("bar".equals(value)) {
				assertEquals(Integer.valueOf(25), accessor.getTimeToLive().getValue(tester));
				barHit = true;
			} else if("default".equals(value)) {
				assertEquals(Integer.valueOf(10), accessor.getTimeToLive().getValue(tester));
				defaultHit = true;
			}
		}
		assertTrue(keyHit);
		assertTrue(barHit);
		assertTrue(defaultHit);
	}

	@RowMapper(columnFamily = "dontCare")
	public static class OkPropertyKeyTester {

		@Key
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
		ColumnAccessor<?, ?> getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue());
		assertEquals("fooBar", getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	public static class NumberedColumnPropertyTester {

		@Key
		private String key;

		@NumberedColumnShort(number = 0)
		private String shortValue;

		@NumberedColumnInt(number = 1)
		private String intValue;

		@NumberedColumnLong(number = 2)
		private String longValue;

		@NumberedColumnFloat(number = 3.0f)
		private String floatValue;

		@NumberedColumnDouble(number = 4.0d)
		private String doubleValue;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getShortValue() {
			return shortValue;
		}

		public void setShortValue(String shortValue) {
			this.shortValue = shortValue;
		}

		public String getIntValue() {
			return intValue;
		}

		public void setIntValue(String intValue) {
			this.intValue = intValue;
		}

		public String getLongValue() {
			return longValue;
		}

		public void setLongValue(String longValue) {
			this.longValue = longValue;
		}

		public String getFloatValue() {
			return floatValue;
		}

		public void setFloatValue(String floatValue) {
			this.floatValue = floatValue;
		}

		public String getDoubleValue() {
			return doubleValue;
		}

		public void setDoubleValue(String doubleValue) {
			this.doubleValue = doubleValue;
		}
	}

	@Test
	public void testNumberedProperty() {
		RowMapperMetaData<NumberedColumnPropertyTester> meta = testObj.readClass(NumberedColumnPropertyTester.class);
		NumberedColumnPropertyTester bean = new NumberedColumnPropertyTester();
		bean.key = "fooBar";
		assertEquals(5, meta.getColumnAccessors().size());
		Class<?>[] expected = new Class<?>[] {Short.class, Integer.class, Long.class, Float.class, Double.class};
		int hitCount = 0;
		for(ColumnAccessor<?, ?> accessor : meta.getColumnAccessors()) {
			StaticValueAccessor<?> identifier = accessor.getColumnIdentifier();
			Class<?> type = identifier.getValueType();
			for(int i = 0; i < expected.length; i++) {
				if(type.equals(expected[i])) {
					hitCount++;
					assertEquals(i, ((Number)identifier.getValue()).intValue());
				}
			}
		}
	}

	@RowMapper(columnFamily = "dontCare")
	public static class NumberedColumnMethodTester {

		private String key;

		@Key
		@NumberedColumnLong(number = 10)
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
		ColumnAccessor<?, ?> getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals(Long.valueOf(10), getter.getColumnIdentifier().getValue());
		assertEquals(Long.class, getter.getColumnIdentifier().getValueType());
		assertEquals("fooBar", getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	public static class BooleanColumnMethodTester {

		private boolean key;

		@Key
		@NamedColumn(name = "foo")
		public boolean isKey() {
			return key;
		}

		public void setKey(boolean key) {
			this.key = key;
		}
	}

	@Test
	public void testBooleanMethod() {
		RowMapperMetaData<BooleanColumnMethodTester> meta = testObj.readClass(BooleanColumnMethodTester.class);
		BooleanColumnMethodTester bean = new BooleanColumnMethodTester();
		bean.setKey(true);
		assertEquals(1, meta.getColumnAccessors().size());
		ColumnAccessor<?, ?> getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue());
		assertEquals(String.class, getter.getColumnIdentifier().getValueType());
		assertEquals(Boolean.TRUE, getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	public static class BooleanColumnPropertyTester {

		@Key
		@NamedColumn(name = "foo")
		private boolean key;

		public boolean isKey() {
			return key;
		}

		public void setKey(boolean key) {
			this.key = key;
		}
	}

	@Test
	public void testBooleanProperty() {
		RowMapperMetaData<BooleanColumnPropertyTester> meta = testObj.readClass(BooleanColumnPropertyTester.class);
		BooleanColumnPropertyTester bean = new BooleanColumnPropertyTester();
		bean.setKey(true);
		assertEquals(1, meta.getColumnAccessors().size());
		ColumnAccessor<?, ?> getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue());
		assertEquals(String.class, getter.getColumnIdentifier().getValueType());
		assertEquals(Boolean.TRUE, getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "dontCare")
	public static class OkMethodKeyTester {

		private String key;

		@Key
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
		ColumnAccessor<?, ?> getter = Iterables.getFirst(meta.getColumnAccessors(), null);
		assertEquals("foo", getter.getColumnIdentifier().getValue());
		assertEquals("fooBar", getter.getValueManipulator().getValue(bean));
	}

	@RowMapper(columnFamily = "testIng")
	public static class ColumnFamilyTester {

		@Key
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
		RowMapperMetaData<ColumnFamilyTester> meta = testObj.readClass(ColumnFamilyTester.class);
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

		@Key
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
		RowMapperMetaData<MethodOnlyReadingTester> meta = testObj.readClass(MethodOnlyReadingTester.class);
		assertEquals(MethodOnlyReadingTester.class, meta.getRowMapperClass());
		Set<ColumnAccessor<?,?>> valueAccessors = meta.getColumnAccessors();
		assertEquals(1, valueAccessors.size());
		ColumnAccessor<?, ?> getter = Iterables.getFirst(valueAccessors, null);
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
		RowMapperMetaData<SomeBean> meta = testObj.readClass(SomeBean.class);
		assertEquals(SomeBean.class, meta.getRowMapperClass());
		Set<ColumnAccessor<?, ?>> valueAccessors = meta.getColumnAccessors();
		assertEquals(1, valueAccessors.size());
		ColumnAccessor<?, ?> getter = Iterables.getFirst(valueAccessors, null);
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
				+ " missing required annotation " + Key.class.getCanonicalName());
		testObj.readClass(MissingKeyColumnAnnotationTester.class);
	}

	@RowMapper(columnFamily = "foo")
	public static class GetterWithoutSetterMethodTester {

		private String key;

		private String column;

		@Key
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
		expectedException.expectMessage("Setter for field column not found");
		testObj.readClass(GetterWithoutSetterMethodTester.class);
	}

	@RowMapper(columnFamily = "foo")
	public static class GetterWithoutAccessibleSetterMethodTester {

		private String key;

		private String column;

		@Key
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
		expectedException.expectMessage("Setter for field column not found");
		testObj.readClass(GetterWithoutAccessibleSetterMethodTester.class);
	}

	@RowMapper(columnFamily = "foo")
	public static class PropertyWithoutAccessibleGetterMethodTester {

		@Key
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

		@Key
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
