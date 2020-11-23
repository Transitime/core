package org.transitclock.monitoring;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.utils.MathUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Provide a monitoring service to an external datastore, such as a CSV file or
 * CloudWatch if configured
 *
 * @author dbenoff
 * @author sheldonabrown
 */
public class MonitoringService {
    private String environmentName = System.getProperty("transitclock.environmentName");
    private String accessKey = System.getProperty("transitclock.cloudwatch.awsAccessKey");
    private String secretKey = System.getProperty("transitclock.cloudwatch.awsSecretKey");
    private String endpoint = System.getProperty("transitclock.cloudwatch.awsEndpoint");
    private AmazonCloudWatchClient cloudWatch;
    private Map<String, MetricDefinition> metricMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor;
    private Integer _batchSize = 1000;
    private boolean isCloudWatchInitialized = false;

    private static final Logger logger = LoggerFactory
            .getLogger(MonitoringService.class);

    private ArrayBlockingQueue<MetricScalar> individualMetricsQueue = new ArrayBlockingQueue<MetricScalar>(
            100000);

    private void init() {
        environmentName = System.getProperty("transitclock.environmentName");
        accessKey = System.getProperty("transitclock.cloudwatch.awsAccessKey");
        secretKey = System.getProperty("transitclock.cloudwatch.awsSecretKey");
        endpoint = System.getProperty("transitclock.cloudwatch.awsEndpoint");
        logger.info("re-loading system properties env={} awsAccessKey={} awsSecretKey={} awsEndpoint={}",
                environmentName, accessKey, secretKey, endpoint);
    }

    private class MetricDefinition {
        String metricName;
        MetricType metricType;
        ReportingIntervalTimeUnit reportingIntervalTimeUnit;
        Integer reportingInterval;
        Long reportingIntervalInMillis;
        Date lastUpdate;
        Collection<Double> data = new LinkedList<>();
        Boolean formatAsPercent = false;
    }

    private class MetricScalar {
        String metricName;
        Double metricValue;
        Boolean formatAsPercent;
    }

    public enum MetricType{
        SCALAR,
        SUM,
        AVERAGE,
        COUNT,
        MIN,
        MAX
    }

    public enum ReportingIntervalTimeUnit {
        IMMEDIATE,
        MINUTE,
        HOUR,
        DAY
    }

    private static MonitoringService singleton;

    private MonitoringService() {
        logger.info("MonitoringService service starting up");

        init();

        if(StringUtils.isBlank(environmentName) || StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey) || StringUtils.isBlank(endpoint)) {
            logger.warn("MonitoringService monitoring not enabled, please specify environmentName, accessKey, secretKey and endpoint in configuration file");
        }else{
            logger.info("starting MonitoringService in env {} with accessKey {} and pass {}", environmentName, accessKey, secretKey);
            AmazonCloudWatchClient cloudWatch = new AmazonCloudWatchClient(new BasicAWSCredentials(accessKey, secretKey));
            cloudWatch.setEndpoint(endpoint);
            this.cloudWatch = cloudWatch;
            isCloudWatchInitialized = true;
        }

