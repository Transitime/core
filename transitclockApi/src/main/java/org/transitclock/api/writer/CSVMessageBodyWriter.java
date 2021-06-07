package org.transitclock.api.writer;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

@Provider
@Produces("text/csv")
public class CSVMessageBodyWriter implements MessageBodyWriter<List> {

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        boolean ret=List.class.isAssignableFrom(type);
        return ret;
    }

    @Override
    public long getSize(List data, Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(List data, Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        if (data!=null && data.size()>0) {
            CsvMapper mapper = new CsvMapper();
            Object o=data.get(0);
            CsvSchema schema = mapper.schemaFor(o.getClass()).withHeader();
            mapper.writer(schema).writeValue(outputStream,data);
        }
    }
}