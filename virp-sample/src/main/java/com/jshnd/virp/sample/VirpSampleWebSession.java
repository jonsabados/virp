package com.jshnd.virp.sample;

import com.jshnd.virp.sample.model.VirpUser;

public class VirpSampleWebSession {

	private VirpUser currentUser;

	public VirpUser getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(VirpUser currentUser) {
		this.currentUser = currentUser;
	}
	
}
