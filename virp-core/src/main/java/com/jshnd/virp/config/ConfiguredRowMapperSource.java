package com.jshnd.virp.config;

import java.util.Set;

public class ConfiguredRowMapperSource implements RowMapperSource {

	private Set<Class<?>> rowMapperClasses;

	@Override
	public Set<Class<?>> getRowMapperClasses() {
		return rowMapperClasses;
	}

	public void setRowMapperClasses(Set<Class<?>> rowMapperClasses) {
		this.rowMapperClasses = rowMapperClasses;
	}

}
