package com.jshnd.virp.hector;

import me.prettyprint.cassandra.serializers.AbstractSerializer;

import java.nio.ByteBuffer;

public class ByteSerializer extends AbstractSerializer<Byte> {

	private static final ByteSerializer instance = new ByteSerializer();

	public static ByteSerializer get() {
		return instance;
	}

	@Override
	public ByteBuffer toByteBuffer(Byte obj) {
		if(obj == null) {
			return null;
		}
		return ByteBuffer.wrap(new byte[] {obj.byteValue()});
	}

	@Override
	public Byte fromByteBuffer(ByteBuffer byteBuffer) {
		if(byteBuffer == null) {
			return null;
		}
		return Byte.valueOf(byteBuffer.get());
	}
}
