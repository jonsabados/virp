package com.jshnd.virp.exception;

public class VirpOperationException extends VirpException {

	private static final long serialVersionUID = 1L;

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
