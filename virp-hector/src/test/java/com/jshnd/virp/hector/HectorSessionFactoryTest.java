package com.jshnd.virp.hector;

import com.google.common.collect.Sets;
import com.jshnd.virp.*;
import com.jshnd.virp.annotation.TimeToLive;
import com.jshnd.virp.config.RowMapperMetaData;

import me.prettyprint.cassandra.serializers.CharSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.ShortSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertSame;

public class HectorSessionFactoryTest {

	private HectorSessionFactory testObj;

	@Before
	public void setup() {
		testObj = new HectorSessionFactory();
	}

	private static class TestValueAccessor<T> extends BaseValueManipulator<T> {

		private Class<T> type;

		TestValueAccessor(Class<T> type) {
			this.type = type;
		}

		@Override
		public void setValue(Object sourceObject, T value) {

		}

		@Override
		public T getValue(Object sourceObject) {
			return null;
		}

		@Override
		public Class<T> getValueType() {
			return type;
		}

		@Override
		public Method getSetter() {
			return null;
		}
	}

	@Test
	public void testStringSerializationRegistration() {
		RowMapperMetaData<Object> type = new RowMapperMetaData<Object>(Object.class);
		TestValueAccessor<String> accessor1 = new TestValueAccessor<String>(String.class);
		StaticValueAccessor<String> accessor2 = new StaticValueAccessor<String>("foo", String.class);
		TestValueAccessor<String> accessor3 = new TestValueAccessor<String>(String.class);
		type.setKeyValueManipulator(accessor1);
		BasicColumnAccessor<String, String> columnAccessor =
				new BasicColumnAccessor<String, String>(accessor2, accessor3, new HardCodedValueAccessor<Integer>(TimeToLive.NONE));
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(StringSerializer.get(), accessor1.getSessionFactoryData());
		assertSame(StringSerializer.get(), accessor2.getSessionFactoryData());
		assertSame(StringSerializer.get(), accessor3.getSessionFactoryData());
	}

	@Test
	public void testLongSerializationRegistration() {
		RowMapperMetaData<Object> type = new RowMapperMetaData<Object>(Object.class);
		TestValueAccessor<Long> accessor1 = new TestValueAccessor<Long>( Long.class);
		StaticValueAccessor<Long> accessor2 = new StaticValueAccessor<Long>(Long.valueOf(1), Long.class);
		TestValueAccessor<Long> accessor3 = new TestValueAccessor<Long>(Long.class);
		type.setKeyValueManipulator(accessor1);
		BasicColumnAccessor<Long, Long> columnAccessor =
				new BasicColumnAccessor<Long, Long>(accessor2, accessor3, new HardCodedValueAccessor<Integer>(TimeToLive.NONE));
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(LongSerializer.get(), accessor1.getSessionFactoryData());
		assertSame(LongSerializer.get(), accessor2.getSessionFactoryData());
		assertSame(LongSerializer.get(), accessor3.getSessionFactoryData());
	}
	
	@Test
	public void testShortSerializationRegistration() {
		RowMapperMetaData<Object> type = new RowMapperMetaData<Object>(Object.class);
		TestValueAccessor<Short> accessor1 = new TestValueAccessor<Short>( Short.class);
		StaticValueAccessor<Short> accessor2 = new StaticValueAccessor<Short>(Short.valueOf((short) 1), Short.class);
		TestValueAccessor<Short> accessor3 = new TestValueAccessor<Short>(Short.class);
		type.setKeyValueManipulator(accessor1);
		BasicColumnAccessor<Short, Short> columnAccessor =
				new BasicColumnAccessor<Short, Short>(accessor2, accessor3, new HardCodedValueAccessor<Integer>(TimeToLive.NONE));
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(ShortSerializer.get(), accessor1.getSessionFactoryData());
		assertSame(ShortSerializer.get(), accessor2.getSessionFactoryData());
		assertSame(ShortSerializer.get(), accessor3.getSessionFactoryData());
	}

	@Test
	public void testCharSerializationRegistration() {
		RowMapperMetaData<Object> type = new RowMapperMetaData<Object>(Object.class);
		TestValueAccessor<Character> accessor1 = new TestValueAccessor<Character>(Character.class);
		StaticValueAccessor<Character> accessor2 = new StaticValueAccessor<Character>(Character.valueOf('c'), Character.class);
		TestValueAccessor<Character> accessor3 = new TestValueAccessor<Character>(Character.class);
		type.setKeyValueManipulator(accessor1);
		BasicColumnAccessor<Character, Character> columnAccessor =
				new BasicColumnAccessor<Character, Character>(accessor2, accessor3, new HardCodedValueAccessor<Integer>(TimeToLive.NONE));
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(CharSerializer.get(), accessor1.getSessionFactoryData());
		assertSame(CharSerializer.get(), accessor2.getSessionFactoryData());
		assertSame(CharSerializer.get(), accessor3.getSessionFactoryData());
	}
	
	@Test
	public void testPrimitiveLongSerializationRegistration() {
		RowMapperMetaData<Object> type = new RowMapperMetaData<Object>(Object.class);
		TestValueAccessor<Long> accessor1 = new TestValueAccessor<Long>(long.class);
		StaticValueAccessor<Long> accessor2 = new StaticValueAccessor<Long>(Long.valueOf(1), long.class);
		TestValueAccessor<Long> accessor3 = new TestValueAccessor<Long>(long.class);
		type.setKeyValueManipulator(accessor1);
		BasicColumnAccessor<Long, Long> columnAccessor =
				new BasicColumnAccessor<Long, Long>(accessor2, accessor3, new HardCodedValueAccessor<Integer>(TimeToLive.NONE));
		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
		testObj.setupClass(type);
		assertSame(LongSerializer.get(), accessor1.getSessionFactoryData());
		assertSame(LongSerializer.get(), accessor2.getSessionFactoryData());
		assertSame(LongSerializer.get(), accessor3.getSessionFactoryData());
	}

}
