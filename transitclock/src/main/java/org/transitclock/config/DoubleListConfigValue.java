package org.transitclock.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Configurable List of Doubles.
 */
public class DoubleListConfigValue extends ConfigValue<List<Double>> {

  private static final Logger logger = LoggerFactory.getLogger(DoubleListConfigValue.class);

  public DoubleListConfigValue(String id, List<Double> defaultValues, String description) {
    super(id, defaultValues, description);
  }

  public static List<Double> unencodeString(String encodedValues) {
    List<Double> defaultValues = new ArrayList<>();
    if (encodedValues != null) {
      for (String s: encodedValues.split(";")) {
        try {
          defaultValues.add(Double.parseDouble(s));
        } catch (NumberFormatException nfe) {
          logger.error("illegal default value {}", s, nfe);
        }
      }
    }
  return defaultValues;
  }

  @Override
  protected List<Double> convertFromString(List<String> dataStr) {
    List<Double> numbers = new ArrayList<>(dataStr.size());
    for (String s : dataStr) {
      try {
        numbers.add(Double.parseDouble(s));
      } catch (NumberFormatException nfe) {
        logger.error("error parsing value {}", s, nfe);
      }
    }
    return numbers;
  }
}
