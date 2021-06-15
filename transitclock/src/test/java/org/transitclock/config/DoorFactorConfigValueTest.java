package org.transitclock.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class DoorFactorConfigValueTest {


  @Test
  public void test() {

    DoorFactorConfigValue config = new DoorFactorConfigValue("foo1", null, "desc");
    // look for no such value
    Double valueForIndex = config.getValueForIndex(0, 0);
    assertNotNull(valueForIndex);
    assertEquals(1.0, valueForIndex, 0.001);
    valueForIndex = config.getValueForIndex(0, 1);
    assertNotNull(valueForIndex);
    assertEquals(1.0, valueForIndex, 0.001);
    valueForIndex = config.getValueForIndex(2, 2);
    assertNotNull(valueForIndex);
    // not the "default" pattern, just the simple anser
    assertEquals(1.0, valueForIndex, 0.001);


    config = new DoorFactorConfigValue("foo2", "default", "desc");
    // look for no such value
    valueForIndex = config.getValueForIndex(0, 0);
    assertNotNull(valueForIndex);
    assertEquals(1.0, valueForIndex, 0.001);
    valueForIndex = config.getValueForIndex(0, 1);
    assertNotNull(valueForIndex);
    assertEquals(1.0, valueForIndex, 0.001);
    valueForIndex = config.getValueForIndex(2, 2);
    assertNotNull(valueForIndex);
    // note the custom value for "default" pattern
    assertEquals(0.96, valueForIndex, 0.001);

    config = new DoorFactorConfigValue("foo3", "classpath:door_factor.csv", "desc");
    // look for no such value
    valueForIndex = config.getValueForIndex(0, 0);
    assertNotNull(valueForIndex);
    assertEquals(1.0, valueForIndex, 0.001);
    valueForIndex = config.getValueForIndex(0, 1);
    assertNotNull(valueForIndex);
    assertEquals(1.0, valueForIndex, 0.001);
    valueForIndex = config.getValueForIndex(2, 2);
    assertNotNull(valueForIndex);
    // note the custom value for "default" pattern
    assertEquals(0.96, valueForIndex, 0.001);

    // a non-sense testing value to confirm the csv loaded
    valueForIndex = config.getValueForIndex(10, 10);
    assertEquals(0.01, valueForIndex, 0.001);
  }

}