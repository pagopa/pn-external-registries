package it.pagopa.pn.external.registries.services;

import it.pagopa.pn.external.registries.middleware.db.dao.CostComponentsDao;
import it.pagopa.pn.external.registries.middleware.db.dao.CostUpdateResultDao;
import it.pagopa.pn.external.registries.middleware.db.mapper.CommunicationResultGroupMapper;
import it.pagopa.pn.external.registries.middleware.db.mapper.CostComponentsMapper;
import it.pagopa.pn.external.registries.middleware.msclient.gpd.GpdClient;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CostUpdateOrchestratorServiceTestIT {
    private CostUpdateOrchestratorService costUpdateOrchestratorService;

    @Mock
    private CostUpdateResultDao costUpdateResultDao;
    @Mock
    private CostComponentsDao costComponentsDao;

    @Mock
    private GpdClient gpdClient;

    private UpdateCostService updateCostService;
    private CostComponentService costComponentService;

    private CostUpdateResultService costUpdateResultService;

    private final CostComponentsMapper costComponentsMapper = new CostComponentsMapper();

    private final CommunicationResultGroupMapper communicationResultGroupMapper = new CommunicationResultGroupMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.costComponentService = new CostComponentService(costComponentsDao, costComponentsMapper);
        this.costUpdateResultService = new CostUpdateResultService(costUpdateResultDao, communicationResultGroupMapper);
        this.updateCostService = new UpdateCostService(gpdClient, costUpdateResultService);
        this.costUpdateOrchestratorService = new CostUpdateOrchestratorService(costComponentService, updateCostService);
    }
}
