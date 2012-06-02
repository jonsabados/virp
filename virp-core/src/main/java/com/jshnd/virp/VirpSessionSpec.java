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

package com.jshnd.virp;

import com.jshnd.virp.config.NullColumnSaveBehavior;
import com.jshnd.virp.config.SessionAttachmentMode;

public class VirpSessionSpec {

	private SessionAttachmentMode sessionAttachmentMode;

	private NullColumnSaveBehavior nullColumnSaveBehavior;

	private boolean noColumnsEqualsNullRow;
	public VirpSessionSpec(VirpConfig config) {
		sessionAttachmentMode = config.getDefaultSessionAttachmentMode();
		nullColumnSaveBehavior = config.getNullColumnSaveBehavior();
		noColumnsEqualsNullRow = config.isNoColumnsEqualsNullRow();
	}

	public VirpSessionSpec(SessionAttachmentMode sessionAttachmentMode,
						   NullColumnSaveBehavior nullColumnSaveBehavior,
						   boolean noColumnsEqualsNullRow) {
		this.sessionAttachmentMode = sessionAttachmentMode;
		this.nullColumnSaveBehavior = nullColumnSaveBehavior;
		this.noColumnsEqualsNullRow = noColumnsEqualsNullRow;
	}

	public VirpSessionSpec withSessionAttachmentMode(SessionAttachmentMode sessionAttachmentMode) {
		return new VirpSessionSpec(sessionAttachmentMode, nullColumnSaveBehavior, noColumnsEqualsNullRow);
	}

	public VirpSessionSpec withNullColumnSaveBehavior(NullColumnSaveBehavior nullColumnSaveBehavior) {
		return new VirpSessionSpec(sessionAttachmentMode, nullColumnSaveBehavior, noColumnsEqualsNullRow);
	}

	public VirpSessionSpec withNoColumnEqualsNullRow(boolean noColumnsEqualsNullRow) {
		return new VirpSessionSpec(sessionAttachmentMode, nullColumnSaveBehavior, noColumnsEqualsNullRow);
	}

	public SessionAttachmentMode getSessionAttachmentMode() {
		return sessionAttachmentMode;
	}

	public NullColumnSaveBehavior getNullColumnSaveBehavior() {
		return nullColumnSaveBehavior;
	}

	public boolean isNoColumnsEqualsNullRow() {
		return noColumnsEqualsNullRow;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		VirpSessionSpec that = (VirpSessionSpec) o;

		if (noColumnsEqualsNullRow != that.noColumnsEqualsNullRow) return false;
		if (nullColumnSaveBehavior != that.nullColumnSaveBehavior) return false;
		if (sessionAttachmentMode != that.sessionAttachmentMode) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = sessionAttachmentMode != null ? sessionAttachmentMode.hashCode() : 0;
		result = 31 * result + (nullColumnSaveBehavior != null ? nullColumnSaveBehavior.hashCode() : 0);
		result = 31 * result + (noColumnsEqualsNullRow ? 1 : 0);
		return result;
	}
}
