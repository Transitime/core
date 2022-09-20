package org.transitclock.core.reporting.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.transitclock.db.structs.FeedInfo;
import org.transitclock.gtfs.gtfsStructs.GtfsFeedInfo;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.transitclock.utils.CsvDataConveterUtil.getInteger;

public class FeedInfoCsvDao {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public FeedInfoCsvDao(String filePath){
        this.filePath = filePath;
    }

    public List<FeedInfo> getAll() throws IOException, ParseException {
        List<FeedInfo> feedInfo = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        InputStream inputStream = FeedInfoCsvDao.class.getClassLoader().getResourceAsStream(getFilePath());
        Reader in = new BufferedReader(new InputStreamReader(inputStream));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);

        for (CSVRecord record : records) {
            GtfsFeedInfo gf = new GtfsFeedInfo(record, false, null);
            int configRev = getInteger(record.get("configRev"));

            feedInfo.add(new FeedInfo(configRev, gf, sdf));
        }

        return feedInfo;
    }
}
