package it.pagopa.pn.external.registries.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.dto.gpd.GPDPaymentInfoInt;
import it.pagopa.pn.external.registries.dto.CostUpdateResultRequestInt;
import it.pagopa.pn.external.registries.middleware.db.dao.CostUpdateResultDao;
import it.pagopa.pn.external.registries.middleware.db.entities.CommunicationResultEntity;
import it.pagopa.pn.external.registries.middleware.db.entities.CostUpdateResultEntity;
import it.pagopa.pn.external.registries.middleware.db.mapper.CommunicationResultGroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CostUpdateResultService {

    private final CostUpdateResultDao dao;
    private final CommunicationResultGroupMapper communicationResultGroupMapper;

    @Autowired
    public CostUpdateResultService(CostUpdateResultDao dao, CommunicationResultGroupMapper communicationResultGroupMapper) {
        this.dao = dao;
        this.communicationResultGroupMapper = communicationResultGroupMapper;
    }

    public Mono<String> createUpdateResult(CostUpdateResultRequestInt request) {
        if (request == null) {
            return Mono.error(new IllegalArgumentException("Request cannot be null"));
        }

        CostUpdateResultEntity entity = new CostUpdateResultEntity();

        String communicationResultGroup = communicationResultGroupMapper.mapToCommunicationResultGroup(request.getStatusCode()).getValue();
        entity.setCommunicationResultGroup(communicationResultGroup);

        entity.setPk(request.getCreditorTaxId() + "##" + request.getNoticeCode());
        entity.setSk(request.getUpdateCostPhase().getValue() + "##" +
                communicationResultGroup + "##" +
                UUID.randomUUID());

        entity.setRequestId(request.getRequestId());

        String resultEnum = communicationResultGroupMapper.getResultEnum(request.getStatusCode()).getValue();
        CommunicationResultEntity communicationResultEntity = new CommunicationResultEntity();
        communicationResultEntity.setStatusCode(request.getStatusCode());
        communicationResultEntity.setResultEnum(resultEnum);

        communicationResultEntity.setJsonResponse(this.cleanUpJsonResponse(request.getJsonResponse()));

        entity.setCommunicationResult(communicationResultEntity);

        entity.setFailedIuv("KO".equals(communicationResultGroup) ? request.getIun() : null);
        entity.setUpdateCostPhase(request.getUpdateCostPhase().getValue());
        entity.setNotificationCost(request.getNotificationCost());
        entity.setIun(request.getIun());
        entity.setEventTimestamp(request.getEventTimestamp());
        entity.setEventStorageTimestamp(request.getEventStorageTimestamp());
        entity.setCommunicationTimestamp(request.getCommunicationTimestamp());

        log.info("Inserting CostUpdateResultEntity: {}", entity);

        // insert and only return communication result group to the caller
        return dao.insertOrUpdate(entity)
                .map(e -> communicationResultGroup);
    }

    private String cleanUpJsonResponse(String jsonResponse) {
        if (jsonResponse == null) {
            return null;
        }

        // serialize the JSON response to GPDPaymentInfoInt and then back to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            var paymentInfo = objectMapper.readValue(jsonResponse, GPDPaymentInfoInt.class);

            // initialize the transfer list to an empty list, if null
            if (paymentInfo.getTransfer() == null) {
                paymentInfo.setTransfer(List.of());
            }

            // serialize back to JSON
            return objectMapper.writeValueAsString(paymentInfo);
        } catch (Exception e) {
            log.error("Error cleaning up JSON response", e);
            return jsonResponse;
        }
    }
}