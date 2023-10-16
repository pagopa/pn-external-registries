package it.pagopa.pn.external.registries.middleware.db.mapper;

import it.pagopa.pn.external.registries.dto.CommunicationResultGroupInt;
import it.pagopa.pn.external.registries.dto.CostUpdateResultResponseInt;
import org.springframework.stereotype.Component;

@Component
public class CommunicationResultGroupMapper {

    public CommunicationResultGroupInt mapToCommunicationResultGroup(int statusCode) {
        CostUpdateResultResponseInt resultEnum = getResultEnum(statusCode);
        return switch (resultEnum) {
            case OK_UPDATED, OK_IN_PAYMENT -> CommunicationResultGroupInt.OK;
            case KO_NOT_FOUND, KO_CANNOT_UPDATE -> CommunicationResultGroupInt.KO;
            case KO_RETRY -> CommunicationResultGroupInt.RETRY;
        };
    }

    public CostUpdateResultResponseInt getResultEnum(int statusCode) {
        return switch (statusCode) {
            case 200 -> CostUpdateResultResponseInt.OK_UPDATED;
            // IUV not present on GPD or EC not present
            case 404 -> CostUpdateResultResponseInt.KO_NOT_FOUND;
            case 422 -> CostUpdateResultResponseInt.KO_CANNOT_UPDATE;
            // TODO: to be changed once we have a specific status code for OK_IN_PAYMENT
            default -> statusCode >= 200 && statusCode < 300 ? CostUpdateResultResponseInt.OK_IN_PAYMENT : CostUpdateResultResponseInt.KO_RETRY;
        };
    }
}
