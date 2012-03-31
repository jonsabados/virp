package com.jshnd.casrom.reflection;

import com.jshnd.casrom.annotation.Column;

public class SomeBean {

	private String someProperty;

	@Column(name = "foo")
	private String columnProperty;
	
	private String methodProperty;
	
	public String getSomeProperty() {
		return someProperty;
	}

	public void setSomeProperty(String someProperty) {
		this.someProperty = someProperty;
	}

	@Column(name = "bar")
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
