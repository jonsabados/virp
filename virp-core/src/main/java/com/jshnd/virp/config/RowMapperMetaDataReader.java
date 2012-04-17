package com.jshnd.virp.config;

public interface RowMapperMetaDataReader {

	public <T> RowMapperMetaData<T> readClass(Class<T> clazz);

}
