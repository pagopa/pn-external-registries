package it.pagopa.pn.external.registries.api.v1.mock;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.pagopa.pn.external.registries.exceptions.InternalErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class MockResponsesHolder {

    private final String mockResourceUri;
    private final ApplicationContext ctx;
    private Resource mockResource;
    private MockResponses mockData;

    public MockResponsesHolder(ApplicationContext ctx,  @Value("${pn.external-registry.mock-data-resources:}") String mockResourceUri) {
        this.ctx = ctx;
        this.mockResourceUri = mockResourceUri;
    }

    @PostConstruct
    public void init() {
        if( StringUtils.hasText( this.mockResourceUri) ) {
            this.mockResource = ctx.getResource( mockResourceUri );
            this.mockData = loadMockResponses( this.mockResource );
        }
    }

    public MockResponses getMockData() {
        return mockData;
    }

    private static MockResponses loadMockResponses(Resource yamlResource )  {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.findAndRegisterModules();

        try(InputStream inStream = yamlResource.getInputStream() ) {
            return mapper.readValue( inStream, MockResponses.class);
        } catch (IOException e) {
            log.error("Cannot load mock responses!!", e);
            throw new InternalErrorException();
        }
    }

}
