package com.jshnd.virp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.prettyprint.hector.api.Keyspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.config.RowMapperSource;

public class VirpSession {

	private static final Logger log = LoggerFactory.getLogger(VirpSession.class);
	
	private RowMapperSource rowMapperSource;

	private RowMapperMetaDataReader metaDataReader;
	
	private Map<Class<?>, RowMapperMetaData> configuredClasses;

    private Keyspace keyspace;
	
	public void init() {
		Map<Class<?>, RowMapperMetaData> workingMap = new HashMap<Class<?>, RowMapperMetaData>();
		Collection<Class<?>> rowMapperClasses = rowMapperSource.getRowMapperClasses();
		log.info("Found " + rowMapperClasses.size() + " mapping classes");	
		for(Class<?> c : rowMapperClasses) {
			configureClass(c, workingMap);
		}
		configuredClasses = Collections.unmodifiableMap(workingMap);
	}
	
	private void configureClass(Class<?> clazz, Map<Class<?>, RowMapperMetaData> workingMap) {
		log.info("Configuring rowmapper class " + clazz);
		RowMapperMetaData meta = metaDataReader.readClass(clazz);
		workingMap.put(clazz, meta);
	}
	
	public void setRowMapperSource(RowMapperSource rowMapperSource) {
		this.rowMapperSource = rowMapperSource;
	}

	public void setMetaDataReader(RowMapperMetaDataReader metaDataReader) {
		this.metaDataReader = metaDataReader;
	}

	protected Map<Class<?>, RowMapperMetaData> getConfiguredClasses() {
		return configuredClasses;
	}
	
}
