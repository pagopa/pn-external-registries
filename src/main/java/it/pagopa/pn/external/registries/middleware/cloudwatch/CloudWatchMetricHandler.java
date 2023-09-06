package it.pagopa.pn.external.registries.middleware.cloudwatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudWatchMetricHandler {

    public static final String NAMESPACE_CW_IO = "IO-COURTESY-MESSAGES";
    public static final String IO_SENT_SUCCESSFULLY = "NumberOfIOMessageSentSuccessfully";
    public static final String IO_SENT_FAILURE = "NumberOfIOMessageSentFailure";

    private final CloudWatchAsyncClient cloudWatchAsyncClient;

    @PostConstruct
    public void init() {
        log.info("Namespace for CloudWatchMetricHandler Activation: {}", NAMESPACE_CW_IO);
    }

    public void sendMetric(String namespace, String metricName, double value) {

        log.trace("Sending information to namespace=[{}] metricname=[{}]", namespace, metricName);

        PutMetricDataRequest metricDataRequest = createMetricDataRequest(metricName, namespace, value);

        Mono.fromFuture(cloudWatchAsyncClient.putMetricData(metricDataRequest))
                .subscribe(putMetricDataResponse -> log.trace("[{}] PutMetricDataResponse: {}", namespace, putMetricDataResponse),
                        throwable -> log.warn(String.format("[%s] Error sending metric", namespace), throwable));
    }

    private PutMetricDataRequest createMetricDataRequest(String metricName, String namespace, double value){
        String dimensionValue = namespace.equals(NAMESPACE_CW_IO) ? "courtesy-messages" : "no-known";
        MetricDatum metricDatum = MetricDatum.builder()
                .metricName(metricName)
                .value(value)
                .dimensions(Collections.singletonList(Dimension.builder()
                        .name("events")
                        .value(dimensionValue)
                        .build()))
                .unit(StandardUnit.COUNT)
                .timestamp(Instant.now())
                .build();

        return PutMetricDataRequest.builder()
                .namespace(namespace)
                .metricData(Collections.singletonList(metricDatum))
                .build();
    }
}