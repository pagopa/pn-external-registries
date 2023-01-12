package it.pagopa.pn.external.registries.mapper;

import it.pagopa.pn.external.registries.middleware.db.entities.OnboardInstitutionEntity;
import it.pagopa.pn.external.registries.middleware.queue.consumer.kafka.onboarding.onboarding.OnBoardingSelfCareDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OnBoardSelfCareToOnBoardInstituteEntityMapper {

    public OnboardInstitutionEntity toEntity(OnBoardingSelfCareDTO onBoardingSelfCareDTO) {
        OnboardInstitutionEntity entity = new OnboardInstitutionEntity();
        entity.setStatus(onBoardingSelfCareDTO.getState());
        entity.setLastUpdate(Instant.from(onBoardingSelfCareDTO.getUpdatedAt()));
        entity.setTaxCode(onBoardingSelfCareDTO.getInstitution().getTaxCode());
        entity.setAddress(onBoardingSelfCareDTO.getInstitution().getAddress());
        entity.setDigitalAddress(onBoardingSelfCareDTO.getInstitution().getDigitalAddress());
        entity.setDescription(onBoardingSelfCareDTO.getInstitution().getDescription());
        entity.setPk(onBoardingSelfCareDTO.getInternalIstitutionID());
        entity.setExternalId(onBoardingSelfCareDTO.getOnboardingTokenId());
        return entity;
    }
}
