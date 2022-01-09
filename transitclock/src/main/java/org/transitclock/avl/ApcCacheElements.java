package org.transitclock.avl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * List of APC moving averages and their observations.
 */
public class ApcCacheElements implements Serializable {
  private List<ApcCacheElement> elements;
  public ApcCacheElements(List<ApcCacheElement> elements, ApcCacheElement extra) {
    elements.add(extra);
    this.elements = elements;
  }
  public ApcCacheElements(ApcCacheElement extra) {
    this.elements = new ArrayList<>();
    elements.add(extra);
  }

  public List<ApcCacheElement> getElements() {
    return elements;
  }
}
