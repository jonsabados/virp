package com.jshnd.virp.hector;

import com.google.common.collect.Sets;
import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.VirpSessionSpec;
import com.jshnd.virp.annotation.*;
import com.jshnd.virp.config.ConfiguredRowMapperSource;
import com.jshnd.virp.config.NullColumnSaveBehavior;
import com.jshnd.virp.config.SessionAttachmentMode;
import com.jshnd.virp.query.Query;
import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.BasicKeyspaceDefinition;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import me.prettyprint.hector.testutils.EmbeddedServerHelper;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;
import static org.junit.Assert.*;

public class VirpHectorITCase {

	private static Keyspace testKeyspace;

	private static Cluster cluster;

	private VirpConfig config;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@BeforeClass
	public static void startupEmbeddedCluster() throws Exception {
		EmbeddedServerHelper helper = new EmbeddedServerHelper();
		helper.setup();
		cluster = getOrCreateCluster("Test Cluster", "127.0.0.1:9170");
		BasicKeyspaceDefinition definition = new BasicKeyspaceDefinition();
		definition.setName("TEST");
		definition.setStrategyClass("org.apache.cassandra.locator.SimpleStrategy");
		definition.setReplicationFactor(1);
		cluster.addKeyspace(definition);
		testKeyspace = createKeyspace("TEST", cluster);
		ColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
		columnFamilyDefinition.setName("BasicTestObject");
		columnFamilyDefinition.setKeyspaceName(testKeyspace.getKeyspaceName());
		cluster.addColumnFamily(columnFamilyDefinition);
		ColumnFamilyDefinition indexed = new BasicColumnFamilyDefinition();
		indexed.setName("HasSecondaryIndex");
		indexed.setKeyspaceName(testKeyspace.getKeyspaceName());
		BasicColumnDefinition indexedColumn = new BasicColumnDefinition();
		indexedColumn.setIndexName("idx_indexed");
		indexedColumn.setIndexType(ColumnIndexType.KEYS);
		indexedColumn.setName(ByteBuffer.wrap("indexed".getBytes()));
		indexedColumn.setValidationClass("UTF8Type");
		indexed.addColumnDefinition(indexedColumn);
		cluster.addColumnFamily(indexed);
	}

	@AfterClass
	public static void cleanup() throws Exception {
		EmbeddedServerHelper.cleanup();
	}

	@Before
	public void setup() {
		cluster.truncate("TEST", "BasicTestObject");
		cluster.truncate("TEST", "HasSecondaryIndex");
		config = new VirpConfig();
		config.setMetaDataReader(new AnnotationDrivenRowMapperMetaDataReader());
		ConfiguredRowMapperSource source = new ConfiguredRowMapperSource();
		source.setRowMapperClasses(Sets.<Class<?>>newHashSet(BasicSaveObject.class, SecondaryIndexObject.class));
		config.setRowMapperSource(source);
		HectorSessionFactory actionFactory = new HectorSessionFactory();
		actionFactory.setKeyspace(testKeyspace);
		config.setSessionFactory(actionFactory);
		config.setDefaultSessionAttachmentMode(SessionAttachmentMode.AUTO_FLUSH);
		config.init();
	}

	@RowMapper(columnFamily = "HasSecondaryIndex")
	public static class	SecondaryIndexObject {

		@Key
		private String key;

		@NamedColumn(name = "indexed")
		private String indexed;

		@NumberedColumnLong(number = 10)
		private Long unindexed;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getIndexed() {
			return indexed;
		}

		public void setIndexed(String indexed) {
			this.indexed = indexed;
		}

		public Long getUnindexed() {
			return unindexed;
		}

		public void setUnindexed(Long unindexed) {
			this.unindexed = unindexed;
		}
	}

	@RowMapper(columnFamily = "BasicTestObject")
	public static class BasicSaveObject {

		@Key
		private String key;

		@NamedColumn(name = "columnOne")
		@TimeToLive(seconds = 20)
		private String columnOne;

		@NamedColumn(name = "columnTwo")
		private String columnTwo;

		@NumberedColumnLong(number = 10)
		private Long columnTen = Long.valueOf(0);

		@NamedColumn(name = "bigDecimal")
		private BigDecimal bigDecimal;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getColumnOne() {
			return columnOne;
		}

		public void setColumnOne(String columnOne) {
			this.columnOne = columnOne;
		}

		public String getColumnTwo() {
			return columnTwo;
		}

		public void setColumnTwo(String columnTwo) {
			this.columnTwo = columnTwo;
		}

		public Long getColumnTen() {
			return columnTen;
		}

