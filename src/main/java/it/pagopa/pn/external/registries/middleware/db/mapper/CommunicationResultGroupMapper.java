package it.pagopa.pn.external.registries.middleware.db.mapper;

import org.springframework.stereotype.Component;

@Component
public class CommunicationResultGroupMapper {

    public String mapToCommunicationResultGroup(int statusCode) {
        return switch (getResultEnum(statusCode)) {
            case "OK_UPDATED", "OK_IN_PAYMENT" -> "OK";
            case "KO_NOT_FOUND", "KO_CANNOT_UPDATE" -> "KO";
            case "KO_RETRY" -> "RETRY";
            default -> "UNKNOWN";
        };
    }

    public String getResultEnum(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK_UPDATED";
            case 404 -> "KO_NOT_FOUND";
            case 422 -> "KO_CANNOT_UPDATE";
            default -> statusCode >= 200 && statusCode < 300 ? "OK_IN_PAYMENT" : "KO_RETRY";
        };
    }
}
