package org.transitime.monitoring;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by dbenoff on 10/6/15.
 */
public class CloudwatchService {

    private String environmentName = System.getProperty("transitime.environmentName");
    private String accessKey = System.getProperty("transitime.cloudwatch.awsAccessKey");
    private String secretKey = System.getProperty("transitime.cloudwatch.awsSecretKey");
    private String endpoint = System.getProperty("transitime.cloudwatch.awsEndpoint");
    private AmazonCloudWatchClient cloudWatch;

    private static final Logger logger = LoggerFactory
            .getLogger(CloudwatchService.class);

    public CloudwatchService (){
        if(environmentName == null || accessKey == null || secretKey == null || endpoint == null){
            logger.warn("Cloudwatch monitoring not enabled, please specify environmentName, accessKey, secretKey and endpoint in configuration file");
        }else{
            logger.info("starting Cloudwatch in env {} with accessKey {} and pass {}", environmentName, accessKey, secretKey);
            AmazonCloudWatchClient cloudWatch = new AmazonCloudWatchClient(new BasicAWSCredentials(accessKey, secretKey));
            cloudWatch.setEndpoint(endpoint);
            this.cloudWatch = cloudWatch;
        }
    }

    public synchronized void publishMetrics(Map<String, Double> metricMap){

        if(cloudWatch == null)
            return;

        List<MetricDatum> data = new ArrayList<>();
        for(String metricName : metricMap.keySet()){
            data.add(
                    new MetricDatum().
                            withMetricName(metricName).
                            withTimestamp(new Date()).
                            withValue(metricMap.get(metricName)).
                            withUnit(StandardUnit.Count)
            );
        }

        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().
                withNamespace(environmentName).
                withMetricData(data);
        cloudWatch.putMetricData(putMetricDataRequest);

    }

    public synchronized void publishMetric(String metricName, Double metricValue){
        logger.info("Cloudwatch[{}]={}", metricName, metricValue);
        if(cloudWatch == null)
            return;

        MetricDatum datum = new MetricDatum().
                withMetricName(metricName).
                withTimestamp(new Date()).
                withValue(metricValue).
                withUnit(StandardUnit.Count);
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().
                withNamespace(environmentName).
                withMetricData(datum);
        cloudWatch.putMetricData(putMetricDataRequest);

    }

}
