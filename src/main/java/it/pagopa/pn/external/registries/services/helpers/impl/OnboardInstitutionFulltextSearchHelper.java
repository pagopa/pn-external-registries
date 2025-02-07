package it.pagopa.pn.external.registries.services.helpers.impl;

import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryExtendedInfoDto;
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
import reactor.core.publisher.Mono;

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

    private Instant childrenMostRecentUpdate = null;


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
        updateInstitutionsCache();
        updateInstitutionsChildrenCache();
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

    private void updateInstitutionsCache() {
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

                this.institutionsCache.putAll(tempLocalCache);
                if (!tempLocalCacheToRemove.isEmpty())
                {
                    for (String paId:
                            tempLocalCacheToRemove.keySet()) {
                        this.institutionsCache.remove(paId);
                    }
                }

                log.info("update loaded institutions added={} removed={} total={}", tempLocalCache.size(), tempLocalCacheToRemove.size(), institutionsCache.size());
            }
            else
            {
                log.info("update loaded, nothing to update");
            }
        } catch (Exception e) {
            log.error("Cannot update pa", e);
        }
    }

    /**
     * Performs a full-text search on PA entities using the local cache.
     * If search is set to "onlyChildren", it will return only children of PAs.
     *
     * @param toQuery The search text that will be matched against the PA names. If null, it will be considered an empty string.
     * @param onlyChildren If true, returns only the children of the PAs; if false, returns both the parents and their children.
     * @return A {@link Flux} of {@link PaSummaryExtendedDto} containing filtered search results.
     */
    @Override
    public Flux<PaSummaryExtendedDto> extendedFullTextSearch(String toQuery, Boolean onlyChildren) {
        String query;
        if(toQuery != null) {
            query = toQuery.toLowerCase(Locale.ROOT);
        } else
            query = "";
        return searchExtended(query, onlyChildren);
    }

    /**
     * Performs an extended search of PAs based on a `query` search filter.
     * If the `onlyChildren` parameter is set to `true`, returns only the children of the PAs;
     * otherwise, returns both parents and their children.
     *
     * @param query The search text to match against PA names. If empty, returns all PAs.
     * @param onlyChildren If `true`, returns only the children of the PAs; if `false`, returns both the parents and their children.
     * @return A {@link Flux} of {@link PaSummaryExtendedDto} containing the search results.
     */
    private Flux<PaSummaryExtendedDto> searchExtended(String query, Boolean onlyChildren) {
        if (Boolean.TRUE.equals(onlyChildren)) {
            return getChildren(query).flux();
        } else {
            return getParentsAndChildren(query);
        }
    }

    /**
     * Retrieves a list of PA children based on a search filter.
     * The method extracts the PA children from the local cache, applies the search filter if present,
     * sorts the results and returns a single object {@link PaSummaryExtendedDto} containing the list of children found.
     *
     * @param query The search text to filter the names of the children of the PAs. If empty, returns all children.
     * @return A {@link Mono} containing a {@link PaSummaryExtendedDto} with the list of children.
     */
    private Mono<PaSummaryExtendedDto> getChildren(String query) {
        return Flux.fromIterable(institutionsChildrenCache.values())
                .filter(son -> query.isEmpty() || son.getPaSummaryExtendedDto().getName().toLowerCase(Locale.ROOT).contains(query))
                .map(son -> new PaSummaryExtendedInfoDto()
                        .id(son.getPaSummaryExtendedDto().getId())
                        .name(son.getPaSummaryExtendedDto().getName()))
                .collectList()
                .filter(children -> !children.isEmpty())
                .map(children -> {
                    children.sort(Comparator.comparing(PaSummaryExtendedInfoDto::getName));
                    PaSummaryExtendedDto responseDto = new PaSummaryExtendedDto();
                    responseDto.setChildrenList(children);
                    log.info("GetChildren() query={} returned result size={}", query, children.size());
                    return responseDto;
                });
    }

    /**
     * Retrieves a list of PAs that have at least one associated child,
     * applying a search filter on the names. The method returns a stream of objects
     * {@link PaSummaryExtendedDto}, containing the PA data and the related lists of children.
     *
     * @param query The search text to filter PA names. If empty, returns all PAs.
     * @return A {@link Flux} of {@link PaSummaryExtendedDto} containing the PAs with their filtered children.
     */
    private Flux<PaSummaryExtendedDto> getParentsAndChildren(String query) {
        return Flux.fromIterable(institutionsCache.values())
                .filter(parent -> query.isEmpty() || parent.getDenominationForSearch().contains(query))
                .flatMap(parent -> {
                    PaSummaryExtendedDto dto = new PaSummaryExtendedDto();
                    dto.setId(parent.getPaSummaryDto().getId());
                    dto.setName(parent.getPaSummaryDto().getName());

                    return Flux.fromIterable(institutionsChildrenCache.values())
                            .filter(son -> son.getRootIdForSearch().equals(parent.getPaSummaryDto().getId()))
                            .map(son -> new PaSummaryExtendedInfoDto()
                                    .id(son.getPaSummaryExtendedDto().getId())
                                    .name(son.getPaSummaryExtendedDto().getName()))
                            .collectList()
                            .map(children -> {
                                children.sort(Comparator.comparing(PaSummaryExtendedInfoDto::getName));
                                if (!children.isEmpty()) {
                                    dto.setChildrenList(children);
                                } else {
                                    dto.setChildrenList(null);
                                }
                                return dto;
                            });
                })
                .collectList()
                .map(parents -> {
                    parents.sort(Comparator.comparing(PaSummaryExtendedDto::getName));
                    log.info("GetParentsAndChildren() query={} returned result size={}", query, parents.size());
                    return parents;
                })
                .flatMapMany(Flux::fromIterable);
    }

    /**
     * Updates the cache of child institutions by retrieving the latest data from the database and updates the cache only if there is new data.
     *
     * <p>The method performs an asynchronous query to get the updated child institutions
     * and places them in a temporary cache. After the retrieval is complete, it updates the main cache
     * by removing old items and adding new ones.</p>
     *
     * <p>If there is no new data, the method exits without updating the cache.</p>
     */
    private void updateInstitutionsChildrenCache() {
        log.info("Updating institutions children cache");
        try {
            Map<String, PaSummaryExtendedDtoForSearch> tempLocalCache = new ConcurrentHashMap<>();
            Map<String, Boolean> tempLocalCacheToRemove = new ConcurrentHashMap<>();
            AtomicBoolean atleastOneUpdate = new AtomicBoolean(false);
            List<Instant> timestamps = new ArrayList<>();

            onboardInstitutionsDao.getNewerChildren(childrenMostRecentUpdate)
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
                mostRecent.ifPresent(onboardInstitutionEntity -> childrenMostRecentUpdate = mostRecent.get());

                institutionsChildrenCache.putAll(tempLocalCache);
                if (!tempLocalCacheToRemove.isEmpty()) {
                    for (String paId: tempLocalCacheToRemove.keySet()) {
                        institutionsChildrenCache.remove(paId);
                    }
                }

                log.info("Update loaded institutions children - added={} removed={} total={}", tempLocalCache.size(), tempLocalCacheToRemove.size(), institutionsChildrenCache.size());
            }
            else {
                log.info("Updated loaded - nothing to update");
            }
        } catch (Exception exception) {
            log.error("Error updating institutions children cache", exception);
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