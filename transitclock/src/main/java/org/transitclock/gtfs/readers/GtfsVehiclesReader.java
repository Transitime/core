package org.transitclock.gtfs.readers;

import org.apache.commons.csv.CSVRecord;
import org.transitclock.gtfs.gtfsStructs.GtfsVehicle;
import org.transitclock.utils.csv.CsvBaseReader;

import java.text.ParseException;

public class GtfsVehiclesReader extends CsvBaseReader<GtfsVehicle> {
  public GtfsVehiclesReader(String dirName) {
    super(dirName, "vehicles.txt", false, false);
  }

  @Override
  protected GtfsVehicle handleRecord(CSVRecord record, boolean supplemental) throws ParseException, NumberFormatException {
    return new GtfsVehicle(record, supplemental, getFileName());
  }
}
