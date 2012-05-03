package com.jshnd.virp;

public interface VirpActionResult {

	public static VirpActionResult NONE = new VirpActionResult() {
		@Override
		public long getExecutionTimeInMicro() {
			return 0;
		}

		@Override
		public long getExecutionTimeInNano() {
			return 0;
		}

		@Override
		public String getNodeName() {
			return null;
		}
	};

	public long getExecutionTimeInMicro();

	public long getExecutionTimeInNano();

	public String getNodeName();
}
