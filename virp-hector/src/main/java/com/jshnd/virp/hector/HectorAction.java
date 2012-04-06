package com.jshnd.virp.hector;

import com.jshnd.virp.VirpAction;
import com.jshnd.virp.VirpException;
import com.jshnd.virp.config.RowMapperMetaData;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HectorAction<T> implements VirpAction {

	private static final Logger log = LoggerFactory.getLogger(HectorAction.class);

	private Mutator<T> mutator;

	@Override
	public void writeRow(Object row, RowMapperMetaData rowMeta) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public HectorActionResult complete() throws VirpException {
		try {
			MutationResult result = mutator.execute();
			if (log.isDebugEnabled()) {
				log.debug("Mutation in " + result.getExecutionTimeNano());
				log.debug("Host executed against: " + result.getHostUsed().getIp());
			}
			return new HectorActionResult(result);
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
