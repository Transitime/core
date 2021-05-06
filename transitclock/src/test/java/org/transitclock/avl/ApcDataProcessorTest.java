package org.transitclock.avl;

import org.junit.Before;
import org.junit.Test;
import org.transitclock.SingletonSupport;

import static org.junit.Assert.*;

public class ApcDataProcessorTest {

  @Before
  public void setup() {
    SingletonSupport.createTestCore();
  }

  @Test
  public void loadYesterdaysRates() {
    // for now this just validates the cron pattern loading
    ApcDataProcessor instance = ApcDataProcessor.getInstance();
    assertNotNull(instance);
    instance.loadYesterdaysRates();
  }
}