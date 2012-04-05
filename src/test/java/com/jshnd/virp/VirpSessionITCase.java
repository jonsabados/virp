package com.jshnd.virp;

import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;
import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.testutils.EmbeddedServerHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class VirpSessionITCase {

	private static EmbeddedServerHelper helper;
	
	private static Cluster cluster;
	
	private static Keyspace testKeyspace;
	
	@BeforeClass
	public static void startupEmbedded() throws Exception {
		helper = new EmbeddedServerHelper();
		helper.setup();
		cluster = getOrCreateCluster("Test Cluster", "127.0.0.1:9170");
		testKeyspace = createKeyspace("TEST", cluster);
	}

	@AfterClass
	public static void cleanup() throws Exception {
		EmbeddedServerHelper.cleanup();
	}
	
	@Test
	public void testNothing() {
		
	}
	
}
