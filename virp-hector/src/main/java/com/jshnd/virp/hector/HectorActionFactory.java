package com.jshnd.virp.hector;

import com.jshnd.virp.VirpAction;
import com.jshnd.virp.VirpActionFactory;
import com.jshnd.virp.config.RowMapperMetaData;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

public class HectorActionFactory implements VirpActionFactory {

	private Keyspace keyspace;

	@Override
	public VirpAction newAction(RowMapperMetaData meta) {
		Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
		return new HectorAction<String>(mutator);
	}

	@Override
	public void setupClass(RowMapperMetaData type) {

	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
}
