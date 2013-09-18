/**
 * Copyright 2013 Stackdriver
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.servo.publish.stackdriver;


import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.netflix.servo.Metric;
import com.netflix.servo.publish.BaseMetricObserver;
import com.stackdriver.api.custommetrics.CustomMetricsPoster;

/**
 * Observer that shunts metrics out to the wicked-cool monitoring backend Stackdriver.
 *
 * @author cfregly@metricsPoster.com
 */
public class StackdriverMetricObserver extends BaseMetricObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackdriverMetricObserver.class);

    private final StackdriverNamingConvention namingConvention;
    private final String instanceId;
    private final CustomMetricsPoster metricsPoster;

    /**
     * @param stackdriverApiKey your Stackdriver API Key
     * @param instanceId instanceId to attach to each metric published (optional)
     */
    public StackdriverMetricObserver(String stackdriverApiKey, String instanceId) {
        this(stackdriverApiKey, new BasicStackdriverNamingConvention(), instanceId);
    }

    /**
     * @param stackdriverApiKey your Stackdriver API Key
     * @param namingConvention naming convention to extract a Stackdriver compatible name from each Metric
     * @param instanceId instanceId to attach to each metric published (optional)
     */
    public StackdriverMetricObserver(String stackdriverApiKey, StackdriverNamingConvention namingConvention, String instanceId) {
        super( "StackdriverMetricObserver." + instanceId );

        this.instanceId = instanceId;        
        this.metricsPoster = new CustomMetricsPoster(stackdriverApiKey);
        this.namingConvention = namingConvention;
    }

    @Override
    public void updateImpl(List<Metric> metrics) {
        try {
            write(metrics);
        }
        catch (IOException e) {
            LOGGER.warn("Stackdriver connection failed on write", e);
        }
    }

    private void write(List<Metric> metrics) throws IOException {
        int count = writeMetrics(metrics);

        LOGGER.debug("Wrote {} metrics to Stackdriver", count);
    }

    private int writeMetrics(List<Metric> metrics) {
        int count = 0;
        
//        CustomMetricsMessage message = new CustomMetricsMessage();
//        message.setMetrics(metrics);
//        
//        metricsPoster.sendMetrics(message);
//
//        return metrics.size();
//        
//        
        for ( Metric metric : metrics ) {
            String publishedName = namingConvention.getName(metric);

            // convert booleans into 0's or 1's
            String metricAsStr = metric.getValue().toString();
            
            // skip if null or empty
            if (!Strings.isNullOrEmpty(metricAsStr)) {
            	if (metricAsStr.equals("false")) {
            		metricAsStr = "0";
            	} else if (metricAsStr.equals("true")) {
            		metricAsStr = "1";
            	}
	
	            try {
	            	// convert to double
	            	Double metricAsDouble = Double.parseDouble(metricAsStr);
	            	
		            // send the metrics
		            if (!Strings.isNullOrEmpty(instanceId)) {  
		            	// include instance-level serverPrefix
		            	metricsPoster.sendInstanceMetricDataPoint(publishedName, metricAsDouble, (new Date(metric.getTimestamp())), instanceId);
		            } else { 
		            	metricsPoster.sendMetricDataPoint(publishedName, metricAsDouble, (new Date(metric.getTimestamp())));
		            }
	            
		            count++;
	            } catch (NumberFormatException exc) {
	            	LOGGER.warn("Unable to convert metric value '{}' into a Double. {}", metric.getValue(), exc);            	
	            }
            }
        }
        return count;
    }
}
