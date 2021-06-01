package org.transitclock.reporting;

import org.junit.Assert;
import org.junit.Test;

public class MedianTest {

    @Test
    public void testMedian(){
        DoubleStatistics medianStatistics = new DoubleStatistics();
        medianStatistics.add(1);
        medianStatistics.add(7);
        medianStatistics.add(5);

        Assert.assertEquals(3, medianStatistics.getCount());
        Assert.assertEquals(5, medianStatistics.getMedian(), 0);
    }
}
