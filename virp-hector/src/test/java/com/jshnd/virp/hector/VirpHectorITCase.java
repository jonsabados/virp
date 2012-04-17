package com.jshnd.virp.hector;

import com.google.common.collect.Sets;
import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.annotation.*;
import com.jshnd.virp.config.ConfiguredRowMapperSource;
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
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import me.prettyprint.hector.testutils.EmbeddedServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;
import static org.junit.Assert.assertEquals;

public class VirpHectorITCase {

	private static EmbeddedServerHelper helper;

	private static Cluster cluster;

	private static Keyspace testKeyspace;

	private VirpConfig config;

	@BeforeClass
	public static void startupEmbeddedCluster() throws Exception {
		helper = new EmbeddedServerHelper();
		helper.setup();
		cluster = getOrCreateCluster("Test Cluster", "127.0.0.1:9160");
		BasicKeyspaceDefinition definition = new BasicKeyspaceDefinition();
		definition.setName("TEST");
		definition.setStrategyClass("org.apache.cassandra.locator.SimpleStrategy");
		definition.setReplicationFactor(1);
		cluster.addKeyspace(definition);
		testKeyspace = createKeyspace("TEST", cluster);
	}

	@AfterClass
	public static void cleanup() throws Exception {
		EmbeddedServerHelper.cleanup();
	}

	@Before
	public void setup() {
		config = new VirpConfig();
		config.setMetaDataReader(new AnnotationDrivenRowMapperMetaDataReader());
	}

	@RowMapper(columnFamily = "BasicSaveObject")
	public static class BasicSaveObject {

		@KeyColumn
		@SuppressWarnings("unused")
		private String key;

		@NamedColumn(name = "columnOne")
		@SuppressWarnings("unused")
		private String columnOne;

		@NamedColumn(name = "columnTwo")
		@SuppressWarnings("unused")
		private String columnTwo;

		@NumberedColumn(number = 10)
		private long columnTen;

	}

	@Test
	public void testBasicSave() {
		ColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
		columnFamilyDefinition.setName("BasicSaveObject");
		columnFamilyDefinition.setKeyspaceName(testKeyspace.getKeyspaceName());
		cluster.addColumnFamily(columnFamilyDefinition);

		ConfiguredRowMapperSource source = new ConfiguredRowMapperSource();
		source.setRowMapperClasses(Sets.<Class<?>>newHashSet(BasicSaveObject.class));
		config.setRowMapperSource(source);
		HectorSessionFactory actionFactory = new HectorSessionFactory();
		actionFactory.setKeyspace(testKeyspace);
		config.setSessionFactory(actionFactory);
		config.init();

		BasicSaveObject row = new BasicSaveObject();
		row.key = "bob";
		row.columnOne = "valueForColumnOne";
		row.columnTwo = "valueForColumnTwo";
		row.columnTen = 20;

		VirpSession session = config.newSession(BasicSaveObject.class);
		session.save(row);
		session.close();

		Serializer stringSerializer = StringSerializer.get();
		SliceQuery<String, String, String> query =
				HFactory.createSliceQuery(testKeyspace, stringSerializer, stringSerializer, stringSerializer);
		query.setColumnFamily("BasicSaveObject");
		query.setKey("bob");
		query.setColumnNames("columnOne", "columnTwo");
		QueryResult<ColumnSlice<String, String>> result = query.execute();
		assertEquals("valueForColumnOne", result.get().getColumnByName("columnOne").getValue());
		assertEquals("valueForColumnTwo", result.get().getColumnByName("columnTwo").getValue());

		Serializer longSerializer = LongSerializer.get();
		ColumnQuery<String, Long, Long> query2 =
				HFactory.createColumnQuery(testKeyspace, stringSerializer, longSerializer, longSerializer);
		query2.setColumnFamily("BasicSaveObject");
		query2.setKey("bob");
		query2.setName(Long.valueOf(10));
		QueryResult<HColumn<Long, Long>> res = query2.execute();
		assertEquals(Long.valueOf(20), res.get().getValue());
	}

}
