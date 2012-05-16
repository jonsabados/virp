package com.jshnd.virp.sample.model;

import com.jshnd.virp.annotation.Key;
import com.jshnd.virp.annotation.NamedColumn;
import com.jshnd.virp.annotation.RowMapper;

@RowMapper(columnFamily = "VirpUser")
public class VirpUser {

	@Key
	private String email;
	
	@NamedColumn(name = "firstName")
	private String firstName;
	
	@NamedColumn(name = "lastName")
	private String lastName;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
}
