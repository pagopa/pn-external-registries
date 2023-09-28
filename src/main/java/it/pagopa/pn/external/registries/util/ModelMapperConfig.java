package it.pagopa.pn.external.registries.util;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {


    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy( MatchingStrategies.STRICT );
        modelMapper.createTypeMap( it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.InstitutionResourceDto.class, it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.InstitutionResourceDto.class );
        modelMapper.createTypeMap( it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.ProductResourceDto.class, it.pagopa.pn.external.registries.generated.openapi.msclient.selfcare.v2.dto.ProductResourceDto.class );
        return modelMapper;
    }

}