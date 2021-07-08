package org.transitclock.monitoring;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private ConcurrentHashMap<String, List<Double>> averageMetrics = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<Double>> rateMetrics = new ConcurrentHashMap<String, List<Double>>();
    private ConcurrentHashMap<String, Double> sumMetrics = new ConcurrentHashMap<String, Double>();
    private ScheduledExecutorService executor;
    private boolean isCloudWatchInitialized = false;
    private static Object singeltonLock = new Object();

    private static final Logger logger = LoggerFactory
            .getLogger(MonitoringService.class);

    private void init() {
        environmentName = System.getProperty("transitclock.environmentName");
        accessKey = System.getProperty("transitclock.cloudwatch.awsAccessKey");
        secretKey = System.getProperty("transitclock.cloudwatch.awsSecretKey");
        endpoint = System.getProperty("transitclock.cloudwatch.awsEndpoint");
        logger.info("re-loading system properties env={} awsAccessKey={} awsSecretKey={} awsEndpoint={}",
                environmentName, accessKey, secretKey, endpoint);
    }

    public void flush() {
        publishSimpleMetrics();
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

        // start up threads regardless of cloudwatch status so logging is available
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new PublishMetricsTask(), 0, 1, TimeUnit.MINUTES);

    }

    /**
     * Returns the singleton MonitoringService
     *
     * @return
     */
    public static MonitoringService getInstance() {
        if(singleton == null) {
            synchronized (singeltonLock) {
                // check to see if we won the race
                if (singleton == null)
                    singleton = new MonitoringService();
            }
        }
        return singleton;
    }


    /**
     * increment the metric sum
     * @param metricName the metric to increment
     */
    public void sumMetric(String metricName) {
        synchronized (sumMetrics) {
            Double sum = sumMetrics.get(metricName);
            if (sum == null) {
                sum = 0.0;
            }
            sum = sum + 1.0;
            sumMetrics.put(metricName, sum);
        }
    }

    /**
     * provide another value to average into a rolling average metric
     * @param metricName the rolling average metric
     * @param metricValue the value to merge in
     */
    public void averageMetric(String metricName, double metricValue) {
        synchronized (averageMetrics) {
            List<Double> metrics = averageMetrics.get(metricName);
            if (metrics == null) {
                metrics = new ArrayList<>();
            }
            metrics.add(metricValue);
            averageMetrics.put(metricName, metrics);
        }
    }

    /**
     * track a rate (such as cache miss/hit rate) and the overall usage count.
     * @param metricName
     * @param hit
     */
    public void rateMetric(String metricName, boolean hit) {
        double metricValue = (hit? 1.0: 0.0);
        synchronized (rateMetrics) {
            List<Double> metrics = rateMetrics.get(metricName);
            if (metrics == null) {
                metrics = new ArrayList<>();
            }
            metrics.add(metricValue);
            rateMetrics.put(metricName, metrics);
        }

    }

    private void publishMetrics(List<MetricDatum> metrics) {
        if (metrics == null || metrics.isEmpty())
            return;

        List<MetricDatum> remainingMetrics = publishOnly20Metrics(metrics);
        while (!remainingMetrics.isEmpty()) {
            remainingMetrics = publishOnly20Metrics(remainingMetrics);
        }
    }

    private List<MetricDatum> publishOnly20Metrics(List<MetricDatum> metrics) {
        if (metrics == null || metrics.isEmpty()) return metrics;

        try {
            int i = 0;
            List<MetricDatum> only20 = new ArrayList<>(20);
            Iterator iterator = metrics.iterator();
            // CloudWatch API limits us to 20 metrics in a batch at a time
            while (iterator.hasNext() && i < 20) {
                i++;
                MetricDatum datum = (MetricDatum) iterator.next();
                if (datum != null && datum.getValue() != null) {
                    only20.add(datum);
                    logger.info("{},{},{}", datum.getUnit(), datum.getMetricName(), datum.getValue());
                } else {
                    logger.info("discarding empty metric {}", datum.getMetricName());
                }
                iterator.remove();
            }
            if (isCloudWatchInitialized) {
                PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest()
                        .withNamespace(environmentName)
                        .withMetricData(only20);
                cloudWatch.putMetricData(putMetricDataRequest);
            }

        } catch (Exception any) {
            logger.error("exception publishing for {}, {}", metrics, any, any);
        }
        return metrics;
    }

    private MetricDatum toDatumCount(String metricName, double metricValue) {
        return toDatum(metricName, metricValue, StandardUnit.Count);
    }
    private MetricDatum toDatumPercent(String metricName, double metricValue) {
        return toDatum(metricName, metricValue, StandardUnit.Percent);
    }

    private MetricDatum toDatum(String metricName, double metricValue, StandardUnit unit) {
        Date timestamp = new Date();

        MetricDatum datum = new MetricDatum().
                withMetricName(metricName).
                withTimestamp(timestamp).
                withValue(metricValue).
                withUnit(unit);
        return datum;
    }

    private void publishSimpleMetrics() {
        long start = System.currentTimeMillis();
        List<MetricDatum> simpleMetrics = new ArrayList<>();

        synchronized (averageMetrics) {
            for (String key : averageMetrics.keySet()) {
                List<Double> metrics = averageMetrics.get(key);
                if (metrics != null && !metrics.isEmpty()) {
                    int size = metrics.size();
                    double total = 0.0;
                    for (double d : metrics) {
                        total += d;
                    }
                    simpleMetrics.add(toDatumCount(key, total/size));
                }
            }
            averageMetrics.clear();
        }

        synchronized (rateMetrics) {
            for (String key : rateMetrics.keySet()) {
                List<Double> metrics = rateMetrics.get(key);
                if (metrics != null && !metrics.isEmpty()) {
                    int size = metrics.size();
                    double total = 0.0;
                    for (double d : metrics) {
                        total += d;
                    }
                    simpleMetrics.add(toDatumPercent(key + "Rate", total/size));
                    simpleMetrics.add(toDatumCount(key, total));
                }
            }
            rateMetrics.clear();
        }
        synchronized (sumMetrics) {
            for (String key : sumMetrics.keySet()) {
                Double sum = sumMetrics.get(key);
                simpleMetrics.add(toDatumCount(key, sum));
            }
            sumMetrics.clear();
        }

        int size = simpleMetrics.size();
        publishMetrics(simpleMetrics);
        long end = System.currentTimeMillis();
        logger.info("published {} metrics in {}ms", size, (end - start));

    }

    private class PublishMetricsTask implements Runnable {
        @Override
        public void run() {
            publishSimpleMetrics();
        }
    }

    public static void main(String[] args){
        MonitoringService cloudwatchService = MonitoringService.getInstance();
        int i = 0;
        while (i < 100){
            cloudwatchService.averageMetric("testing", Math.random());
            i++;
        }
        cloudwatchService.flush();
    }
}
