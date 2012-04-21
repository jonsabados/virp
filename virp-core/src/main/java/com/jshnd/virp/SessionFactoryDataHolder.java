package com.jshnd.virp;

public interface SessionFactoryDataHolder<T> extends ValueTypeHolder<T> {

	public Object getSessionFactoryData();

	public void setSessionFactoryData(Object sessionFactoryData);

}
