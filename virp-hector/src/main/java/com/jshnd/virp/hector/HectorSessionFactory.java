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

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.SessionFactoryDataHolder;
import com.jshnd.virp.VirpConfig;
import com.jshnd.virp.VirpSessionFactory;
import com.jshnd.virp.config.NullColumnSaveBehavior;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.SessionAttachmentMode;
import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.hector.api.Keyspace;

public class HectorSessionFactory implements VirpSessionFactory {

	private Keyspace keyspace;

	@Override
	public HectorSession newSession(VirpConfig config, SessionAttachmentMode attachmentMode, 
				NullColumnSaveBehavior nullBehavior) {
		return new HectorSession(config, keyspace, attachmentMode, nullBehavior);
	}

	@Override
	public void setupClass(RowMapperMetaData<?> type) {
		setupSerializer(type.getKeyValueManipulator());
		for (ColumnAccessor<?, ?> accessor : type.getColumnAccessors()) {
			setupSerializer(accessor.getValueManipulator());
			setupSerializer(accessor.getColumnIdentifier());
		}
	}

	private void setupSerializer(SessionFactoryDataHolder<?> accessor) {
		Class<?> type = accessor.getValueType();
		if (type.isPrimitive()) {
			setupPrimitive(accessor, type);
		} else {
			if (type.isAssignableFrom(String.class)) {
				accessor.setSessionFactoryData(StringSerializer.get());
			} else 	if (type.isAssignableFrom(Character.class)) {
				accessor.setSessionFactoryData(CharSerializer.get());
			} else if (type.isAssignableFrom(Long.class)) {
				accessor.setSessionFactoryData(LongSerializer.get());
			} else if (type.isAssignableFrom(Integer.class)) {
				accessor.setSessionFactoryData(IntegerSerializer.get());
			} else if (type.isAssignableFrom(Short.class)) {
				accessor.setSessionFactoryData(ShortSerializer.get());
			} else if (type.isAssignableFrom(Byte.class)) {
				accessor.setSessionFactoryData(ByteSerializer.get());
			} else if (type.isAssignableFrom(Float.class)) {
				accessor.setSessionFactoryData(FloatSerializer.get());
			} else if (type.isAssignableFrom(Double.class)) {
				accessor.setSessionFactoryData(DoubleSerializer.get());
			}else if (type.isAssignableFrom(Boolean.class)) {
				accessor.setSessionFactoryData(BooleanSerializer.get());
			} else {
				throw new VirpHectorException("Unable to deal with " + type.getCanonicalName() +
						", serializer needs setup.");
			}
		}
	}

	private void setupPrimitive(SessionFactoryDataHolder<?> accessor, Class<?> type) {
		if (type.equals(long.class)) {
			accessor.setSessionFactoryData(LongSerializer.get());
		} else if (type.equals(int.class)) {
			accessor.setSessionFactoryData(IntegerSerializer.get());
		} else if (type.equals(short.class)) {
			accessor.setSessionFactoryData(ShortSerializer.get());
		} else if (type.equals(byte.class)) {
			accessor.setSessionFactoryData(ByteSerializer.get());
		} else if (type.equals(float.class)) {
			accessor.setSessionFactoryData(FloatSerializer.get());
		} else if (type.equals(double.class)) {
			accessor.setSessionFactoryData(DoubleSerializer.get());
		} else if (type.equals(boolean.class)) {
			accessor.setSessionFactoryData(BooleanSerializer.get());
		} else if (type.equals(char.class)) {
			accessor.setSessionFactoryData(CharSerializer.get());
		}
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
}
