package com.jshnd.virp.exception;

import com.jshnd.virp.exception.VirpException;

public class VirpOperationException extends VirpException {

	public VirpOperationException() {
	}

	public VirpOperationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public VirpOperationException(String arg0) {
		super(arg0);
	}

	public VirpOperationException(Throwable arg0) {
		super(arg0);
	}
}
