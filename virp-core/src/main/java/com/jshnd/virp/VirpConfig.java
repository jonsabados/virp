package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.config.RowMapperSource;
import com.jshnd.virp.exception.VirpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VirpConfig {

	private boolean initialized = false;

	private static final Logger log = LoggerFactory.getLogger(VirpConfig.class);

	private RowMapperSource rowMapperSource;

	private RowMapperMetaDataReader metaDataReader;

	private VirpSessionFactory sessionFactory;

	private Map<Class<?>, RowMapperMetaData> configuredClasses;

	public VirpSession newSession() {
		if(!initialized) {
			throw new VirpException("Session has not been initialized - call init() first.");
		}
		return sessionFactory.newSession(this);
	}

	public synchronized void init() {
		sanityChecks();
		Map<Class<?>, RowMapperMetaData> workingMap = new HashMap<Class<?>, RowMapperMetaData>();
		Collection<Class<?>> rowMapperClasses = rowMapperSource.getRowMapperClasses();
		log.info("Found " + rowMapperClasses.size() + " mapping classes");
		for (Class<?> c : rowMapperClasses) {
			configureClass(c, workingMap);
		}
		configuredClasses = Collections.unmodifiableMap(workingMap);
		initialized = true;
	}

	protected RowMapperMetaData getMetaData(Class<?> type) {
		return configuredClasses.get(type);
	}

	private void sanityChecks() {
		if(initialized) {
			throw new VirpException("Already initialized");
		}
		if(rowMapperSource == null) {
			throw new VirpException("rowMapperSource is required");
		}
		if(metaDataReader == null) {
			throw new VirpException("metaDataReader is required");
		}
		if(sessionFactory == null) {
			throw new VirpException("sessionFactory is required");
		}
	}

	private void configureClass(Class<?> clazz, Map<Class<?>, RowMapperMetaData> workingMap) {
		log.info("Configuring rowmapper class " + clazz);
		RowMapperMetaData meta = metaDataReader.readClass(clazz);
		sessionFactory.setupClass(meta);
		workingMap.put(clazz, meta);
	}

	public void setRowMapperSource(RowMapperSource rowMapperSource) {
		this.rowMapperSource = rowMapperSource;
	}

	public void setMetaDataReader(RowMapperMetaDataReader metaDataReader) {
		this.metaDataReader = metaDataReader;
	}

	public void setSessionFactory(VirpSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Map<Class<?>, RowMapperMetaData> getConfiguredClasses() {
		return configuredClasses;
	}

}
