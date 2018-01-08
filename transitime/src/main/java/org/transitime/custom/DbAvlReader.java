package org.transitime.custom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.transitime.avl.AvlClient;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;
import org.transitime.db.structs.AvlReport;
import org.transitime.modules.Module;
import org.transitime.utils.Time;
import org.transitime.utils.threading.BoundedExecutor;
import org.transitime.utils.threading.NamedThreadFactory;

public class DbAvlReader extends Module {

  public static final String DB_URL = "url";
  private static StringConfigValue dbUrl =
      new StringConfigValue("transitime.avl.dbUrl", null, "The jdbc url of the avl database");
  private static StringConfigValue dbQuery =
      new StringConfigValue("transitime.avl.dbQuery", "select ID as Id, BusID as busId, "
          + " CAST(CONCAT(RptDate, ' ', RptTime) AS DATETIME) AS rptDateTime, "
          + "LatDD as lat, LonDD as lon, LogonRoute as logonRoute, LogonTrip as logonTrip, "
          + "RptDate as reportDate from tblbuses;", "The query to execute to receive AVL data");
  
  private static StringConfigValue validationQuery =
      new StringConfigValue("transitime.avl.validationQuery", "select 1");
  private static IntegerConfigValue numAvlThreads = 
      new IntegerConfigValue("transitime.avl.jmsNumThreads", 1,
          "How many threads to be used for processing the AVL " +
          "data. For most applications just using a single thread " +
          "is probably sufficient and it makes the logging simpler " +
          "since the messages will not be interleaved. But for " +
          "large systems with lots of vehicles then should use " +
          "multiple threads, such as 3-5 so that more of the cores " +
          "are used. Only for when JMS is used.");
  private static IntegerConfigValue avlQueueSize = 
      new IntegerConfigValue("transitime.avl.jmsQueueSize", 350,
          "How many items to go into the blocking AVL queue "
          + "before need to wait for queue to have space. "
          + "Only for when JMS is used.");
  private static IntegerConfigValue pollingRateMsec =
      new IntegerConfigValue("transitime.avl.pollingRateMsec", 30000,
          "Milliseconds between AVL database polls");
  
  private final BoundedExecutor _avlClientExecutor;
  private Connection connection = null;
  
  public DbAvlReader(String agencyId) throws Exception {
    super(agencyId);
    int numberThreads = numAvlThreads.getValue();
    int maxAVLQueueSize = avlQueueSize.getValue();
    // Create the executor that actually processes the AVL data
    NamedThreadFactory avlClientThreadFactory = new NamedThreadFactory(
        "avlClient");
    Executor executor = Executors.newFixedThreadPool(numberThreads,
        avlClientThreadFactory);
    _avlClientExecutor = new BoundedExecutor(executor, maxAVLQueueSize);
  }

  protected List<AvlReport> getAvlReports(Connection connection) throws Exception {
    ResultSet rs = null;
    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    rs = statement.executeQuery(dbQuery.getValue());
    return map(rs);
  }
  
  private List<AvlReport> map(ResultSet rs) throws Exception {
    ArrayList<AvlReport> data = new ArrayList<AvlReport>();
    while (rs.next()) {
      AvlReport avl = readRow(rs);
      if (avl != null) {
        data.add(avl);
      }
    }
    return data;
  }

  private AvlReport readRow(ResultSet rs) throws Exception {
    Integer id = rs.getInt(1);
    String vehicleId = rs.getString(2);
    Date reportedTime = rs.getTimestamp(3);
    Double lat = rs.getDouble(4);
    Double lon = rs.getDouble(5);
    String source = "db";
    AvlReport avl = new AvlReport(vehicleId, reportedTime.getTime(), lat, lon, source);
    return avl;
  }

  private Connection getAndValidateConnection() throws Exception {
    if (connection == null) {
      connection = getConnection(getConnectionProperties());
    }
    if (!isValid(connection)) {
      connection = getConnection(getConnectionProperties());
    }
    return connection;
  }
  
  private boolean isValid(Connection connection) {
    if (connection == null ) return false;
    try {
    Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    statement.executeQuery(validationQuery.getValue());
    logger.info("validation succeeded");
    return true;
    } catch (Throwable t) {
      return false;
    }
  }

  Map getConnectionProperties() {
    HashMap<String, String> properties = new HashMap<String, String>();
    properties.put(DB_URL, dbUrl.getValue());
    return properties;
  }
  
  Connection getConnection(Map<String, String> properties) throws Exception {
    /* here we load the driver by hand -- the avl database may (will!) be
     * completely different from the transitime database. 
     */
    return DriverManager.getConnection(properties.get(DB_URL));
  }
  
  @Override
  public void run() {

    if (dbUrl.getValue() == null) {
      logger.error("no dbUrl configured for reader, exiting");
      return;
    }
    
    while (!Thread.interrupted()) {
      
      try {
        Connection conn =  getAndValidateConnection();
        if (conn != null) { 
          logger.info("poll");
          List<AvlReport> reports = getAvlReports(conn);
          logger.info("found {} reports: {}", reports.size(), reports);
          for (AvlReport report : reports) {
            Runnable avlClient = new AvlClient(report);
            _avlClientExecutor.execute(avlClient);
          }
        } else {
          logger.error("no connection available for {}", dbUrl.getValue());
        }
        Time.sleep(pollingRateMsec.getValue());
      } catch (Exception any) {
        logger.error("issue with avl {}", any);
      }
    }
  }

}
