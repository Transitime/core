package org.transitclock.api.data.reporting.prescriptive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.transitclock.api.data.reporting.ReportDataFormatter;
import org.transitclock.ipc.data.IpcPrescriptiveRunTime;
import org.transitclock.ipc.data.IpcPrescriptiveRunTimesForTimeBands;

import javax.json.JsonArray;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.out;


public class PrescriptiveRunTimeCsvOutput implements Serializable {

    public static StreamingOutput getCsv(String body){
        StreamingOutput entity = out -> {
            Writer writer = new BufferedWriter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8));
            csvWriter(body, writer);
            out.flush();
        };
        return entity;
    }

    private static void csvWriter(String body, Writer writer) throws IOException {
        String jsonOutput = body;

        JsonNode jsonTree = new ObjectMapper().readTree(jsonOutput);

        CsvMapper mapper = new CsvMapper();

        JsonNode tablesNode = jsonTree.get("runTimeTables");
        JsonNode routeNameNode = jsonTree.get("routeName");

        mapper.writer(CsvSchema.builder().build().withoutQuoteChar()).writeValues(writer).write(routeNameNode).write("\r");

        if(tablesNode.isArray()){
            for(JsonNode tableNode : tablesNode) {
                CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
                List<ArrayNode> rowValues = new ArrayList<>();

                JsonNode headersNode = tableNode.get("rowHeader");
                for (JsonNode header : headersNode) {
                    csvSchemaBuilder.addColumn(header.toString());
                }

                JsonNode rowValuesNode = tableNode.get("rowValues");
                for (JsonNode rowValueNode : rowValuesNode) {
                    rowValues.add((ArrayNode) rowValueNode);
                }

                CsvSchema schema = csvSchemaBuilder.build().withLineSeparator("\r").withoutQuoteChar().withHeader();
                mapper.writer(schema).writeValues(writer).writeAll(rowValues);
            }
        }

        writer.flush();
    }


}
