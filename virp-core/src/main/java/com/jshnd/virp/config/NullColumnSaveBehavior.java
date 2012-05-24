package com.jshnd.virp.config;

import com.jshnd.virp.VirpConfig;

/**
 * Enum for the behavior when saving a row with a null value in a property mapped to a column.
 * @see {@link VirpConfig#setNullColumnSaveBehavior(NullColumnSaveBehavior)}
 */
public enum NullColumnSaveBehavior {

	/**
	 * No column will be added - if the column already exists for the key it will be removed.
	 */
	NO_COLUMN,
	/**
	 * No action will be taken - if the column already exists for the key it will be left as is.
	 * Potentially useful if you want to update a single column of a row &amp; you don't care
	 * what else might be in it.
	 */
	DO_NOTHING,
	/**
	 * Warning - HERE BE DRAGONS!!! Use with caution, as an empty byte array be inserted. For some
	 * things like numbers this works for write and read, however for other things (strings) an
	 * empty byte array translates into an empty string so you will save null, but read empty. This
	 * may or may not live on in future releases.
	 */
	EMPTY_BYTE_ARRAY,
	
}
