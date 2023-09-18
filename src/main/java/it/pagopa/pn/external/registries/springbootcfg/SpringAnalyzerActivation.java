package it.pagopa.pn.external.registries.springbootcfg;

import io.micrometer.core.instrument.MeterRegistry;
import it.pagopa.pn.commons.utils.cloudwatch.CloudWatchMetricHandler;
import it.pagopa.pn.commons.utils.metrics.SpringAnalyzer;
import lombok.CustomLog;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Component
@CustomLog
@Import(CloudWatchMetricHandler.class)
public class SpringAnalyzerActivation extends SpringAnalyzer {
//

    public static final String IO_SENT_SUCCESSFULLY = "NumberOfIOMessageSentSuccessfully";
    public static final String IO_SENT_FAILURE = "NumberOfIOMessageSentFailure";
    MeterRegistry meterRegistry;
    CloudWatchMetricHandler cloudWatchMetricHandler;

    public static final Collection<String> BUSINESS_METRICS = Arrays.asList(IO_SENT_SUCCESSFULLY, IO_SENT_FAILURE);

    public SpringAnalyzerActivation(CloudWatchMetricHandler cloudWatchMetricHandler, MetricsEndpoint metricsEndpoint, MeterRegistry meterRegistry) {
        super(cloudWatchMetricHandler, metricsEndpoint);
        this.cloudWatchMetricHandler = cloudWatchMetricHandler;
        this.meterRegistry = meterRegistry;
        this.meterRegistry.counter(IO_SENT_SUCCESSFULLY);
        this.meterRegistry.counter(IO_SENT_FAILURE);
    }

    @Override
    public void init() {
        super.init();
        this.getMetrics().addAll(BUSINESS_METRICS);
    }

    @Override
    protected Dimension customizedDimension(Dimension dimension, String metricName) {
        Dimension result;
        if (BUSINESS_METRICS.contains(metricName)){
            result = Dimension.builder()
                    .name("events")
                    .value("courtesy-messages")
                    .build();
        }
        else {
            result = super.customizedDimension(dimension, metricName);
        }
        return result;
    }

    @Override
    protected void metricSuccessfullSendListener(String metricName) {
        if (BUSINESS_METRICS.contains(metricName)) {
            this.meterRegistry.remove(this.meterRegistry.get(metricName).meter());
            this.meterRegistry.counter(metricName);
        } else {
            super.metricSuccessfullSendListener(metricName);
        }
    }
}
