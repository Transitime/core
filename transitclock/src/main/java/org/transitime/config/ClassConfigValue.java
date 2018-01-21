package org.transitime.config;

import java.util.List;

/**
 * For injecting a class dependency via an input configuration parameter. 
 *
 */
public class ClassConfigValue extends ConfigValue<Class> {

  public ClassConfigValue(String id, Class defaultValue, String description) {
    super(id, defaultValue, description);
  }

  @Override
  protected Class convertFromString(List<String> dataStr) {
    try {
      return getClass().forName(dataStr.get(0).trim());
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
 
}