		public void setColumnTen(Long columnTen) {
			this.columnTen = columnTen;
		}

		public BigDecimal getBigDecimal() {
			return bigDecimal;
		}

		public void setBigDecimal(BigDecimal bigDecimal) {
			this.bigDecimal = bigDecimal;
		}
	}

	@Test
	public void testBasicSave() {
		BasicSaveObject row = new BasicSaveObject();
		row.setKey("save");
		row.setColumnOne("valueForColumnOne");
		row.setColumnTwo("valueForColumnTwo");
		row.setColumnTen(Long.valueOf(20));
		row.setBigDecimal(new BigDecimal("10.001"));

		VirpSession session = config.newSession();
		session.save(row);
		// should not save until flush
		verifyBasicSaveObject("save", null, null, null, null);
		session.close();

		verifyBasicSaveObject("save", "valueForColumnOne", "valueForColumnTwo", Long.valueOf(20),
				new BigDecimal("10.001"));
	}
	
	@Test
	public void testSaveWithNullColumnDoNothing() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "save", "someValue", "priorValue", Long.valueOf(21), new BigDecimal("10.00"));
		mutator.execute();
		
		BasicSaveObject row = new BasicSaveObject();
		row.setKey("save");
		row.setColumnOne("valueForColumnOne");
		row.setColumnTwo(null);
		row.setColumnTen(Long.valueOf(20));

		VirpSessionSpec spec =
				new VirpSessionSpec(config).withNullColumnSaveBehavior(NullColumnSaveBehavior.DO_NOTHING);
		VirpSession session = config.newSession(spec);
		session.save(row);
		session.close();

		verifyBasicSaveObject("save", "valueForColumnOne", "priorValue", Long.valueOf(20), new BigDecimal("10.00"));
	}

	@Test
	public void testSaveWithNullNoColumn() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "save", "someValue", "priorValue", Long.valueOf(21), BigDecimal.ONE);
		mutator.execute();

		BasicSaveObject row = new BasicSaveObject();
		row.setKey("save");
		row.setColumnOne("valueForColumnOne");
		row.setColumnTwo(null);
		row.setColumnTen(Long.valueOf(20));

		VirpSessionSpec spec =
				new VirpSessionSpec(config).withNullColumnSaveBehavior(NullColumnSaveBehavior.NO_COLUMN);
		VirpSession session = config.newSession(spec);
		session.save(row);
		session.close();

		verifyBasicSaveObject("save", "valueForColumnOne", null, Long.valueOf(20), null);
	}
	
	@Test
	public void testSaveWithWithEmptyByteArray() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "save", "someValue", "priorValue", Long.valueOf(21), BigDecimal.ONE);
		mutator.execute();
		
		BasicSaveObject row = new BasicSaveObject();
		row.setKey("save");
		row.setColumnOne("valueForColumnOne");
		row.setColumnTwo(null);
		row.setColumnTen(null);

		VirpSessionSpec spec =
				new VirpSessionSpec(config).withNullColumnSaveBehavior(NullColumnSaveBehavior.EMPTY_BYTE_ARRAY);
		VirpSession session = config.newSession(spec);
		session.save(row);
		session.close();

		verifyBasicSaveObject("save", "valueForColumnOne", "", null, null);
	}

	private void verifyBasicSaveObject(String key, String columnOne, String columnTwo,
									   Long col10, BigDecimal bigDecimal) {
		Serializer<String> stringSerializer = StringSerializer.get();
		SliceQuery<String, String, String> query =
				HFactory.createSliceQuery(testKeyspace, stringSerializer, stringSerializer, stringSerializer);
		query.setColumnFamily("BasicTestObject");
		query.setKey(key);
		query.setColumnNames("columnOne", "columnTwo", "bigDecimal");
		QueryResult<ColumnSlice<String, String>> result = query.execute();
		HColumn<String, String> one = result.get().getColumnByName("columnOne");
		if(columnOne == null) {
			assertNull(one);
		} else {
			assertEquals(columnOne, one.getValue());
			assertEquals(Integer.valueOf(20), Integer.valueOf(one.getTtl()));
		}
		HColumn<String, String> two = result.get().getColumnByName("columnTwo");
		if(columnTwo == null) {
			assertNull(two);
		} else {
			assertEquals(columnTwo, two.getValue());
			assertEquals(Integer.valueOf(0), Integer.valueOf(two.getTtl()));
		}
		Serializer<Long> longSerializer = LongSerializer.get();
		ColumnQuery<String, Long, Long> query2 =
				HFactory.createColumnQuery(testKeyspace, stringSerializer, longSerializer, longSerializer);
		query2.setColumnFamily("BasicTestObject");
		query2.setKey(key);
		query2.setName(Long.valueOf(10));
		QueryResult<HColumn<Long, Long>> res = query2.execute();
		if(col10 == null) {
			if(res.get() != null) {
				assertNull("got value when expecting null: " + res.get().getValue(), res.get().getValue());
			} else {
				assertNull(res.get());
			}
		} else {
			assertEquals(col10, res.get().getValue());
		}

		Serializer<BigDecimal> bigDSerializer = BigDecimalSerializer.get();
		ColumnQuery<String, String, BigDecimal> query3 =
				HFactory.createColumnQuery(testKeyspace, stringSerializer, stringSerializer, bigDSerializer);
		query3.setColumnFamily("BasicTestObject");
		query3.setKey(key);
		query3.setName("bigDecimal");
		QueryResult<HColumn<String, BigDecimal>> res3 = query3.execute();
		if(bigDecimal == null) {
			if(res3.get() != null) {
				assertNull("got value when expecting null: " + res3.get().getValue(), res3.get().getValue());
			} else {
				assertNull(res3.get());
			}
		} else {
			assertEquals(bigDecimal, res3.get().getValue());
		}
	}

	@Test
	public void testDelete() throws Exception {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "delete", "foo", "bar", Long.valueOf(21), BigDecimal.TEN);
		mutator.execute();


		VirpSessionSpec spec =
				new VirpSessionSpec(config).withSessionAttachmentMode(SessionAttachmentMode.MANUAL_FLUSH);
		VirpSession session = config.newSession(spec);
		BasicSaveObject res = session.get(BasicSaveObject.class, "delete");
		session.delete(res);
		// should not delete until flush
		verifyBasicSaveObject("delete", "foo", "bar", Long.valueOf(21), BigDecimal.TEN);
		session.flush();
		verifyBasicSaveObject("delete", null, null, null, null);
		
		session.close();
	}
	
	@Test
	public void testBasicReadSessionAttachmentModeNone() {
		testBasicRead(SessionAttachmentMode.MANUAL_FLUSH);
	}
	
	@Test
	public void testBasicReadAutoFlush() {
		testBasicRead(SessionAttachmentMode.AUTO_FLUSH);
	}
	
	@Test
	public void testBasicReadManualFlush() {
		testBasicRead(SessionAttachmentMode.MANUAL_FLUSH);
	}

	private void testBasicRead(SessionAttachmentMode flushMode) {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "read", "foo", "bar", Long.valueOf(21), BigDecimal.ONE);
		mutator.execute();

		VirpSessionSpec spec = new VirpSessionSpec(config).withSessionAttachmentMode(flushMode);
		VirpSession session = config.newSession(spec);
		BasicSaveObject res = session.get(BasicSaveObject.class, "read");
		assertNotNull(res);
		assertEquals("read", res.getKey());
		assertEquals("foo", res.getColumnOne());
		assertEquals("bar", res.getColumnTwo());
		assertEquals(Long.valueOf(21), res.getColumnTen());
		assertEquals(BigDecimal.ONE, res.getBigDecimal());

		session.close();
	}
	
	@Test
	public void testChanges() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "changes", "of", "ta", Long.valueOf(21), BigDecimal.ONE);
		mutator.execute();

		VirpSessionSpec spec =
				new VirpSessionSpec(config).withSessionAttachmentMode(SessionAttachmentMode.AUTO_FLUSH);
		VirpSession session = config.newSession(spec);
		BasicSaveObject res = session.get(BasicSaveObject.class, "changes");
		res.setColumnOne("itsbeenchanged");
		res.setColumnTwo("soHasThis");

		Serializer<String> stringSerializer = StringSerializer.get();
		SliceQuery<String, String, String> query =
				HFactory.createSliceQuery(testKeyspace, stringSerializer, stringSerializer, stringSerializer);
		query.setColumnFamily("BasicTestObject");
		query.setKey("changes");
		query.setColumnNames("columnOne", "columnTwo");
		QueryResult<ColumnSlice<String, String>> result = query.execute();
		assertEquals("of", result.get().getColumnByName("columnOne").getValue());
		assertEquals("ta", result.get().getColumnByName("columnTwo").getValue());

		session.close();

		query =
				HFactory.createSliceQuery(testKeyspace, stringSerializer, stringSerializer, stringSerializer);
		query.setColumnFamily("BasicTestObject");
		query.setKey("changes");
		query.setColumnNames("columnOne", "columnTwo");
		result = query.execute();
		assertEquals("itsbeenchanged", result.get().getColumnByName("columnOne").getValue());
		assertEquals("soHasThis", result.get().getColumnByName("columnTwo").getValue());
	}

	@Test
	public void testReadNoRecordNullConfig() {
		VirpSessionSpec spec = new VirpSessionSpec(config).withNoColumnEqualsNullRow(true);
		VirpSession session = config.newSession(spec);

		BasicSaveObject res = session.get(BasicSaveObject.class, "nothingToSeeHere");
		assertNull(res);

		session.close();
	}

	@Test
	public void testReadNoRecordNotNullConfig() {
		VirpSessionSpec spec = new VirpSessionSpec(config).withNoColumnEqualsNullRow(false);
		VirpSession session = config.newSession();

		BasicSaveObject res = session.get(BasicSaveObject.class, "kindaSomethingToSeeHere");
		assertNotNull(res);
		assertEquals("kindaSomethingToSeeHere", res.getKey());
		assertNull(res.getColumnOne());
		assertNull(res.getColumnTwo());
		assertEquals(Long.valueOf(0), res.getColumnTen());

		session.close();
	}

	@Test
	public void testGetMultiple() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleA", "one", "two", Long.valueOf(22), BigDecimal.TEN);
		createBasicTestObject(mutator, "multipleB", "three", "four", Long.valueOf(23), BigDecimal.ONE);
		mutator.execute();

		VirpSession session = config.newSession();
		List<BasicSaveObject> result = session.get(BasicSaveObject.class, "multipleA", "multipleB");
		assertEquals(2, result.size());
		boolean aHit = false;
		boolean bHit = false;
		for(BasicSaveObject object : result) {
			if("multipleA".equals(object.getKey())) {
				aHit = true;
				assertEquals("one", object.getColumnOne());
				assertEquals("two", object.getColumnTwo());
				assertEquals(Long.valueOf(22), Long.valueOf(object.getColumnTen()));
				assertEquals(BigDecimal.TEN, object.getBigDecimal());
			} else if("multipleB".equals(object.getKey())) {
				bHit = true;
				assertEquals("three", object.getColumnOne());
				assertEquals("four", object.getColumnTwo());
				assertEquals(Long.valueOf(23), Long.valueOf(object.getColumnTen()));
				assertEquals(BigDecimal.ONE, object.getBigDecimal());
			} else {
				fail("Unexpected key: "  + object.getKey());
			}
		}
		assertTrue(aHit);
		assertTrue(bHit);
	}

	@Test
	public void testGetMultipleMap() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleC", "one", "two", Long.valueOf(22), BigDecimal.ZERO);
		createBasicTestObject(mutator, "multipleD", "three", "four", Long.valueOf(23), BigDecimal.ZERO);
		mutator.execute();

		VirpSession session = config.newSession();
		Map<String, BasicSaveObject> result = session.getAsMap(BasicSaveObject.class, "multipleC", "multipleD");
		assertEquals(2, result.size());
		assertTrue(result.containsKey("multipleC"));
		assertTrue(result.containsKey("multipleD"));
		BasicSaveObject object = result.get("multipleC");
		assertEquals("multipleC", object.key);
		assertEquals("one", object.columnOne);
		assertEquals("two", object.columnTwo);
		assertEquals(Long.valueOf(22), Long.valueOf(object.columnTen));
		assertEquals(BigDecimal.ZERO, object.getBigDecimal());

		object = result.get("multipleD");
		assertEquals("multipleD", object.key);
		assertEquals("three", object.columnOne);
		assertEquals("four", object.columnTwo);
		assertEquals(Long.valueOf(23), Long.valueOf(object.columnTen));
		assertEquals(BigDecimal.ZERO, object.getBigDecimal());
	}

	@Test
	public void testFindByExampleQueryOnIndexed() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createIndexedObject(mutator, "foo", "one", Long.valueOf(10));
		createIndexedObject(mutator, "bar", "one", Long.valueOf(9));
		createIndexedObject(mutator, "blah", "two", Long.valueOf(10));
		mutator.execute();

		VirpSession session = config.newSession();

		SecondaryIndexObject test = session.get(SecondaryIndexObject.class, "foo");
		assertNotNull("sanity check failed", test);
		assertEquals("foo", test.getKey());
		assertEquals("one", test.getIndexed());
		assertEquals(Long.valueOf(10), test.getUnindexed());

		SecondaryIndexObject example = new SecondaryIndexObject();
		example.setIndexed("one");
		example.setUnindexed(Long.valueOf(10));

		Query<SecondaryIndexObject> query = session.createByExampleQuery(example);
		List<SecondaryIndexObject> matches = session.find(query);
		assertEquals(Integer.valueOf(1), Integer.valueOf(matches.size()));

		SecondaryIndexObject res = matches.get(0);
		assertEquals("foo", res.getKey());
		assertEquals("one", res.getIndexed());
		assertEquals(Long.valueOf(10), res.getUnindexed());
	}

	@Test
	public void testFindByExampleQueryOnUnidexed() {
		expectedException.expect(VirpHectorException.class);

		VirpSession session = config.newSession();

		SecondaryIndexObject example = new SecondaryIndexObject();
		example.setUnindexed(Long.valueOf(10));

		Query<SecondaryIndexObject> query = session.createByExampleQuery(example);
		session.find(query);
	}

	@Test
	public void testGetMultipleNotNullConfig() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleE", "one", "two", Long.valueOf(22), BigDecimal.ZERO);
		mutator.execute();

		VirpSessionSpec spec = new VirpSessionSpec(config).withNoColumnEqualsNullRow(false);
		VirpSession session = config.newSession(spec);
		List<BasicSaveObject> result = session.get(BasicSaveObject.class, "multipleE", "multipleF");
		assertEquals(2, result.size());
		boolean eHit = false;
		boolean fHit = false;
		for(BasicSaveObject object : result) {
			if("multipleE".equals(object.getKey())) {
				eHit = true;
				assertEquals("one", object.getColumnOne());
				assertEquals("two", object.getColumnTwo());
				assertEquals(Long.valueOf(22), Long.valueOf(object.getColumnTen()));
				assertEquals(BigDecimal.ZERO, object.getBigDecimal());
			} else if("multipleF".equals(object.getKey())) {
				fHit = true;
				assertNull(object.getColumnOne());
				assertNull(object.getColumnTwo());
				assertEquals(Long.valueOf(0), Long.valueOf(object.getColumnTen()));
				assertNull(object.getBigDecimal());
			} else {
				fail("Unexpected key: "  + object.getKey());
			}
		}
		assertTrue(eHit);
		assertTrue(fHit);
	}

	@Test
	public void testGetMultipleNullConfig() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleG", "one", "two", Long.valueOf(22), BigDecimal.ZERO);
		mutator.execute();

		VirpSessionSpec spec = new VirpSessionSpec(config).withNoColumnEqualsNullRow(true);
		VirpSession session = config.newSession(spec);
		List<BasicSaveObject> result = session.get(BasicSaveObject.class, "multipleG", "multipleH");
		assertEquals(1, result.size());

		BasicSaveObject object = result.get(0);

		assertEquals("multipleG", object.getKey());
		assertEquals("one", object.getColumnOne());
		assertEquals("two", object.getColumnTwo());
		assertEquals(BigDecimal.ZERO, object.getBigDecimal());
		assertEquals(Long.valueOf(22), Long.valueOf(object.getColumnTen()));
	}

	private void createIndexedObject(Mutator<String> mutator, String key, String indexed, Long unindexed) {
		Serializer<String> stringSerializer = StringSerializer.get();
		Serializer<Long> longSerializer = LongSerializer.get();
		mutator.addInsertion(key, "HasSecondaryIndex",
				HFactory.createColumn("indexed", indexed, stringSerializer, stringSerializer));
		mutator.addInsertion(key, "HasSecondaryIndex",
				HFactory.createColumn(Long.valueOf(10), unindexed, longSerializer, longSerializer));
	}

	private void createBasicTestObject(Mutator<String> mutator, String key, String columnOne, String columnTwo,
									   Long columnTen, BigDecimal bigDecimal) {
		Serializer<String> stringSerializer = StringSerializer.get();
		Serializer<Long> longSerializer = LongSerializer.get();
		Serializer<BigDecimal> decimalSerializer = BigDecimalSerializer.get();
		mutator.addInsertion(key, "BasicTestObject",
				HFactory.createColumn("columnOne", columnOne, 20, stringSerializer, stringSerializer));
		mutator.addInsertion(key, "BasicTestObject",
				HFactory.createColumn("columnTwo", columnTwo, stringSerializer, stringSerializer));
		mutator.addInsertion(key, "BasicTestObject",
				HFactory.createColumn(Long.valueOf(10), columnTen, longSerializer, longSerializer));
		mutator.addInsertion(key, "BasicTestObject",
				HFactory.createColumn("bigDecimal", bigDecimal, stringSerializer, decimalSerializer));
	}

}
