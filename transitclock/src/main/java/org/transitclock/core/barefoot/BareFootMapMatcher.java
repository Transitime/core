package org.transitclock.core.barefoot;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.configData.CoreConfig;
import org.transitclock.core.MapMatcher;
import org.transitclock.core.SpatialMatch;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.Block;
import org.transitclock.db.structs.Location;

import com.bmwcarit.barefoot.matcher.Matcher;
import com.bmwcarit.barefoot.matcher.MatcherCandidate;
import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.matcher.MatcherSample;
import com.bmwcarit.barefoot.roadmap.Road;
import com.bmwcarit.barefoot.roadmap.RoadMap;
import com.bmwcarit.barefoot.roadmap.RoadPoint;
import com.bmwcarit.barefoot.roadmap.TimePriority;
import com.bmwcarit.barefoot.spatial.Geography;
import com.bmwcarit.barefoot.spatial.SpatialOperator;
import com.bmwcarit.barefoot.topology.Dijkstra;
import com.esri.core.geometry.Point;
import org.transitclock.utils.Geo;
import org.transitclock.core.SpatialMatch;

public class BareFootMapMatcher implements MapMatcher {

    private RoadMap barefootMap = null;

    private Matcher barefootMatcher = null;

    private MatcherKState barefootState = null;

    private Block block = null;

    private int tripIndex = -1;

    private static SpatialOperator spatial = new Geography();

    private static final Logger logger = LoggerFactory.getLogger(BareFootMapMatcher.class);

    @Override
    public void setMatcher(Block block, Date assignmentTime) {

        if (block != null) {

            this.block = block;

            tripIndex = block.activeTripIndex(assignmentTime, 0);

            TransitClockRoadReader roadReader = new TransitClockRoadReader(block, tripIndex);

            barefootMap = RoadMap.Load(roadReader);

            barefootMap.construct();

            barefootMatcher = new Matcher(barefootMap, new Dijkstra<Road, RoadPoint>(), new TimePriority(),
                    new Geography());

            barefootMatcher.shortenTurns(false);

            barefootState = new MatcherKState();
        }
    }

    @Override
    public SpatialMatch getSpatialMatch(AvlReport avlReport) {

        if (barefootState != null) {
            Point point = new Point();
            point.setX(avlReport.getLon());
            point.setY(avlReport.getLat());
            MatcherSample sample = new MatcherSample(avlReport.getTime(), point);

            Set<MatcherCandidate> result = barefootMatcher.execute(barefootState.vector(), barefootState.sample(),
                    sample);

            barefootState.update(result, sample);

            MatcherCandidate estimate = barefootState.estimate();

            logger.debug("Vehicle {} has {} samples.", avlReport.getVehicleId(), barefootState.samples().size());

            if (estimate != null) {

                Location location = new Location(estimate.point().geometry().getY(),
                        estimate.point().geometry().getX());

                ReferenceId refId = ReferenceId.deconstructRefId(estimate.point().edge().base().refid());

                logger.debug(
                        "Vehicle {} assigned to {} is {} metres from GPS coordindates on {}. Probability is {} and Sequence probabilty is {}.",
                        avlReport.getVehicleId(), avlReport.getAssignmentId(),
                        Geo.distance(location, avlReport.getLocation()), refId, estimate.filtprob(),
                        estimate.seqprob());

                return new SpatialMatch(avlReport.getTime(), block, tripIndex, refId.getStopPathIndex(),
                        refId.getSegmentIndex(), 0, spatial.intercept(estimate.point().edge().geometry(), point)
                        * spatial.length(estimate.point().edge().geometry()), SpatialMatch.MatchType.BAREFOOT);

            }
        }
        return null;
    }

}