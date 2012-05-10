package com.jshnd.virp.hector;

import com.google.common.collect.Sets;
import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.annotation.*;
import com.jshnd.virp.config.ConfiguredRowMapperSource;
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
		cluster = getOrCreateCluster("Test Cluster", "127.0.0.1:9160");
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
	@SuppressWarnings("unchecked")
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
		config.init();
	}

	@SuppressWarnings("unused")
	@RowMapper(columnFamily = "HasSecondaryIndex")
	public static class	SecondaryIndexObject {

		@KeyColumn
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

	@SuppressWarnings("unused")
	@RowMapper(columnFamily = "BasicTestObject")
	public static class BasicSaveObject {

		@KeyColumn
		private String key;

		@NamedColumn(name = "columnOne")
		@TimeToLive(seconds = 20)
		private String columnOne;

		@NamedColumn(name = "columnTwo")
		private String columnTwo;

		@NumberedColumnLong(number = 10)
		private long columnTen;

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

		public long getColumnTen() {
			return columnTen;
		}

		public void setColumnTen(long columnTen) {
			this.columnTen = columnTen;
		}
	}

	@Test
	public void testBasicSave() {
		BasicSaveObject row = new BasicSaveObject();
		row.key = "save";
		row.columnOne = "valueForColumnOne";
		row.columnTwo = "valueForColumnTwo";
		row.columnTen = 20;

		VirpSession session = config.newSession();
		session.save(row);
		session.close();

		Serializer<String> stringSerializer = StringSerializer.get();
		SliceQuery<String, String, String> query =
				HFactory.createSliceQuery(testKeyspace, stringSerializer, stringSerializer, stringSerializer);
		query.setColumnFamily("BasicTestObject");
		query.setKey("save");
		query.setColumnNames("columnOne", "columnTwo");
		QueryResult<ColumnSlice<String, String>> result = query.execute();
		HColumn one = result.get().getColumnByName("columnOne");
		assertEquals("valueForColumnOne", one.getValue());
		assertEquals(Integer.valueOf(20), Integer.valueOf(one.getTtl()));
		HColumn two = result.get().getColumnByName("columnTwo");
		assertEquals("valueForColumnTwo", two.getValue());
		assertEquals(Integer.valueOf(0), Integer.valueOf(two.getTtl()));
		Serializer<Long> longSerializer = LongSerializer.get();
		ColumnQuery<String, Long, Long> query2 =
				HFactory.createColumnQuery(testKeyspace, stringSerializer, longSerializer, longSerializer);
		query2.setColumnFamily("BasicTestObject");
		query2.setKey("save");
		query2.setName(Long.valueOf(10));
		QueryResult<HColumn<Long, Long>> res = query2.execute();
		assertEquals(Long.valueOf(20), res.get().getValue());
	}

	@Test
	public void testBasicRead() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "read", "foo", "bar", Long.valueOf(21));
		mutator.execute();

		VirpSession session = config.newSession();
		BasicSaveObject res = session.get(BasicSaveObject.class, "read");
		assertNotNull(res);
		assertEquals("read", res.key);
		assertEquals("foo", res.columnOne);
		assertEquals("bar", res.columnTwo);
		assertEquals(21, res.columnTen);

		session.close();
	}

	@Test
	public void testChanges() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "changes", "of", "ta", Long.valueOf(21));
		mutator.execute();

		VirpSession session = config.newSession(SessionAttachmentMode.AUTO_FLUSH);
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
		config.setNoColumnsEqualsNullRow(true);
		VirpSession session = config.newSession();

		BasicSaveObject res = session.get(BasicSaveObject.class, "nothingToSeeHere");
		assertNull(res);

		session.close();
	}

	@Test
	public void testReadNoRecordNotNullConfig() {
		config.setNoColumnsEqualsNullRow(false);
		VirpSession session = config.newSession();

		BasicSaveObject res = session.get(BasicSaveObject.class, "kindaSomethingToSeeHere");
		assertNotNull(res);
		assertEquals("kindaSomethingToSeeHere", res.key);
		assertNull(res.columnOne);
		assertNull(res.columnTwo);
		assertEquals(0, res.columnTen);

		session.close();
	}

	@Test
	public void testGetMultiple() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleA", "one", "two", Long.valueOf(22));
		createBasicTestObject(mutator, "multipleB", "three", "four", Long.valueOf(23));
		mutator.execute();

		VirpSession session = config.newSession();
		List<BasicSaveObject> result = session.get(BasicSaveObject.class, "multipleA", "multipleB");
		assertEquals(2, result.size());
		boolean aHit = false;
		boolean bHit = false;
		for(BasicSaveObject object : result) {
			if("multipleA".equals(object.key)) {
				aHit = true;
				assertEquals("one", object.columnOne);
				assertEquals("two", object.columnTwo);
				assertEquals(Long.valueOf(22), Long.valueOf(object.columnTen));
			} else if("multipleB".equals(object.key)) {
				bHit = true;
				assertEquals("three", object.columnOne);
				assertEquals("four", object.columnTwo);
				assertEquals(Long.valueOf(23), Long.valueOf(object.columnTen));
			} else {
				fail("Unexpected key: "  + object.key);
			}
		}
		assertTrue(aHit);
		assertTrue(bHit);
	}

	@Test
	public void testGetMultipleMap() {
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleC", "one", "two", Long.valueOf(22));
		createBasicTestObject(mutator, "multipleD", "three", "four", Long.valueOf(23));
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

		object = result.get("multipleD");
		assertEquals("multipleD", object.key);
		assertEquals("three", object.columnOne);
		assertEquals("four", object.columnTwo);
		assertEquals(Long.valueOf(23), Long.valueOf(object.columnTen));
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
		config.setNoColumnsEqualsNullRow(false);
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleE", "one", "two", Long.valueOf(22));
		mutator.execute();

		VirpSession session = config.newSession();
		List<BasicSaveObject> result = session.get(BasicSaveObject.class, "multipleE", "multipleF");
		assertEquals(2, result.size());
		boolean eHit = false;
		boolean fHit = false;
		for(BasicSaveObject object : result) {
			if("multipleE".equals(object.key)) {
				eHit = true;
				assertEquals("one", object.columnOne);
				assertEquals("two", object.columnTwo);
				assertEquals(Long.valueOf(22), Long.valueOf(object.columnTen));
			} else if("multipleF".equals(object.key)) {
				fHit = true;
				assertNull(object.columnOne);
				assertNull(object.columnTwo);
				assertEquals(Long.valueOf(0), Long.valueOf(object.columnTen));
			} else {
				fail("Unexpected key: "  + object.key);
			}
		}
		assertTrue(eHit);
		assertTrue(fHit);
	}

	@Test
	public void testGetMultipleNullConfig() {
		config.setNoColumnsEqualsNullRow(true);
		Mutator<String> mutator = HFactory.createMutator(testKeyspace, StringSerializer.get());
		createBasicTestObject(mutator, "multipleG", "one", "two", Long.valueOf(22));
		mutator.execute();

		VirpSession session = config.newSession();
		List<BasicSaveObject> result = session.get(BasicSaveObject.class, "multipleG", "multipleH");
		assertEquals(1, result.size());

		BasicSaveObject object = result.get(0);

		assertEquals("multipleG", object.key);
		assertEquals("one", object.columnOne);
		assertEquals("two", object.columnTwo);
		assertEquals(Long.valueOf(22), Long.valueOf(object.columnTen));
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
									   Long columnTen) {
		Serializer<String> stringSerializer = StringSerializer.get();
		Serializer<Long> longSerializer = LongSerializer.get();
		mutator.addInsertion(key, "BasicTestObject",
				HFactory.createColumn("columnOne", columnOne, stringSerializer, stringSerializer));
		mutator.addInsertion(key, "BasicTestObject",
				HFactory.createColumn("columnTwo", columnTwo, stringSerializer, stringSerializer));
		mutator.addInsertion(key, "BasicTestObject",
				HFactory.createColumn(Long.valueOf(10), columnTen, longSerializer, longSerializer));
	}

}
