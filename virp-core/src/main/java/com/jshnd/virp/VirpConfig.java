/*
 * Copyright 2012 Jonathan Sabados
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jshnd.virp;

import com.jshnd.virp.config.*;
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

	private Map<Class<?>, RowMapperMetaData<?>> configuredClasses;

	private boolean noColumnsEqualsNullRow = false;

	private SessionAttachmentMode defaultSessionAttachmentMode = SessionAttachmentMode.NONE;

	private NullColumnSaveBehavior defaultNullColumnSaveBehavior = NullColumnSaveBehavior.NO_COLUMN;
	
	public VirpSession newSession() {
		return newSession(new VirpSessionSpec(this));
	}

	public VirpSession newSession(VirpSessionSpec sessionSpec) {
		if(!initialized) {
			throw new VirpException("Session has not been initialized - call init() first.");
		}
		return sessionFactory.newSession(this, sessionSpec);
	}

	public synchronized void init() {
		sanityChecks();
		Map<Class<?>, RowMapperMetaData<?>> workingMap = new HashMap<Class<?>, RowMapperMetaData<?>>();
		Collection<Class<?>> rowMapperClasses = rowMapperSource.getRowMapperClasses();
		log.info("Found " + rowMapperClasses.size() + " mapping classes");
		for (Class<?> c : rowMapperClasses) {
			configureClass(c, workingMap);
		}
		configuredClasses = Collections.unmodifiableMap(workingMap);
		initialized = true;
	}

	@SuppressWarnings("unchecked")
	protected <T> RowMapperMetaData<T> getMetaData(Class<T> type) {
		return (RowMapperMetaData<T>) configuredClasses.get(type);
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

	private void configureClass(Class<?> clazz, Map<Class<?>, RowMapperMetaData<?>> workingMap) {
		log.info("Configuring rowmapper class " + clazz);
		RowMapperMetaData<?> meta = metaDataReader.readClass(clazz);
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

	protected boolean isNoColumnsEqualsNullRow() {
		return noColumnsEqualsNullRow;
	}

	public void setNoColumnsEqualsNullRow(boolean noColumnsEqualsNullRow) {
		this.noColumnsEqualsNullRow = noColumnsEqualsNullRow;
	}

	protected NullColumnSaveBehavior getNullColumnSaveBehavior() {
		return defaultNullColumnSaveBehavior;
	}

	/**
	 * Controls how rows are saved when dealing with a null value. Defaults to
	 * {@link NullColumnSaveBehavior#NO_COLUMN}.
	 * 
	 * @param nullColumnSaveBehavior
	 *            The behavior to use when persisting a row with a column
	 *            definition that has a null value
	 */
	public void setNullColumnSaveBehavior(NullColumnSaveBehavior nullColumnSaveBehavior) {
		this.defaultNullColumnSaveBehavior = nullColumnSaveBehavior;
	}

	protected SessionAttachmentMode getDefaultSessionAttachmentMode() {
		return defaultSessionAttachmentMode;
	}

	public void setDefaultSessionAttachmentMode(SessionAttachmentMode defaultSessionAttachmentMode) {
		this.defaultSessionAttachmentMode = defaultSessionAttachmentMode;
	}

	protected Map<Class<?>, RowMapperMetaData<?>> getConfiguredClasses() {
		return configuredClasses;
	}

}
