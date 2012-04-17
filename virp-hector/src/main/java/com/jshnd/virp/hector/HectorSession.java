package com.jshnd.virp.hector;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.VirpSession;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpException;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HectorSession<T> extends VirpSession {

	private static final Logger log = LoggerFactory.getLogger(HectorSession.class);

	private Mutator<T> mutator;

	public HectorSession(RowMapperMetaData rowMeta, Mutator<T> mutator) {
		super(rowMeta);
		this.mutator = mutator;
	}

	@Override
	protected void doSave(Object row) {
		String columnFamily = rowMeta.getColumnFamily();
		ValueAccessor<T> keyAccessor = (ValueAccessor<T>) rowMeta.getKeyValueAccessor();
		T key = keyAccessor.getValue(row);
		for(ColumnAccessor accessor : rowMeta.getColumnAccessors()) {
			ValueAccessor identifier = accessor.getColumnIdentifier();
			ValueAccessor value = accessor.getValueAccessor();
			HColumn hcolumn = HFactory.createColumn(identifier.getValue(row), value.getValue(row),
					(Serializer) identifier.getActionFactoryMeta(), (Serializer) value.getActionFactoryMeta());
			mutator.addInsertion(key, columnFamily, hcolumn);
		}
	}

	@Override
	public HectorActionResult doClose() throws VirpException {
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

}
