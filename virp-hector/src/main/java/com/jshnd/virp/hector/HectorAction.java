package com.jshnd.virp.hector;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.VirpAction;
import com.jshnd.virp.VirpException;
import com.jshnd.virp.config.RowMapperMetaData;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HectorAction<T> implements VirpAction {

	private static final Logger log = LoggerFactory.getLogger(HectorAction.class);

	private Mutator<T> mutator;

	public HectorAction(Mutator<T> mutator) {
		this.mutator = mutator;
	}

	@Override
	public void writeRow(Object row, RowMapperMetaData rowMeta) {
		String columnFamily = rowMeta.getColumnFamily();
		ValueAccessor<T> keyAccessor = (ValueAccessor<T>) rowMeta.getKeyValueAccessor();
		T key = keyAccessor.getValue(row);
		for(ColumnAccessor accessor : rowMeta.getColumnAccessors()) {
			ValueAccessor identifier = accessor.getColumnIdentifier();
			ValueAccessor value = accessor.getValueAccessor();
			HColumn hcolumn = HFactory.createColumn(identifier.getValue(row), value.getValue(row),
					(Serializer) identifier.getMeta(), (Serializer) value.getMeta());
			mutator.addInsertion(key, columnFamily, hcolumn);
		}
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
