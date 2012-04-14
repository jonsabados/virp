package com.jshnd.virp.reflection;

import com.jshnd.virp.annotation.NamedColumn;
import com.jshnd.virp.annotation.RowMapper;

@RowMapper(columnFamily = "SomeBean")
public class SomeBean {

	private String someProperty;

	@NamedColumn(name = "foo")
	private String columnProperty;

	private String methodProperty;

	public String getSomeProperty() {
		return someProperty;
	}

	public void setSomeProperty(String someProperty) {
		this.someProperty = someProperty;
	}

	@NamedColumn(name = "bar")
	public String getMethodProperty() {
		return methodProperty;
	}

	public void setMethodProperty(String methodProperty) {
		this.methodProperty = methodProperty;
	}

	public String getColumnProperty() {
		return columnProperty;
	}

	public void setColumnProperty(String columnProperty) {
		this.columnProperty = columnProperty;
	}

}
