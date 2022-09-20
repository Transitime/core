package org.transitclock.avl;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.ApcReport;

import java.util.List;

/**
 * Support for loading APC data.
 */
public class BatchCsvApcModule {

    private static final Logger logger = LoggerFactory
            .getLogger(BatchCsvApcModule.class);
    private String agencyId;
    private int configRev;
    private String csvFileName;

    private Session session;

    private List<ApcReport> reports;

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
    }


    public BatchCsvApcModule(String agencyId, int configRev, Session session) {
        this.agencyId = agencyId;
        this.configRev = configRev;
        this.session = session;
    }

    public List<ApcReport> run() {
        ApcReport lastReport = null;
        try {
            String fileName = getCsvFileName();
            ApcCsvReader apcCsvReader = new ApcCsvReader(fileName, configRev);
            this.reports = apcCsvReader.get();
            int i = 0;
            for (ApcReport report : reports) {
                i++;
                lastReport = report;
                if (i % 1000 == 0) {
                    logger.info("saving report {}", report);
                }
                session.save(report);
            }
        } catch (Throwable t) {
            logger.error("issue with record {}, {}", lastReport, t, t);
        } finally {
            session.flush();
        }
        return reports;
    }

    private List<ApcReport> getReports() { return reports; }
}