        // start up threads regardess of cloudwatch status so logging is available
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new PublishSummaryMetricsTask(), 0, 1, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(new PublishIndividualMetricsTask(), 0, 1, TimeUnit.MINUTES);

    }

    /**
     * Returns the singleton MonitrongService
     *
     * @return
     */
    public synchronized static MonitoringService getInstance() {
        if(singleton == null)
            singleton = new MonitoringService();
        return singleton;
    }


    /**
     * increment the metric sum
     * @param metricName the metric to increment
     */
    public void sumMetric(String metricName) {
        saveMetric(metricName, 1.0, 1, MonitoringService.MetricType.SUM, MonitoringService.ReportingIntervalTimeUnit.MINUTE, false);
    }

    /**
     * provide another value to average into a rolling average metric
     * @param metricName the rolling average metric
     * @param metricValue the value to merge in
     */
    public void averageMetric(String metricName, double metricValue) {
        saveMetric(metricName, metricValue, 1, MonitoringService.MetricType.AVERAGE, MonitoringService.ReportingIntervalTimeUnit.MINUTE, false);
    }

    /**
     * track a rate (such as cache miss/hit rate) and the overall usage count.
     * @param metricName
     * @param hit
     */
    public void rateMetric(String metricName, boolean hit) {
        double metricValue = (hit? 1.0: 0.0);
        saveMetric(metricName, metricValue, 1, MonitoringService.MetricType.SUM, MonitoringService.ReportingIntervalTimeUnit.MINUTE, false);
        saveMetric(metricName + "Rate", metricValue, 1, MonitoringService.MetricType.AVERAGE, MonitoringService.ReportingIntervalTimeUnit.MINUTE, true);

    }


    /**
     *
     * Saves metric to local cache, to be reported to Cloudwatch by PublishMetricsTask
     *
     * @param metricName
     * @param metricValue
     * @param reportingInterval
     * @param metricType
     * @param reportingIntervalTimeUnit
     * @param formatAsPercent
     */
    public void saveMetric(String metricName, Double metricValue, Integer reportingInterval, MetricType metricType,
                           ReportingIntervalTimeUnit reportingIntervalTimeUnit, Boolean formatAsPercent){

        if(metricValue == null)
            return;

        if(metricType == MetricType.SCALAR && reportingIntervalTimeUnit == ReportingIntervalTimeUnit.IMMEDIATE){
            logger.info("adding individual metric to queue [{}]={}", metricName, metricValue);
            MetricScalar metricScalar = new MetricScalar();
            metricScalar.metricName = metricName;
            metricScalar.metricValue = metricValue;
            metricScalar.formatAsPercent = formatAsPercent;
            individualMetricsQueue.add(metricScalar);
            return;
        }


        if(!this.metricMap.containsKey(metricName)){
            if(metricType == MetricType.SCALAR && reportingIntervalTimeUnit != ReportingIntervalTimeUnit.IMMEDIATE)
                throw new IllegalArgumentException("SCALAR values must be reported with IMMEDIATE frequency");
            if(reportingIntervalTimeUnit != ReportingIntervalTimeUnit.IMMEDIATE && metricType == MetricType.SCALAR)
                throw new IllegalArgumentException("IMMEDIATE frequency can only be specified for SCALAR values");

            MetricDefinition metricDefinition = new MetricDefinition();
            metricDefinition.lastUpdate = new Date();
            metricDefinition.metricName = metricName;
            metricDefinition.metricType = metricType;
            metricDefinition.reportingInterval = reportingInterval;
            metricDefinition.reportingIntervalTimeUnit = reportingIntervalTimeUnit;
            metricDefinition.formatAsPercent = formatAsPercent;
            if(reportingIntervalTimeUnit == ReportingIntervalTimeUnit.MINUTE)
                metricDefinition.reportingIntervalInMillis = reportingInterval * 60l * 1000l;
            if(reportingIntervalTimeUnit == ReportingIntervalTimeUnit.HOUR)
                metricDefinition.reportingIntervalInMillis = reportingInterval * 60l * 60l * 1000l;
            if(reportingIntervalTimeUnit == ReportingIntervalTimeUnit.DAY)
                metricDefinition.reportingIntervalInMillis = reportingInterval * 24l * 60l * 60l * 1000l;
            this.metricMap.put(metricName, metricDefinition);
        }
        logger.debug("saving metric for summarized publication to Cloudwatch [{}]={}", metricName, metricValue);
        MetricDefinition metricDefinition = this.metricMap.get(metricName);
        synchronized (metricDefinition) {
            metricDefinition.data.add(metricValue);
        }
    }

    private void publishMetric(String metricName, Double metricValue){
      if (metricName == null || metricValue == null)
        return ;

        Date timestamp = new Date();

        MetricDatum datum = new MetricDatum().
                withMetricName(metricName).
                withTimestamp(timestamp).
                withValue(metricValue).
                withUnit(StandardUnit.Count);
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().
                withNamespace(environmentName).
                withMetricData(datum);
        try {
            logger.info("COUNT,{},{},{}", metricName, timestamp.getTime()/1000, metricValue);
            if (isCloudWatchInitialized) {
                cloudWatch.putMetricData(putMetricDataRequest);
            }
        } catch (Exception any) {
          logger.error("exception publishing for {}={}: {}", metricName, metricValue, any);
        }
    }

    /**
     * metric value should be between 0 and 1 representing values 0 to 100 percent.
     * @param metricName
     * @param metricValue
     */
    private synchronized void publishMetricAsPercent(String metricName, Double metricValue){
        if(metricName == null || metricValue == null)
          return;

        Date timestamp = new Date();

        MetricDatum datum = new MetricDatum().
                withMetricName(metricName).
                withTimestamp(timestamp).
                withValue(metricValue * 100d).
                withUnit(StandardUnit.Percent);
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().
                withNamespace(environmentName).
                withMetricData(datum);
        try {
            logger.info("PERCENT,{},{},{}", metricName, timestamp.getTime()/1000, metricValue);
            if (isCloudWatchInitialized) {
                cloudWatch.putMetricData(putMetricDataRequest);
            }
        } catch (Exception any) {
          logger.error("exception publishing for {}={}: {}", metricName, metricValue, any);
        }

    }

    private class PublishIndividualMetricsTask implements Runnable {

        @Override
        public void run() {
            List<MetricScalar> records = new ArrayList<MetricScalar>();
            individualMetricsQueue.drainTo(records, _batchSize);
            for(MetricScalar metricScalar : records){
                logger.debug("Publishing individual metric [{}]={}", metricScalar.metricName, metricScalar.metricValue);
                if(metricScalar.formatAsPercent){
                    publishMetricAsPercent(metricScalar.metricName, metricScalar.metricValue);
                }else{
                    publishMetric(metricScalar.metricName, metricScalar.metricValue);
                }
            }
        }
    }

    private class PublishSummaryMetricsTask implements Runnable {

        @Override
        public void run() {
            try {
                Date now = new Date();
                for (String metricName : metricMap.keySet()) {
                    MetricDefinition metricDefinition = metricMap.get(metricName);
                    try {
                      if(metricDefinition == null || metricDefinition.reportingIntervalTimeUnit == ReportingIntervalTimeUnit.IMMEDIATE)
                          continue;
                      if (metricDefinition.lastUpdate == null)
                        continue;
                      long dateDiff = now.getTime() - metricDefinition.lastUpdate.getTime();

                      Collection<Double> dataCopy = new ArrayList<>();
                      synchronized (metricDefinition) {
                          if (metricDefinition.data != null && !metricDefinition.data.isEmpty()) {
                              dataCopy = new ArrayList(metricDefinition.data);
                              metricDefinition.data.clear();
                          }
                      }

                      Double metric = null;
                      if (dateDiff >= metricDefinition.reportingIntervalInMillis) {
                          if (metricDefinition.metricType == MetricType.AVERAGE) {
                              if (dataCopy.isEmpty()) continue;
                              metric = MathUtils.average(dataCopy);
                          } else if (metricDefinition.metricType == MetricType.COUNT) {
                              metric = new Double(dataCopy.size());
                          } else if (metricDefinition.metricType == MetricType.SUM) {
                              metric = MathUtils.sum(dataCopy);
                          } else if (metricDefinition.metricType == MetricType.MIN) {
                              if (dataCopy.size() < 1)
                                  return;
                              metric = MathUtils.min(dataCopy);
                          } else if (metricDefinition.metricType == MetricType.MAX) {
                              if (dataCopy.size() < 1)
                                  return;
                              metric = MathUtils.max(dataCopy);
                          }
                          metricDefinition.lastUpdate = now;
                      }
                      if (metric != null) {
                          if (metricDefinition.formatAsPercent) {
                              publishMetricAsPercent(metricName, metric);
                          } else {
                              publishMetric(metricName, metric);
                          }
                      }
                  } catch (Exception e) {
                    logger.error("issue with metric " + metricName + ": ", e);
                  }
                }
            } catch (Exception e) {
                logger.error("Exception with metrics: ", e);
            }
        }
    }

    public static void main(String[] args){
        MonitoringService cloudwatchService = MonitoringService.getInstance();
        int i = 0;
        while (i < 100){
            cloudwatchService.saveMetric("testing", Math.random(), 1, MetricType.AVERAGE, ReportingIntervalTimeUnit.MINUTE, false);
            i++;
        }
    }
}
