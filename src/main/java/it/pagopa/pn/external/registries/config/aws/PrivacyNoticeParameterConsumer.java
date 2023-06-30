package it.pagopa.pn.external.registries.config.aws;

import it.pagopa.pn.commons.abstractions.impl.AbstractCachedSsmParameterConsumer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;

@Component
public class PrivacyNoticeParameterConsumer extends AbstractCachedSsmParameterConsumer {

    public PrivacyNoticeParameterConsumer(SsmClient ssmClient) {
        super(ssmClient);
    }

}
