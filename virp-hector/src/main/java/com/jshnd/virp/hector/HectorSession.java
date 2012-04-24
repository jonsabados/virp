package com.jshnd.virp.hector;

import com.jshnd.virp.*;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpException;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class HectorSession extends VirpSession {

	private static final Logger log = LoggerFactory.getLogger(HectorSession.class);

	private Mutator<byte[]> mutator;

	private Keyspace keyspace;

	public HectorSession(VirpConfig config, Mutator<byte[]> mutator, Keyspace keyspace) {
		super(config);
		this.mutator = mutator;
		this.keyspace = keyspace;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> void doSave(RowMapperMetaData<T> type, T row) {
		String columnFamily = type.getColumnFamily();
		ValueAccessor<?> keyAccessor = type.getKeyValueManipulator();
		Serializer keySerializer = (Serializer) keyAccessor.getSessionFactoryData();
		byte[] key = keySerializer.toBytes(keyAccessor.getValue(row));
		Set<ColumnAccessor<?,?>> accessors = type.getColumnAccessors();
		for(ColumnAccessor<?, ?> accessor : accessors) {
			StaticValueAccessor<?> identifier = accessor.getColumnIdentifier();
			ValueAccessor<?> value = accessor.getValueManipulator();
			HColumn hcolumn = HFactory.createColumn(identifier.getValue(), value.getValue(row),
					(Serializer) identifier.getSessionFactoryData(), (Serializer) value.getSessionFactoryData());
			mutator.addInsertion(key, columnFamily, hcolumn);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> T doGet(RowMapperMetaData<T> type, Object key) {
		BytesArraySerializer serializer = BytesArraySerializer.get();
		SliceQuery<byte[], byte[], byte[]> query =
				HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);

		TypeBits typeBits = new TypeBits<T>(type);
		query.setKey(typeBits.keySerializer.toBytes(key));

		query.setColumnFamily(type.getColumnFamily());
		query.setColumnNames(typeBits.columns);

		QueryResult<ColumnSlice<byte[], byte[]>> result = query.execute();
		if(log.isTraceEnabled()) {
			log.trace("Query for type " + type.getClass().getCanonicalName() + ", key " + key
					+ " executed against " + result.getHostUsed().getName() + ", took "
					+ result.getExecutionTimeMicro() + " micro");
		}
		ColumnSlice<byte[], byte[]> slice = result.get();
		T ret = fromSlice(type, typeBits, slice);

		if(ret != null) {
			typeBits.keyManipulator.setValue(ret, key);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromSlice(RowMapperMetaData<T> type, TypeBits typeBits, ColumnSlice<byte[], byte[]> slice) {
		if(slice.getColumns().size() == 0 && config.isNoColumnsEqualsNullRow()) {
			return null;
		}
		T ret;
		try {
			ret = type.getRowMapperClass().newInstance();
		} catch (Exception e) {
			throw new VirpHectorException("Unable to initialize class of type " + type.getClass().getCanonicalName());
		}
		for (int i = 0; i < slice.getColumns().size(); i++) {
			HColumn<byte[], byte[]> column = slice.getColumns().get(i);
			if (column != null) {
				for (int j = 0; j < typeBits.columnCount; j++) {
					if (equal(column.getName(), typeBits.columns[j])) {
						ValueManipulator manipulator = typeBits.manipulators[j];
						Serializer valueSerializer = (Serializer) manipulator.getSessionFactoryData();
						Object value = valueSerializer.fromBytes(column.getValue());
						manipulator.setValue(ret, value);
					}
				}
			}
		}
		return ret;
	}

	private boolean equal(byte[] one, byte[] two) {
		if(one.length != two.length) {
			return false;
		}
		for(int i = 0; i < one.length; i++) {
			if(one[i] != two[i]) {
				return false;
			}
		}
		return true;
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

	private static class TypeBits<T> {
		private byte[][] columns;
		private ValueManipulator[] manipulators;
		private int columnCount;
		ValueManipulator keyManipulator;
		Serializer keySerializer;

		@SuppressWarnings("unchecked")
		TypeBits(RowMapperMetaData<T> type) {
			Set<ColumnAccessor<?,?>> accessors = type.getColumnAccessors();
			keyManipulator = type.getKeyValueManipulator();
			keySerializer = (Serializer) keyManipulator.getSessionFactoryData();
			columns = new byte[accessors.size()][];
			manipulators = new ValueManipulator[accessors.size()];
			columnCount = 0;
			for(ColumnAccessor<?,?> accessor : type.getColumnAccessors()) {
				StaticValueAccessor identifier = accessor.getColumnIdentifier();
				Serializer identifierSerializer = (Serializer) identifier.getSessionFactoryData();
				columns[columnCount] = identifierSerializer.toBytes(identifier.getValue());
				manipulators[columnCount] = accessor.getValueManipulator();
				columnCount++;
			}
		}

	}
}
