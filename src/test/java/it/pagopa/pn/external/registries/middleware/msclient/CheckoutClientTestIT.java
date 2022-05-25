package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@EnableConfigurationProperties
@SpringBootTest(classes = {PnExternalRegistriesConfig.class, CheckoutClient.class})
class CheckoutClientTestIT {

    @Autowired
    private CheckoutClient client;

    @Mock
    private PnExternalRegistriesConfig cfg;

    @BeforeEach
    void setup() {
        Mockito.when( cfg.getCheckoutApiKey() ).thenReturn( "fake_apikey" );
        this.client = new CheckoutClient( cfg );
        this.client.init();
    }

    @Test
    void getPaymentInfo() {

        //Given

        //When
        client.getPaymentInfo( "77777777777302000100000019421" );

        //Then

    }

}