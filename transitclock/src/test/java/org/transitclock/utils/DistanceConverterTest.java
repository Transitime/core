package org.transitclock.utils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DistanceConverterTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    // Test to make sure String label matches to enum
    @Test
    public void distanceTypeLabelTest(){
        DistanceType distanceType = DistanceType.valueOfLabel("km");
        Assert.assertEquals(DistanceType.KM, distanceType);
    }

    @Test
    public void distanceTypeUnknownLabel(){
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("No enum constant with label badValue");
        DistanceType.valueOfLabel("badValue");
    }

    @Test
    public void kmToMetersEmptyValueTest(){
        DistanceType distanceType = DistanceType.valueOfLabel("km");
        Assert.assertNull(distanceType.convertDistanceToMeters(null));
    }

    @Test
    public void kmToMetersTest(){
        DistanceType distanceType = DistanceType.valueOfLabel("km");
        Assert.assertEquals(1000d, distanceType.convertDistanceToMeters(1d), 0);

        distanceType = DistanceType.valueOfLabel("kilometers");
        Assert.assertEquals(1000d, distanceType.convertDistanceToMeters(1d), 0);

        distanceType = DistanceType.valueOfLabel("kilometer");
        Assert.assertEquals(1000d, distanceType.convertDistanceToMeters(1d), 0);
    }

    @Test
    public void ftToMetersTest(){
        DistanceType distanceType = DistanceType.valueOfLabel("feet");
        Assert.assertEquals(0.3048d, distanceType.convertDistanceToMeters(1d), .001);

        distanceType = DistanceType.valueOfLabel("FOOT");
        Assert.assertEquals(0.3048d, distanceType.convertDistanceToMeters(1d), .001);

        distanceType = DistanceType.valueOfLabel("Ft");
        Assert.assertEquals(0.3048d, distanceType.convertDistanceToMeters(1d), .001);
    }

    @Test
    public void mileToMetersTest(){
        DistanceType distanceType = DistanceType.valueOfLabel("mile");
        Assert.assertEquals(1609.34d, distanceType.convertDistanceToMeters(1d), .2);

        distanceType = DistanceType.valueOfLabel("mi");
        Assert.assertEquals(1609.34d, distanceType.convertDistanceToMeters(1d), .2);
    }

   @Test
    public void yardsToMetersTest(){
        DistanceType distanceType = DistanceType.valueOfLabel("yard");
        Assert.assertEquals(0.9144d, distanceType.convertDistanceToMeters(1d), .001);

        distanceType = DistanceType.valueOfLabel("yards");
        Assert.assertEquals(0.9144d, distanceType.convertDistanceToMeters(1d), .001);

        distanceType = DistanceType.valueOfLabel("yd");
        Assert.assertEquals(0.9144d, distanceType.convertDistanceToMeters(1d), .001);
    }


    @Test
    public void furlongToMetersTest(){
        DistanceType distanceType = DistanceType.valueOfLabel("furlong");
        Assert.assertEquals(201.168d, distanceType.convertDistanceToMeters(1d), .02);
    }


}
