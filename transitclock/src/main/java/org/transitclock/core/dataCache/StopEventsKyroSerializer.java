package org.transitclock.core.dataCache;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.ehcache.core.spi.service.FileBasedPersistenceContext;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class StopEventsKyroSerializer implements Serializer<StopEvents> {

	private static final Kryo kryo = new Kryo();
	public StopEventsKyroSerializer(ClassLoader classLoader) {
	}
	public StopEventsKyroSerializer(ClassLoader classLoader, FileBasedPersistenceContext persistenceContext) {
	}
	@Override
	public boolean equals(StopEvents arg0, ByteBuffer arg1) throws ClassNotFoundException, SerializerException {
		 return arg0.equals(read(arg1));
	}

	@Override
	public StopEvents read(ByteBuffer arg0) throws ClassNotFoundException, SerializerException {
		Input input =  new Input(new ByteBufferInputStream(arg0)) ;
	    return kryo.readObject(input, StopEvents.class);
	}

	@Override
	public ByteBuffer serialize(StopEvents arg0) throws SerializerException {
		Output output = new Output(new ByteArrayOutputStream());
		kryo.writeObject(output, arg0);
		output.close();
		return ByteBuffer.wrap(output.getBuffer());
	}

}
