package org.transitclock.core.barefoot;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.configData.CoreConfig;
import org.transitclock.core.Indices;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Trip;
import org.transitclock.db.structs.VectorWithHeading;

import com.bmwcarit.barefoot.road.BaseRoad;
import com.bmwcarit.barefoot.road.RoadReader;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.util.SourceException;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

public class TransitClockRoadReader implements RoadReader {
    Trip trip = null;

    Indices indices = null;

    int tripIndex = -1;

    long segmentCounter = 0;
    private static final Logger logger =
            LoggerFactory.getLogger(TransitClockRoadReader.class);

    private static SpatialOperator spatial = new Geography();

    public TransitClockRoadReader(Block block, int tripIndex) {

        trip = block.getTrip(tripIndex);
        indices = new Indices(block, tripIndex, 0, 0);
    }

    @Override
    public BaseRoad next() throws SourceException {

        if (indices.atEndOfTrip())
            return null;

        Polyline polyLine = new Polyline();

        Point startPoint = null;
        Point endPoint = null;
        Line polyLineSegment = new Line();

        indices.increment(Core.getInstance().getSystemTime());

        VectorWithHeading segment = indices.getSegment();

        startPoint = new Point();
        endPoint = new Point();
        startPoint.setXY(segment.getL1().getLon(), segment.getL1().getLat());
        endPoint.setXY(segment.getL2().getLon(), segment.getL2().getLat());
        polyLineSegment.setStart(startPoint);
        polyLineSegment.setEnd(endPoint);

        polyLine.addSegment(polyLineSegment, false);

        ReferenceId refId=new ReferenceId(indices.getStopPathIndex(),indices.getSegmentIndex());

        return new BaseRoad(indices.hashCode(), segmentCounter, segmentCounter++, refId.getRefId(), true, (short) 1, 1F, 60F, 60F, (float)spatial.length(polyLine),
                polyLine);
    }



    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }



    @Override
    public void open() throws SourceException {
        // TODO Auto-generated method stub

    }



    @Override
    public void open(Polygon polygon, HashSet<Short> exclusion) throws SourceException {
        // TODO Auto-generated method stub

    }



    @Override
    public void close() throws SourceException {
        // TODO Auto-generated method stub

    }

}