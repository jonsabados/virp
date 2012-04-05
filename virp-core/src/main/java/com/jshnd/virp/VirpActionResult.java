package com.jshnd.virp;

public interface VirpActionResult {

	public long getExecutionTimeInMicro();

	public long getExecutionTimeInNano();

	public String getNodeName();
}
