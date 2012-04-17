package com.jshnd.virp.exception;

public class VirpException extends RuntimeException {

	private static final long serialVersionUID = -925919941477407922L;

	public VirpException() {
		super();
	}

	public VirpException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public VirpException(String arg0) {
		super(arg0);
	}

	public VirpException(Throwable arg0) {
		super(arg0);
	}

}
