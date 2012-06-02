/*
 * Copyright 2012 Jonathan Sabados
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jshnd.virp.hector;

import com.jshnd.virp.*;
import com.jshnd.virp.annotation.TimeToLive;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpException;
import com.jshnd.virp.query.Query;
import com.jshnd.virp.query.QueryParameter;
import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ResultStatus;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.*;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.MutationResult;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HectorSession extends VirpSession {

	private static final Logger log = LoggerFactory.getLogger(HectorSession.class);

	private Mutator<byte[]> mutator;

	private Keyspace keyspace;

	public HectorSession(VirpConfig config, Keyspace keyspace, VirpSessionSpec sessionSpec) {
		super(config, sessionSpec);
		this.keyspace = keyspace;
		this.mutator = HFactory.createMutator(keyspace, BytesArraySerializer.get());
	}

	@Override
	protected <T> void doSave(RowMapperMetaData<T> type, T row) {
		addColumns(type, row, type.getColumnAccessors());
	}

	@Override
	protected <T> void doDelete(RowMapperMetaData<T> type, T row) {
		ValueManipulator<Object> keyManipulator = type.getKeyValueManipulator();
		Serializer<Object> keySerializer = (Serializer<Object>) keyManipulator.getSessionFactoryData();
		mutator.addDeletion(keySerializer.toBytes(keyManipulator.getValue(row)), type.getColumnFamily());
	}

	@Override
	protected <T> void doChange(RowMapperMetaData<T> type, T row, ColumnAccessor<?, ?> accessor) {
		Set<ColumnAccessor<?, ?>> accessors = new HashSet<ColumnAccessor<?, ?>>(1);
		accessors.add(accessor);
		addColumns(type, row, accessors);
	}

	private <T> void addColumns(RowMapperMetaData<T> type, T row, Set<ColumnAccessor<?, ?>> accessors) {
		String columnFamily = type.getColumnFamily();
		ValueAccessor<?> keyAccessor = type.getKeyValueManipulator();
		@SuppressWarnings("unchecked")
		Serializer<Object> keySerializer = (Serializer<Object>) keyAccessor.getSessionFactoryData();
		byte[] key = keySerializer.toBytes(keyAccessor.getValue(row));
		for(ColumnAccessor<?, ?> accessor : accessors) {
			StaticValueAccessor<?> identifier = accessor.getColumnIdentifier();
			ValueAccessor<?> valueAccessor = accessor.getValueManipulator();
			Object value  = valueAccessor.getValue(row);
			if(value == null) {
				switch(sessionSpec.getNullColumnSaveBehavior()) {
					case DO_NOTHING:
						break;
					case EMPTY_BYTE_ARRAY:
						addColumn(row, columnFamily, key, accessor, identifier, new byte[0], 
								BytesArraySerializer.get());
						break;
					case NO_COLUMN:
					@SuppressWarnings("unchecked")
					Serializer<Object> identifierSerializer = (Serializer<Object>) identifier
							.getSessionFactoryData();
					mutator.addDeletion(key, columnFamily,
							identifier.getValue(), identifierSerializer);
					break;
				default:
						throw new IllegalStateException("Please implement the new behavior.");
				}
			} else {
				@SuppressWarnings("unchecked")
				Serializer<Object> valueSerializer = (Serializer<Object>) valueAccessor.getSessionFactoryData();
				addColumn(row, columnFamily, key, accessor, identifier, value, valueSerializer);
			}
		}
	}

	private <T, V> void addColumn(T row, String columnFamily, byte[] key,
								ColumnAccessor<?, ?> accessor, StaticValueAccessor<?> identifier,
								V value, Serializer<V> valueSerializer) {
		@SuppressWarnings("unchecked")
		HColumn<?, ?> hcolumn = HFactory.createColumn(identifier.getValue(), value,
				(Serializer<Object>) identifier.getSessionFactoryData(), valueSerializer);
		int ttl = accessor.getTimeToLive().getValue(row).intValue();
		if(ttl != TimeToLive.NONE) {
			hcolumn.setTtl(ttl);
		}
		mutator.addInsertion(key, columnFamily, hcolumn);
	}

	@Override
	protected <T, K> T doGet(RowMapperMetaData<T> type, K key) {
		BytesArraySerializer serializer = BytesArraySerializer.get();
		SliceQuery<byte[], byte[], byte[]> query =
				HFactory.createSliceQuery(keyspace, serializer, serializer, serializer);

		TypeBits<T, K> typeBits = new TypeBits<T, K>(type);
		query.setKey(typeBits.keySerializer.toBytes(key));

		query.setColumnFamily(type.getColumnFamily());
		query.setColumnNames(typeBits.columns);

		QueryResult<ColumnSlice<byte[], byte[]>> result = query.execute();

		logQuery(type, result, key);

		ColumnSlice<byte[], byte[]> slice = result.get();
		T ret = fromSlice(type, typeBits, slice);

		if(ret != null) {
			typeBits.keyManipulator.setValue(ret, key);
		}
		return ret;
	}

	@Override
	protected <T, K> List<T> doGet(RowMapperMetaData<T> type, K... keys) {
		List<T> ret = new ArrayList<T>();
		BytesArraySerializer serializer = BytesArraySerializer.get();
		MultigetSliceQuery<byte[], byte[], byte[]> query =
				HFactory.createMultigetSliceQuery(keyspace, serializer, serializer, serializer);

		TypeBits<T, K> typeBits = new TypeBits<T, K>(type);

		byte[][] keyBits = new byte[keys.length][];
		for(int i = 0; i < keys.length; i++) {
			keyBits[i]  = typeBits.keySerializer.toBytes(keys[i]);
		}

		query.setKeys(keyBits);
		query.setColumnFamily(type.getColumnFamily());
		query.setColumnNames(typeBits.columns);

		QueryResult<Rows<byte[], byte[], byte[]>> result = query.execute();

		logQuery(type, result, keys);

		return rowsToList(type, ret, typeBits, result);
	}

	@Override
	protected <T> List<T> doFind(Query<T> virpQuery, RowMapperMetaData<T> type) {
		List<T> ret = new ArrayList<T>();
		BytesArraySerializer serializer = BytesArraySerializer.get();
		IndexedSlicesQuery<byte[], byte[], byte[]> query =
				HFactory.createIndexedSlicesQuery(keyspace, serializer, serializer, serializer);

		TypeBits<T, Object> typeBits = new TypeBits<T, Object>(type);

		for(QueryParameter<?, ?> param : virpQuery.getParameters()) {
			addParameter(query, param);
		}
		query.setColumnFamily(type.getColumnFamily());
		query.setColumnNames(typeBits.columns);

		try {
			QueryResult<OrderedRows<byte[], byte[], byte[]>> result = query.execute();
			logQuery(type, result);

			return rowsToList(type, ret, typeBits, result);
		} catch(Exception e) {
			throw new VirpHectorException(e);
		}
	}

	private void addParameter(IndexedSlicesQuery<byte[], byte[], byte[]> query, QueryParameter<?, ?> param) {
		Serializer<Object> nameSerializer = (Serializer<Object>) param.getColumnIdentifier().getSessionFactoryData();
		Serializer<Object> valueSerializer = (Serializer<Object>) param.getSessionFactoryData().getSessionFactoryData();
		byte[] column = nameSerializer.toBytes(param.getColumnIdentifier().getValue());
		byte[] value = valueSerializer.toBytes(param.getArgument());
		switch (param.getCriteria()) {
			case EQUAL:
				query.addEqualsExpression(column, value);
				break;
			case GREATER:
				query.addGtExpression(column, value);
				break;
			case GREATER_OR_EQUAL:
				query.addGteExpression(column, value);
				break;
			case LESSER:
				query.addLtExpression(column, value);
				break;
			case LESSER_OR_EQUAL:
				query.addLteExpression(column, value);
				break;
			default:
				throw new VirpHectorException("Someone added a new criteria type... please fix me");
		}
	}

	private <T, K> List<T> rowsToList(RowMapperMetaData<T> type, List<T> ret, TypeBits<T, K> typeBits,
								   QueryResult<? extends Rows<byte[], byte[], byte[]>> result) {
		for (Row<byte[], byte[], byte[]> row : result.get()) {
			T object = fromSlice(type, typeBits, row.getColumnSlice());
			if (object != null) {
				typeBits.keyManipulator.setValue(object, typeBits.keySerializer.fromBytes(row.getKey()));
				ret.add(object);
			}
		}
		return ret;
	}

	private void logQuery(RowMapperMetaData<?> type, ResultStatus result, Object... keys) {
		if(log.isTraceEnabled()) {
			StringBuilder keyString = new StringBuilder();
			for(int i = 0; i < keys.length; i++) {
				keyString.append(keys[i]);
				if(i < keys.length - 1) {
					keyString.append(",");
				}
			}
			log.trace("Query for type " + type.getClass().getCanonicalName() + ", keys: " + keyString
					+ " executed against " + result.getHostUsed().getName() + ", took "
					+ result.getExecutionTimeMicro() + " micro");
		}
	}

	private <T, K> T fromSlice(RowMapperMetaData<T> type, TypeBits<T, K> typeBits, ColumnSlice<byte[], byte[]> slice) {
		if(slice.getColumns().size() == 0 && sessionSpec.isNoColumnsEqualsNullRow()) {
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
						ValueManipulator<Object> manipulator = typeBits.manipulators[j];
						Serializer<Object> valueSerializer = (Serializer) manipulator.getSessionFactoryData();
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
	protected void doClose() {
		
	}

	@Override
	public HectorActionResult doFlush() throws VirpException {
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

	private static class TypeBits<T, K> {
		private byte[][] columns;
		private ValueManipulator[] manipulators;
		private int columnCount;
		ValueManipulator<K> keyManipulator;
		Serializer<K> keySerializer;

		TypeBits(RowMapperMetaData<T> type) {
			Set<ColumnAccessor<?,?>> accessors = type.getColumnAccessors();
			keyManipulator = (ValueManipulator<K>) type.getKeyValueManipulator();
			keySerializer = (Serializer<K>) keyManipulator.getSessionFactoryData();
			columns = new byte[accessors.size()][];
			manipulators = new ValueManipulator[accessors.size()];
			columnCount = 0;
			for(ColumnAccessor<?,?> accessor : type.getColumnAccessors()) {
				StaticValueAccessor<?> identifier = accessor.getColumnIdentifier();
				Serializer identifierSerializer = (Serializer) identifier.getSessionFactoryData();
				columns[columnCount] = identifierSerializer.toBytes(identifier.getValue());
				manipulators[columnCount] = accessor.getValueManipulator();
				columnCount++;
			}
		}

	}
}
