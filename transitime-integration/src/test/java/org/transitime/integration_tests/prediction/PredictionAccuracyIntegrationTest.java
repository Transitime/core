package org.transitime.integration_tests.prediction;

import junit.framework.TestCase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.List;

/**
 * This integration test buids an entirely new transitime DB from GTFS files, prepares the DB for the app to run
 * imports a CSV file of avl test data, waits for predictions to be generated, then checks the output against
 * a csv file of expected prediction values.
 *
 * Because this test case invokes main classes that in turn spawn their own threads, there's no good way to tell
 * when the import processes have finished, and polling the HSQL database for record counts can cause deadlocks,
 * so the hackish solution is to poll for system property changes.
 *
 * Because of problems with HSQL, we are unzipping a stored gtfs database rather than building it from scratch
 * during the test
 */
public class PredictionAccuracyIntegrationTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(PredictionAccuracyIntegrationTest.class);

    @Test
    public static void test() {
        try {
            /*logger.info("invoking GtfsFileProcessor");
            String commandLineArgs = "-c src/test/resources/transiTimeConfigIntegrationTest.xml -gtfsDirectoryName src/test/resources/wmata_gtfs -storeNewRevs -maxTravelTimeSegmentLength 1000";
            start("org.transitime.applications.GtfsFileProcessor", commandLineArgs.split(" "));
            logger.info("setting up database");
            setupDatabase();*/
            System.setProperty("transitime.core.integrationTest","true");
            System.setProperty("transitime.core.agencyId","1");
            System.setProperty("transitime.modules.optionalModulesList","org.transitime.avl.BatchCsvAvlFeedModule");
            System.setProperty("transitime.avl.csvAvlFeedFileName","src/test/resources/avltest.csv");
            System.setProperty("transitime.configFiles","src/test/resources/transiTimeConfigIntegrationTest.xml");
            System.setProperty("transitime.core.maxPredictionTimeForDbSecs", "3600");
            start("org.transitime.applications.Core", new String[]{});
            checkPredictions();
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }

    public static void checkPredictions() throws Exception {

        while(true) {
            String csvImported = System.getProperty("transitime.core.csvImported");
            if (csvImported != null) {
                logger.info("avl data imported, checking prediction results");
                Class.forName("org.hsqldb.jdbcDriver");
                Connection connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/transitime_test", "sa", "");

                FileReader fileReader = new FileReader("src/test/resources/avltest_values.csv");
                CSVParser csvFileParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
                List<CSVRecord> csvRecords = csvFileParser.getRecords();

                for(CSVRecord csvRecord : csvRecords){
                    csvRecord.get(0);

                    PreparedStatement pstmt = connection.prepareStatement("select predictiontime from predictions where routeid = ? " +
                            "AND stopid = ? " +
                            "AND tripid = ? " +
                            "AND vehicleid = ? " +
                            "AND gtfsstopseq = ? " +
                            "AND avltime = ? ");

                    String avlTimestampString = csvRecord.get(5);
                    String predictionTimeString = csvRecord.get(6);
                    pstmt.setInt(1, Integer.parseInt(csvRecord.get(0)));
                    pstmt.setInt(2, Integer.parseInt(csvRecord.get(1)));
                    pstmt.setInt(3, Integer.parseInt(csvRecord.get(2)));
                    pstmt.setInt(4, Integer.parseInt(csvRecord.get(3)));
                    pstmt.setInt(5, Integer.parseInt(csvRecord.get(4)));
                    pstmt.setString(6, avlTimestampString);

                    ResultSet rs = pstmt.executeQuery();
                    if(rs.next()){
                        String actualPredictionString = rs.getString(1);
                        logger.info("Checking prediction for record " + csvRecord.toString());
                        assertEquals(actualPredictionString, predictionTimeString);
                    }else{
                        throw new AssertionError("No prediction found for " + csvRecord.toString());
                    }
                }
                logger.info("all predictions are correct");
                break;
            }
        }
    }

    public static void setupDatabase() throws Exception {
        while(true){
            String gtfsImported = System.getProperty("transitime.core.gtfsImported");
            if(gtfsImported != null){
                Class.forName("org.hsqldb.jdbcDriver");
                Connection connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/transitime_test", "sa", "");

                logger.info("Connected to local hsql DB");

                Statement statement = connection.createStatement();
                statement.execute("delete from apikeys");
                statement.execute("delete from WebAgencies");
                statement.executeQuery("insert into ApiKeys values ('test', '8a3273b0', 'http://localhost:8080/transtime', 'test', 'test@example.com', '12345678');");
                statement.executeQuery("insert into WebAgencies values ('1', 1, 'BQ3GfXcRNvxnM2K5LtOVQqlJwWkdrDeg', 'localhost', 'transitime', 'mysql', 'root', 'localhost');");
                statement.close();
                connection.close();

                logger.info("local hsql DB updated");
                break;
            }
        }
    }

    public static void start(final String classname, final String...params) throws Exception {

        final Class<?> clazz = Class.forName(classname);
        final Method main = clazz.getMethod("main", String[].class);

        new Thread() {
            
            public void run() {
                try {
                    main.invoke(null, new Object[]{params});
                } catch(Exception e) {
                    throw new AssertionError(e);
                }
            }
        }.start();
    }
}
