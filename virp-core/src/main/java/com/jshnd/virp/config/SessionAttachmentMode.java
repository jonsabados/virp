/*
 * Copyright 2012 Jonathan Sabados
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
