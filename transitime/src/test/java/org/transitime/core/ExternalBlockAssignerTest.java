package org.transitime.core;

import junit.framework.TestCase;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Trip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ExternalBlockAssignerTest extends TestCase {

    private static String FEED_1 = "block,vehicle\nblock_1,vehicle_1\nblock_2,vehicle_2\n";

    public void testGetFeed() throws Exception {
        ExternalBlockAssigner eba = ExternalBlockAssigner.getInstance();
        assertNull(eba.getBlockAssignmentsByVehicleIdFeed());
        System.setProperty("transitime.externalAssignerUrl", "file:///tmp/no_such_file.txt");
        eba.externalAssignerUrl.readValue();
        try {
            InputStream feed = eba.getBlockAssignmentsByVehicleIdFeed();
            fail();
        } catch (FileNotFoundException fnfe) {
        // good!
        }

        writeToTempFile(eba, FEED_1);

        InputStream feed = eba.getBlockAssignmentsByVehicleIdFeed();
        assertNotNull(feed);

        Map<String, ArrayList<String>> feedMap = eba.getBlockAssignmentsByVehicleIdMap();
        assertNotNull(feedMap);
        assertEquals(2, feedMap.size()); // force throwing away header
        assertTrue(feedMap.containsKey("vehicle_1"));
        assertEquals("block_1", feedMap.get("vehicle_1").get(0));
        assertTrue(feedMap.containsKey("vehicle_2"));
        assertEquals("block_2", feedMap.get("vehicle_2").get(0));


    }

    public void testGetActiveAssignmentForVehicle() throws Exception {

        ExternalBlockAssigner eba = new ExternalBlockAssigner() {
            @Override
            Block getActiveBlock(String assignmentId, Date serviceDate) {
                if (assignmentId != null && assignmentId.startsWith("block_") && serviceDate.getTime() > 0) {
                    Block b = new Block(-1,
                            assignmentId,
                            "serviceId",
                            (int)(System.currentTimeMillis()-1000)/1000,
                            (int)(System.currentTimeMillis()+1000)/1000,
                            new ArrayList<Trip>());
                    return b;
                }
                return null;
            }
        };

        writeToTempFile(eba, FEED_1);

        AvlReport avl = new AvlReport("vehicle_1", System.currentTimeMillis(), 0.0, 0.0,
        "OpenGTS");
        String blockId = eba.getActiveAssignmentForVehicle(avl);

        assertNotNull(blockId);
        assertEquals("block_1", blockId);

        avl = new AvlReport("vehicle_1", -1, 0.0, 0.0,
                "OpenGTS");
        blockId = eba.getActiveAssignmentForVehicle(avl);
        assertNull(blockId);

    }

    private void writeToFile(File file, String contents) throws Exception {
        PrintWriter out = new PrintWriter(file);
        out.write(contents);
        out.close();
    }

    private void writeToTempFile(ExternalBlockAssigner eba, String contents) throws Exception {
        File tmpFile = File.createTempFile("externalBlockAssiger", ".csv");
        tmpFile.deleteOnExit();
        writeToFile(tmpFile, FEED_1);
        System.setProperty("transitime.externalAssignerUrl", "file://" + tmpFile.getAbsolutePath());
        eba.externalAssignerUrl.readValue();

    }
}
