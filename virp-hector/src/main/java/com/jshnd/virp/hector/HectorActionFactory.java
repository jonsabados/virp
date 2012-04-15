package com.jshnd.virp.hector;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.VirpAction;
import com.jshnd.virp.VirpActionFactory;
import com.jshnd.virp.config.RowMapperMetaData;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

public class HectorActionFactory implements VirpActionFactory {

	private Keyspace keyspace;

	@Override
	public VirpAction newAction(RowMapperMetaData meta) {
		Mutator mutator = HFactory.createMutator(keyspace, (Serializer) meta.getKeyValueAccessor().meta());
		return new HectorAction(mutator);
	}

	@Override
	public void setupClass(RowMapperMetaData type) {
		setupSerializer(type.getKeyValueAccessor());
		for(ColumnAccessor<?, ?> accessor : type.getColumnAccessors()) {
			setupSerializer(accessor.getValueAccessor());
			setupSerializer(accessor.getColumnIdentifier());
		}
	}

	private void setupSerializer(ValueAccessor<?> accessor) {
		Class<?> type = accessor.getValueType();
		if(type.isAssignableFrom(String.class)) {
			accessor.setMeta(StringSerializer.get());
		} else if(type.isAssignableFrom(Long.class)) {
			accessor.setMeta(LongSerializer.get());
		} else if(type.isAssignableFrom(long.class)) {
			accessor.setMeta(LongSerializer.get());
		}
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
}
