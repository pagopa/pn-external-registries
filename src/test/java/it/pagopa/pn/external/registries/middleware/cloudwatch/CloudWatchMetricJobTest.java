package it.pagopa.pn.external.registries.middleware.cloudwatch;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudWatchMetricJobTest {

    @Mock
    private CloudWatchAsyncClient cloudWatchAsyncClient;

    @InjectMocks
    private CloudWatchMetricHandler cloudWatchMetricHandler;

    @Test
    void testSendMetricToCloudWatch() {
        PutMetricDataResponse putMetricDataResponse = PutMetricDataResponse.builder().build();
        when(cloudWatchAsyncClient.putMetricData(any(PutMetricDataRequest.class))).thenReturn(CompletableFuture.completedFuture(putMetricDataResponse));
        Dimension dimension = Dimension.builder()
                .name("events")
                .value("courtesy-messages")
                .build();
        Assertions.assertDoesNotThrow(() -> cloudWatchMetricHandler.sendMetric(CloudWatchMetricHandler.NAMESPACE_CW_IO, dimension,"NumberOfIOMessageSentSuccessfully", 1));
    }
}
