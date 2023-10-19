package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.middleware.db.dao.CostUpdateResultDao;
import it.pagopa.pn.external.registries.middleware.db.mapper.CommunicationResultGroupMapper;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UpdateCostServiceTestIT {
    private UpdateCostService updateCostService;

    @Mock
    private GpdClient gpdClient;

    @Mock
    private CostUpdateResultDao costUpdateResultDao;

    private CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CostUpdateResultService costUpdateResultService = new CostUpdateResultService(costUpdateResultDao, communicationResultGroupMapper);
        updateCostService = new UpdateCostService(gpdClient, costUpdateResultService);
    }
}
