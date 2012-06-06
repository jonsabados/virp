package com.jshnd.virp.jackson;

import com.google.common.collect.Sets;
import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.annotation.*;
import com.jshnd.virp.config.ConfiguredRowMapperSource;
import com.jshnd.virp.config.SessionAttachmentMode;
import com.jshnd.virp.hector.HectorSessionFactory;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.BasicKeyspaceDefinition;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.testutils.EmbeddedServerHelper;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;
import static org.junit.Assert.assertEquals;

public class JacksonTransformerITCase {

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
		columnFamilyDefinition.setName("JacksonTest");
		columnFamilyDefinition.setKeyspaceName(testKeyspace.getKeyspaceName());
		cluster.addColumnFamily(columnFamilyDefinition);
	}

	@AfterClass
	public static void cleanup() throws Exception {
		EmbeddedServerHelper.cleanup();
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		cluster.truncate("TEST", "JacksonTest");
		config = new VirpConfig();
		config.setMetaDataReader(new AnnotationDrivenRowMapperMetaDataReader());
		ConfiguredRowMapperSource source = new ConfiguredRowMapperSource();
		source.setRowMapperClasses(Sets.<Class<?>>newHashSet(JacksonWrappedObject.class));
		config.setRowMapperSource(source);
		HectorSessionFactory actionFactory = new HectorSessionFactory();
		actionFactory.setKeyspace(testKeyspace);
		config.setSessionFactory(actionFactory);
		config.setDefaultSessionAttachmentMode(SessionAttachmentMode.AUTO_FLUSH);
		config.init();
	}

	@RowMapper(columnFamily = "JacksonTest")
	public static class	JacksonWrappedObject {

		@Key
		private String key;

		@NamedColumn(name = "control")
		private String control;

		@NamedColumn(name = "jsonData")
		@DataTransformer(transformer = JacksonTransformer.class)
		private Map<String, String> dataMap;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getControl() {
			return control;
		}

		public void setControl(String control) {
			this.control = control;
		}

		public Map<String, String> getDataMap() {
			return dataMap;
		}

		public void setDataMap(Map<String, String> dataMap) {
			this.dataMap = dataMap;
		}
	}

	@Test
	public void testJsonMap() {
		VirpSession session = config.newSession();
		JacksonWrappedObject saveObj = new JacksonWrappedObject();
		Map<String, String> vals = new HashMap<String, String>();
		vals.put("one", "1");
		vals.put("two", "2");
		saveObj.setKey("myKey");
		saveObj.setControl("sanity");
		saveObj.setDataMap(vals);
		session.save(saveObj);
		session.close();

		session = config.newSession();
		JacksonWrappedObject get = session.get(JacksonWrappedObject.class, "myKey");
		assertEquals("myKey", get.getKey());
		assertEquals("sanity", get.getControl());
		Map<String, String> dataMap = get.getDataMap();
		assertEquals(Integer.valueOf(2), Integer.valueOf(dataMap.size()));
		assertEquals("1", dataMap.get("one"));
		assertEquals("2", dataMap.get("two"));
		session.close();
	}


}
