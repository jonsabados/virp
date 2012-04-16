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
		Mutator mutator = HFactory.createMutator(keyspace, (Serializer) meta.getKeyValueAccessor().getActionFactoryMeta());
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
				accessor.setActionFactoryMeta(StringSerializer.get());
			} else if (type.isAssignableFrom(Integer.class)) {
				accessor.setActionFactoryMeta(IntegerSerializer.get());
			} else if (type.isAssignableFrom(Long.class)) {
				accessor.setActionFactoryMeta(LongSerializer.get());
			} else if (type.isAssignableFrom(Float.class)) {
				accessor.setActionFactoryMeta(FloatSerializer.get());
			} else if (type.isAssignableFrom(Double.class)) {
				accessor.setActionFactoryMeta(DoubleSerializer.get());
			} else if (type.isAssignableFrom(ShortSerializer.class)) {
				accessor.setActionFactoryMeta(ShortSerializer.get());
			} else if (type.isAssignableFrom(Boolean.class)) {
				accessor.setActionFactoryMeta(BooleanSerializer.get());
			} else {
				throw new VirpHectorException("Unable to deal with " + type.getCanonicalName() +
						", serializer needs setup.");
			}
		}
	}

	private void setupPrimitive(ValueAccessor<?> accessor, Class<?> type) {
		if (type.equals(long.class)) {
			accessor.setActionFactoryMeta(LongSerializer.get());
		} else if (type.equals(int.class)) {
			accessor.setActionFactoryMeta(IntegerSerializer.get());
		} else if (type.equals(short.class)) {
			accessor.setActionFactoryMeta(ShortSerializer.get());
		} else if (type.equals(byte.class)) {
			accessor.setActionFactoryMeta(ByteSerializer.get());
		} else if (type.equals(float.class)) {
			accessor.setActionFactoryMeta(FloatSerializer.get());
		} else if (type.equals(double.class)) {
			accessor.setActionFactoryMeta(DoubleSerializer.get());
		} else if (type.equals(boolean.class)) {
			accessor.setActionFactoryMeta(BooleanSerializer.get());
		} else if (type.equals(char.class)) {
			accessor.setActionFactoryMeta(CharSerializer.get());
		}
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
}
