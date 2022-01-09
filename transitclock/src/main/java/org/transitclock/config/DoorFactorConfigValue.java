package org.transitclock.config;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CSV based configuration of Door Factors.  defaultValue can be null to bypass,
 * or "default" to use standard configuration, or a file on disk to load a CSV
 * dataset.
 */
public class DoorFactorConfigValue extends ConfigValue<String> {


  private Map<FactorKey, FactorValue> factorMap = null;
  private FactorValue defaultFactorValue = null;
  private boolean isInitialized = false;


  public DoorFactorConfigValue(String id, String defaultValue, String description) {
    super(id, defaultValue, description, true);
  }

  @Override
  protected String convertFromString(List<String> dataStr) {
    String value = dataStr.get(0);
    if ("default".equals(value)) {
      initDefault();
      return "default";
    }
    return loadFromCsv(value);
  }

  private String loadFromCsv(String value) {
    List<DoorFactor> doorFactors =
            (new DoorFactorCsvReader(value)).get();
    if (doorFactors == null || doorFactors.size() == 0) {
      throw new IllegalArgumentException("Configuration file '" +
              this.value + "' not present, empty, or invalid!");
    }
    factorMap = new HashMap<>();

    for (DoorFactor df : doorFactors) {
      factorMap.put(df.getKey(), df.getValue());
    }
    init();
    return "true";
  }

  private void lazyInit() {
    if (isInitialized) return;

    if (this.value == null) {
      init();
      isInitialized = true;
      return;
    }
    // treat value as a config hint and fall back to defaults
    if ("default".equals(this.value)) {
      initDefault();
      isInitialized = true;
      return;
    }
    // treat value as a filename
    loadFromCsv(this.value);
    isInitialized = true;
  }

  private void init() {
    defaultFactorValue = new FactorValue(1, 1);
  }

  private void initDefault() {
    factorMap = new HashMap<>();
    factorMap.put(new FactorKey(0, 1), new FactorValue(1, 1));
    factorMap.put(new FactorKey(0, 2), new FactorValue(1, 0.58));
    factorMap.put(new FactorKey(1, 2), new FactorValue(0.6, 0.66));
    factorMap.put(new FactorKey(1, 3), new FactorValue(0.4, 0.48));
    factorMap.put(new FactorKey(2, 1), new FactorValue(1, 1));
    factorMap.put(new FactorKey(2, 2), new FactorValue(0.96, 0.68));
    init();
  }

  public Double getValueForIndex(Integer boardingType, Integer doorCount) {
    lazyInit();
    if (factorMap == null) return defaultFactorValue.getRatioOns();
    FactorValue value = factorMap.get(new FactorKey(boardingType, doorCount));
    if (value == null) return defaultFactorValue.getRatioOns();
    return value.getRatioOns();
  }

  private static class FactorKey {
    private int boardingType;
    private int doorCount;
    public FactorKey(int boardingType, int doorCount) {
      this.boardingType = boardingType;
      this.doorCount = doorCount;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return true;
      FactorKey that = (FactorKey) o;
      return Objects.equals(boardingType, that.boardingType)
              && Objects.equals(doorCount, that.doorCount);
    }

    @Override
    public int hashCode() {
      return Objects.hash(boardingType, doorCount);
    }

    @Override
    public String toString() {
      return "{boardingType=" + boardingType
              + ", doorCount=" + doorCount
              + "}";
    }
  }

  private static class FactorValue {
    private double ratioOns;
    private double ratioOffs;
    public FactorValue(double ratioOns, double ratioOffs) {
      this.ratioOns = ratioOns;
      this.ratioOffs = ratioOffs;
    }

    public double getRatioOns() { return ratioOns; }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return true;
      FactorValue that = (FactorValue) o;
      return Objects.equals(ratioOns, that.ratioOns)
              && Objects.equals(ratioOffs, that.ratioOffs);
    }

    @Override
    public int hashCode() {
      return Objects.hash(ratioOns, ratioOffs);
    }

    @Override
    public String toString() {
      return "{ratioOns=" + ratioOns
              + ", ratioOffs=" + ratioOffs
              + "}";
    }
  }

  private static class DoorFactor {
    private int boardingType;
    private int doorCount;
    private double ratioOns;
    private double ratioOffs;
    public DoorFactor(int boardingType, int doorCount, double ratioOns, double ratioOffs) {
      this.boardingType = boardingType;
      this.doorCount = doorCount;
      this.ratioOns = ratioOns;
      this.ratioOffs = ratioOffs;
    }

    public FactorKey getKey() {
      return new FactorKey(boardingType, doorCount);
    }

    public FactorValue getValue() {
      return new FactorValue(ratioOns, ratioOffs);
    }
  }

  private static class DoorFactorCsvReader extends CsvBaseReader<DoorFactor> {

    public DoorFactorCsvReader(String filename) { super(filename); }

    @Override
    protected DoorFactor handleRecord(CSVRecord record, boolean supplemental) throws ParseException, NumberFormatException {
      return new DoorFactor(getRequiredInt(record, "boarding_type"),
              getRequiredInt(record, "door_count"),
              getRequiredDouble(record, "ratio_ons"),
              getRequiredDouble(record, "ratio_offs"));
    }

    private double getRequiredDouble(CSVRecord record, String value) {
      String s = record.get(value);
      if (s == null) return 0.0;
      try {
        return Double.parseDouble(s);
      } catch (NumberFormatException nfe) {
        return 0.0;
      }
    }

    private int getRequiredInt(CSVRecord record, String value) {
      String s = record.get(value);
      if (s == null) return -1;
      try {
        return Integer.parseInt(s);
      } catch (NumberFormatException nfe) {
        return -1;
      }
    }
  }
}
