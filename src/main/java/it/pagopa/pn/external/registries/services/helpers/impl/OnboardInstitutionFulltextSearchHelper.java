package it.pagopa.pn.external.registries.services.helpers.impl;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedDto;
import it.pagopa.pn.external.registries.mapper.OnboardInstitutionEntityToPaSummaryDto;
import it.pagopa.pn.external.registries.mapper.OnboardInstitutionEntityToPaSummaryExtendedDtoMapper;
import it.pagopa.pn.external.registries.middleware.db.dao.OnboardInstitutionsDao;
import it.pagopa.pn.external.registries.services.helpers.PaExtendedFullText;
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
public class OnboardInstitutionFulltextSearchHelper implements PaExtendedFullText {

    private final OnboardInstitutionsDao onboardInstitutionsDao;

    private final Map<String, PaSummaryDtoForSearch> institutionsCache = new ConcurrentHashMap<>();

    private final Map<String, PaSummaryExtendedDtoForSearch> institutionsChildrenCache = new ConcurrentHashMap<>();

    private final int maxSearchResults;

    private final List<String> aooUoSenderID;

    private Instant mostRecentUpdate = null;

    private Instant extendedMostRecentUpdate = null;

    public OnboardInstitutionFulltextSearchHelper(OnboardInstitutionsDao onboardInstitutionsDao, PnExternalRegistriesConfig cfg) {
        this.onboardInstitutionsDao = onboardInstitutionsDao;
        this.maxSearchResults = cfg.getFulltextsearchMaxResults();
        this.aooUoSenderID = Optional.ofNullable(cfg.getAoouosenderid()).orElse(new LinkedList<>());
    }

    @PostConstruct
    public void init() {
        update();
    }

    @Scheduled(cron = "${pn.external-registry.fulltextsearch-update-cron-expression}")
    public void update(){
        try {
            log.info("updating institutions cache");
            updateInstitutionsCache();
            updateInstitutionsChildrenCache();
        } catch (Exception e) {
            log.error("Cannot update pa", e);
        }
    }


    private void updateInstitutionsCache() {
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

            this.institutionsCache.putAll(tempLocalCache);
            if (!tempLocalCacheToRemove.isEmpty()) {
                for (String paId: tempLocalCacheToRemove.keySet()) {
                    this.institutionsCache.remove(paId);
                }
            }

            log.info("update loaded institutions added={} removed={} total={}", tempLocalCache.size(), tempLocalCacheToRemove.size(), institutionsCache.size());
        }
        else {
            log.info("update loaded, nothing to update");
        }
    }

    public Flux<PaSummaryDto> fullTextSearch(String query) {
        final String fquery = query.toLowerCase(Locale.ROOT);
        return Flux.fromIterable(search(fquery));
    }

    @NotNull
    private List<PaSummaryDto> search(String fquery) {
        List<PaSummaryDto> results = institutionsCache.values().stream().filter(x -> x.getDenominationForSearch().contains(fquery))
                .limit(this.maxSearchResults)
                .map(PaSummaryDtoForSearch::getPaSummaryDto)
                .sorted(Comparator.comparing(PaSummaryDto::getName))
                .collect(Collectors.toList());

        log.info("search query={} returned result size={}", fquery, results.size());
        return results;
    }

    @Override
    public Flux<PaSummaryExtendedDto> extendedFullTextSearch(String query, Boolean onlyChildren) {
        String fquery;
        if(query != null) {
            fquery = query.toLowerCase();
        } else
            fquery = "";
        return Flux.fromIterable(searchExtended(fquery, onlyChildren));
    }

    @NotNull
    private List<PaSummaryExtendedDto> searchExtended(String fquery, Boolean onlyChildren) {
        return institutionsCache.values().stream()
                .filter(parent -> fquery.isEmpty() || parent.getDenominationForSearch().contains(fquery))
                .map(parent -> {
                    PaSummaryExtendedDto dto = new PaSummaryExtendedDto();
                    dto.setId(parent.getPaSummaryDto().getId());
                    dto.setName(parent.getPaSummaryDto().getName());
                    return dto;
                })
                .sorted(Comparator.comparing(PaSummaryExtendedDto::getName))
                .collect(Collectors.toList());
    }

    private void updateInstitutionsChildrenCache() {
        Map<String, PaSummaryExtendedDtoForSearch> tempLocalCache = new ConcurrentHashMap<>();
        Map<String, Boolean> tempLocalCacheToRemove = new ConcurrentHashMap<>();
        AtomicBoolean atleastOneUpdate = new AtomicBoolean(false);
        List<Instant> timestamps = new ArrayList<>();

        onboardInstitutionsDao.getNewerChildren(extendedMostRecentUpdate)
                .doOnNext(entity -> {
                    atleastOneUpdate.set(true);
                    timestamps.add(entity.getLastUpdate());

                    if (entity.isActive() && !aooUoSenderID.contains(entity.getInstitutionId())) {
                        PaSummaryExtendedDtoForSearch dtoForSearch = new PaSummaryExtendedDtoForSearch();
                        dtoForSearch.setDenominationForSearch(entity.getDescription().toLowerCase(Locale.ROOT));
                        dtoForSearch.setRootIdForSearch(entity.getRootId());
                        dtoForSearch.setPaSummaryExtendedDto(OnboardInstitutionEntityToPaSummaryExtendedDtoMapper.toDto(entity));
                        tempLocalCache.put(entity.getInstitutionId(), dtoForSearch);
                    }
                    else {
                        tempLocalCacheToRemove.put(entity.getInstitutionId(), false);
                    }
                }).blockLast();

        if (atleastOneUpdate.get()) {
            Optional<Instant> mostRecent =timestamps.stream().max(Comparator.comparing(instant -> instant));
            mostRecent.ifPresent(onboardInstitutionEntity -> this.extendedMostRecentUpdate = mostRecent.get());

            this.institutionsChildrenCache.putAll(tempLocalCache);
            if (!tempLocalCacheToRemove.isEmpty()) {
                for (String paId: tempLocalCacheToRemove.keySet()) {
                    this.institutionsChildrenCache.remove(paId);
                }
            }

            log.info("update loaded institutions added={} removed={} total={}", tempLocalCache.size(), tempLocalCacheToRemove.size(), institutionsChildrenCache.size());
        }
        else {
            log.info("update loaded, nothing to update");
        }
    }

    @Data
    private static class PaSummaryDtoForSearch {
        private String denominationForSearch;
        private PaSummaryDto paSummaryDto;
    }

    @Data
    private static class PaSummaryExtendedDtoForSearch {
        private String rootIdForSearch;
        private String denominationForSearch;
        private PaSummaryExtendedDto paSummaryExtendedDto;
    }
}
