package com.jshnd.casrom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jshnd.casrom.config.RowMapperMetaData;
import com.jshnd.casrom.config.RowMapperMetaDataReader;
import com.jshnd.casrom.config.RowMapperSource;

public class CasromContext {

	private static final Logger log = LoggerFactory.getLogger(CasromContext.class);
	
	private RowMapperSource rowMapperSource;

	private RowMapperMetaDataReader metaDataReader;
	
	private Map<Class<?>, RowMapperMetaData> configuredClasses;
	
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
