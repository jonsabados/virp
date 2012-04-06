package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.config.RowMapperSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VirpSession {

	private boolean initialized = false;

	private static final Logger log = LoggerFactory.getLogger(VirpSession.class);

	private RowMapperSource rowMapperSource;

	private RowMapperMetaDataReader metaDataReader;

	private VirpActionFactory actionFactory;

	private Map<Class<?>, RowMapperMetaData> configuredClasses;

	public VirpActionResult writeRow(Object row) {
		if(!initialized) {
			throw new VirpException("Session has not been initialized - call init() first.");
		}
		Class<?> rowClass = row.getClass();
		if(!configuredClasses.containsKey(rowClass)) {
			throw new VirpException(rowClass.getCanonicalName() + " has not been configured");
		}
		VirpAction action = actionFactory.newAction();
		action.writeRow(row, configuredClasses.get(rowClass));
		return action.complete();
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
		if(actionFactory == null) {
			throw new VirpException("actionFactory is required");
		}
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

	public void setActionFactory(VirpActionFactory actionFactory) {
		this.actionFactory = actionFactory;
	}

	protected Map<Class<?>, RowMapperMetaData> getConfiguredClasses() {
		return configuredClasses;
	}

}
