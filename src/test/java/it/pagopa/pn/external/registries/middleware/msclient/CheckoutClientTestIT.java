package it.pagopa.pn.external.registries.middleware.msclient;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.checkout.client.v1.dto.PaymentRequestsGetResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties
@SpringBootTest(classes = {PnExternalRegistriesConfig.class, CheckoutClient.class})
class CheckoutClientTestIT {

    final String CHECKOUT_BASE_URL = "https://api.dev.platform.pagopa.it/checkout/auth/payments/v2";

    @Autowired
    private CheckoutClient client;

    @Mock
    private PnExternalRegistriesConfig cfg;

    @BeforeEach
    void setup() {
        Mockito.when( cfg.getCheckoutApiKey() ).thenReturn( "fake_apikey" );
        Mockito.when( cfg.getCheckoutBaseUrl() ).thenReturn( CHECKOUT_BASE_URL );
        this.client = new CheckoutClient( cfg );
        this.client.init();
    }

    @Test
    void getPaymentInfo() {

        //When
        PaymentRequestsGetResponseDto response = client.getPaymentInfo( "77777777777302000100000019421" ).block();

        //Then
        Assertions.assertNotNull( response );
        Assertions.assertEquals( 1200 , response.getImportoSingoloVersamento() );
    }

}