package com.jshnd.virp.hector;

import com.google.common.collect.Sets;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.annotation.Column;
import com.jshnd.virp.annotation.KeyColumn;
import com.jshnd.virp.annotation.RowMapper;
import com.jshnd.virp.annotation.AnnotationDrivenRowMapperMetaDataReader;
import com.jshnd.virp.config.ConfiguredRowMapperSource;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.testutils.EmbeddedServerHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;

public class VirpHectorITCase {

	private static EmbeddedServerHelper helper;

	private static Cluster cluster;

	private static Keyspace testKeyspace;

	private VirpSession session;

	@BeforeClass
	public static void startupEmbeddedCluster() throws Exception {
		helper = new EmbeddedServerHelper();
		helper.setup();
		cluster = getOrCreateCluster("Test Cluster", "127.0.0.1:9170");
		testKeyspace = createKeyspace("TEST", cluster);
	}

	@AfterClass
	public static void cleanup() throws Exception {
		EmbeddedServerHelper.cleanup();
	}

	@Before
	public void setup() {
		session = new VirpSession();
		session.setMetaDataReader(new AnnotationDrivenRowMapperMetaDataReader());
	}

	@RowMapper(columnFamily = "BasicSaveObject")
	public static class BasicSaveObject {

		@KeyColumn
		private String key;

		@Column(name = "columnOne")
		private String columnOne;

		@Column(name = "columnTwo")
		private String columnTwo;

	}

	@Test
	public void testBasicSave() {
		ConfiguredRowMapperSource source = new ConfiguredRowMapperSource();
		source.setRowMapperClasses(Sets.<Class<?>>newHashSet(BasicSaveObject.class));
		session.setRowMapperSource(source);
		session.init();
	}

}
