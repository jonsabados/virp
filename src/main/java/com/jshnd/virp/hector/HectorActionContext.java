package com.jshnd.virp.hector;

import com.jshnd.virp.VirpActionContext;
import com.jshnd.virp.VirpException;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HectorActionContext<T> implements VirpActionContext {

	private static final Logger log = LoggerFactory.getLogger(HectorActionContext.class);

	private Mutator<T> mutator;

	@Override
	public void complete() throws VirpException {
		try {
			MutationResult result = mutator.execute();
			if (log.isDebugEnabled()) {
				log.debug("Mutation in " + result.getExecutionTimeNano());
				log.debug("Host executed against: " + result.getHostUsed().getIp());
			}
		} catch (Exception e) {
			throw new VirpException(e);
		}
	}

	public Mutator<T> getMutator() {
		return mutator;
	}

	public void setMutator(Mutator<T> mutator) {
		this.mutator = mutator;
	}
}
