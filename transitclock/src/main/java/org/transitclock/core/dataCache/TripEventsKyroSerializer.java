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

public class TripEventsKyroSerializer implements Serializer<TripEvents> {

	private static final Kryo kryo = new Kryo();
	public TripEventsKyroSerializer(ClassLoader classLoader) {
	}
	public TripEventsKyroSerializer(ClassLoader classLoader, FileBasedPersistenceContext persistenceContext) {
	}
	@Override
	public boolean equals(TripEvents arg0, ByteBuffer arg1) throws ClassNotFoundException, SerializerException {
		 return arg0.equals(read(arg1));
	}

	@Override
	public TripEvents read(ByteBuffer arg0) throws ClassNotFoundException, SerializerException {
		Input input =  new Input(new ByteBufferInputStream(arg0)) ;
	    return kryo.readObject(input, TripEvents.class);
	}

	@Override
	public ByteBuffer serialize(TripEvents arg0) throws SerializerException {
		Output output = new Output(new ByteArrayOutputStream());
		kryo.writeObject(output, arg0);
		output.close();
		return ByteBuffer.wrap(output.getBuffer());
	}

}
