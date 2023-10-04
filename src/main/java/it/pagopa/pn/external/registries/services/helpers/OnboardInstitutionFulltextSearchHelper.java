package it.pagopa.pn.external.registries.services.helpers;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.mapper.OnboardInstitutionEntityToPaSummaryDto;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OnboardInstitutionFulltextSearchHelper {

    private final OnboardInstitutionsDao onboardInstitutionsDao;

    private final Map<String, PaSummaryDtoForSearch> localCache = new ConcurrentHashMap<>();

    private final int maxSearchResults;

    private final List<String> aooUoSenderID;
    private Instant mostRecentUpdate = null;

    public OnboardInstitutionFulltextSearchHelper(OnboardInstitutionsDao onboardInstitutionsDao, PnExternalRegistriesConfig cfg) {
        this.onboardInstitutionsDao = onboardInstitutionsDao;
        this.maxSearchResults = cfg.getFulltextsearchMaxResults();
        this.aooUoSenderID = Optional.ofNullable(cfg.getAoouoSenderId()).orElse(new LinkedList<>());
    }

    @PostConstruct
    public void init() {
        update();
    }

    @Scheduled(cron = "${pn.external-registry.fulltextsearch-update-cron-expression}")
    public void update(){
        try {
            log.info("updating institutions cache");

            Map<String, PaSummaryDtoForSearch> tempLocalCache = new ConcurrentHashMap<>();
            Map<String, Boolean> tempLocalCacheToRemove = new ConcurrentHashMap<>();
            AtomicBoolean atleastOneUpdate = new AtomicBoolean(false);
            List<Instant> timestamps = new ArrayList<>();
            onboardInstitutionsDao.getNewer(mostRecentUpdate)
                    .doOnNext(entity -> {
                        atleastOneUpdate.set(true);
                        timestamps.add(entity.getLastUpdate());

                        if (entity.isActive() && !aooUoSenderID.contains(entity.getInstitutionId())) {
                            PaSummaryDtoForSearch dtoForSearch = new PaSummaryDtoForSearch();
                            dtoForSearch.setDenominationForSearch(entity.getDescription().toLowerCase(Locale.ROOT));
                            dtoForSearch.setPaSummaryDto(OnboardInstitutionEntityToPaSummaryDto.toDto(entity));
                            tempLocalCache.put(entity.getInstitutionId(), dtoForSearch);
                        }
                        else {
                            tempLocalCacheToRemove.put(entity.getInstitutionId(), false);
                        }
                    }).blockLast();

            if (atleastOneUpdate.get()) {
                Optional<Instant> mostRecent =timestamps.stream().max(Comparator.comparing(instant -> instant));
                mostRecent.ifPresent(onboardInstitutionEntity -> this.mostRecentUpdate = mostRecent.get());

                this.localCache.putAll(tempLocalCache);
                if (!tempLocalCacheToRemove.isEmpty())
                {
                    for (String paId:
                            tempLocalCacheToRemove.keySet()) {
                        this.localCache.remove(paId);
                    }
                }

                log.info("update loaded institutions added={} removed={} total={}", tempLocalCache.size(), tempLocalCacheToRemove.size(), localCache.size());
            }
            else
            {
                log.info("update loaded, nothing to update");
            }
        } catch (Exception e) {
            log.error("Cannot update pa", e);
        }
    }

    public Flux<PaSummaryDto> fullTextSearch(String query) {
        final String fquery = query.toLowerCase(Locale.ROOT);
        return Flux.fromIterable(search(fquery));
    }

    @NotNull
    private List<PaSummaryDto> search(String fquery) {
        List<PaSummaryDto> results = localCache.values().stream().filter(x -> x.getDenominationForSearch().contains(fquery))
                .limit(this.maxSearchResults)
                .map(PaSummaryDtoForSearch::getPaSummaryDto)
                .sorted(Comparator.comparing(PaSummaryDto::getName))
                .collect(Collectors.toList());

        log.info("search query={} returned result size={}", fquery, results.size());
        return results;
    }

    @Data
    private static class PaSummaryDtoForSearch {
        private String denominationForSearch;
        private PaSummaryDto paSummaryDto;
    }
}
