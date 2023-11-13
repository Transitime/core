package org.transitclock.core;

import java.util.Date;

import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Location;

public interface MapMatcher {
    void setMatcher(Block block,  Date assignmentTime);
    SpatialMatch getSpatialMatch(AvlReport avlReport);
}