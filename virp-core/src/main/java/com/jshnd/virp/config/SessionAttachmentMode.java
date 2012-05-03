package com.jshnd.virp.config;

public enum SessionAttachmentMode {
	/**
	 * Changes to an object retrieved from a session will never be persisted
	 */
	NONE(false, false),
	/**
	 * Changes to an object retrieved from a session will only be persisted when flush is called
	 */
	MANUAL_FLUSH(true, false),
	/**
	 * Changes to an object retrieved from a session will be persisted when the session is closed,
	 * or flush is called.
	 */
	AUTO_FLUSH(true, true);

	private final boolean attach;

	private final boolean autoFlush;

	private SessionAttachmentMode(boolean attach, boolean autoFlush) {
		this.attach = attach;
		this.autoFlush = autoFlush;
	}

	public boolean isAttach() {
		return attach;
	}

	public boolean isAutoFlush() {
		return autoFlush;
	}

}
