package com.jshnd.virp.hector;

import com.jshnd.virp.VirpActionResult;
import me.prettyprint.hector.api.mutation.MutationResult;

public class HectorActionResult implements VirpActionResult {

	private MutationResult mutationResult;

	public HectorActionResult(MutationResult mutationResult) {
		this.mutationResult = mutationResult;
	}

	@Override
	public long getExecutionTimeInMicro() {
		return mutationResult.getExecutionTimeMicro();
	}

	@Override
	public long getExecutionTimeInNano() {
		return mutationResult.getExecutionTimeNano();
	}

	@Override
	public String getNodeName() {
		return mutationResult.getHostUsed().getName();
	}

	public MutationResult getMutationResult() {
		return mutationResult;
	}
}
