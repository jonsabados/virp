package com.jshnd.virp.hector;

import com.google.common.collect.Sets;
import com.jshnd.virp.BasicColumnAccessor;
import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.StaticValueAccessor;
import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.config.RowMapperMetaData;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class HectorActionFactoryTest {

	private HectorActionFactory testObj;

	@Before
	public void setup() {
		testObj = new HectorActionFactory();
	}

	@Test
	public void testStringSerializationRegistration() {
		RowMapperMetaData type = new RowMapperMetaData(Object.class);
		ValueAccessor<String> accessor1 = new StaticValueAccessor<String>("foo", String.class);
		ValueAccessor<String> accessor2 = new StaticValueAccessor<String>("foo", String.class);
		ValueAccessor<String> accessor3 = new StaticValueAccessor<String>("foo", String.class);
		type.setKeyValueAccessor(accessor1);
		BasicColumnAccessor<String, String> columnAccessor =
				new BasicColumnAccessor<String, String>(accessor2, accessor3);
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(StringSerializer.get(), accessor1.getMeta());
		assertSame(StringSerializer.get(), accessor2.getMeta());
		assertSame(StringSerializer.get(), accessor3.getMeta());
	}

	@Test
	public void testLongSerializationRegistration() {
		RowMapperMetaData type = new RowMapperMetaData(Object.class);
		ValueAccessor<Long> accessor1 = new StaticValueAccessor<Long>(Long.valueOf(1), Long.class);
		ValueAccessor<Long> accessor2 = new StaticValueAccessor<Long>(Long.valueOf(1), Long.class);
		ValueAccessor<Long> accessor3 = new StaticValueAccessor<Long>(Long.valueOf(1), Long.class);
		type.setKeyValueAccessor(accessor1);
		BasicColumnAccessor<Long, Long> columnAccessor =
				new BasicColumnAccessor<Long, Long>(accessor2, accessor3);
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(LongSerializer.get(), accessor1.getMeta());
		assertSame(LongSerializer.get(), accessor2.getMeta());
		assertSame(LongSerializer.get(), accessor3.getMeta());
	}

	@Test
	public void testPrimitiveLongSerializationRegistration() {
		RowMapperMetaData type = new RowMapperMetaData(Object.class);
		ValueAccessor<Long> accessor1 = new StaticValueAccessor<Long>(Long.valueOf(1), long.class);
		ValueAccessor<Long> accessor2 = new StaticValueAccessor<Long>(Long.valueOf(1), long.class);
		ValueAccessor<Long> accessor3 = new StaticValueAccessor<Long>(Long.valueOf(1), long.class);
		type.setKeyValueAccessor(accessor1);
		BasicColumnAccessor<Long, Long> columnAccessor =
				new BasicColumnAccessor<Long, Long>(accessor2, accessor3);
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(LongSerializer.get(), accessor1.getMeta());
		assertSame(LongSerializer.get(), accessor2.getMeta());
		assertSame(LongSerializer.get(), accessor3.getMeta());
	}

}
