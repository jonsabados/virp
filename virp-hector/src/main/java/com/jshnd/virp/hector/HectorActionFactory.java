package com.jshnd.virp.hector;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueAccessor;
import com.jshnd.virp.VirpAction;
import com.jshnd.virp.VirpActionFactory;
import com.jshnd.virp.config.RowMapperMetaData;
import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

public class HectorActionFactory implements VirpActionFactory {

	private Keyspace keyspace;

	@Override
	@SuppressWarnings("unchecked")
	public VirpAction newAction(RowMapperMetaData meta) {
		Mutator mutator = HFactory.createMutator(keyspace, (Serializer) meta.getKeyValueAccessor().getMeta());
		return new HectorAction(mutator);
	}

	@Override
	public void setupClass(RowMapperMetaData type) {
		setupSerializer(type.getKeyValueAccessor());
		for (ColumnAccessor<?, ?> accessor : type.getColumnAccessors()) {
			setupSerializer(accessor.getValueAccessor());
			setupSerializer(accessor.getColumnIdentifier());
		}
	}

	private void setupSerializer(ValueAccessor<?> accessor) {
		Class<?> type = accessor.getValueType();
		if (type.isPrimitive()) {
			setupPrimitive(accessor, type);
		} else {
			if (type.isAssignableFrom(String.class)) {
				accessor.setMeta(StringSerializer.get());
			} else if (type.isAssignableFrom(Integer.class)) {
				accessor.setMeta(IntegerSerializer.get());
			} else if (type.isAssignableFrom(Long.class)) {
				accessor.setMeta(LongSerializer.get());
			} else if (type.isAssignableFrom(Float.class)) {
				accessor.setMeta(FloatSerializer.get());
			} else if (type.isAssignableFrom(Double.class)) {
				accessor.setMeta(DoubleSerializer.get());
			} else if (type.isAssignableFrom(ShortSerializer.class)) {
				accessor.setMeta(ShortSerializer.get());
			} else if (type.isAssignableFrom(Boolean.class)) {
				accessor.setMeta(BooleanSerializer.get());
			} else {
				throw new VirpHectorException("Unable to deal with " + type.getCanonicalName() +
						", serializer needs setup.");
			}
		}
	}

	private void setupPrimitive(ValueAccessor<?> accessor, Class<?> type) {
		if (type.equals(long.class)) {
			accessor.setMeta(LongSerializer.class);
		} else if (type.equals(int.class)) {
			accessor.setMeta(IntegerSerializer.class);
		} else if (type.equals(short.class)) {
			accessor.setMeta(ShortSerializer.class);
		} else if (type.equals(byte.class)) {
			accessor.setMeta(new ByteSerializer());
		} else if (type.equals(float.class)) {
			accessor.setMeta(FloatSerializer.class);
		} else if (type.equals(double.class)) {
			accessor.setMeta(DoubleSerializer.class);
		} else if (type.equals(boolean.class)) {
			accessor.setMeta(BooleanSerializer.class);
		} else if (type.equals(char.class)) {
			accessor.setMeta(new CharSerializer());
		}
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
}
