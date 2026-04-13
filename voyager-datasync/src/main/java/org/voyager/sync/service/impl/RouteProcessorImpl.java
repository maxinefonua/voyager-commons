package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.route.*;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.service.RouteSyncService;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.RouteProcessor;

import java.util.*;

public class RouteProcessorImpl implements RouteProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteProcessorImpl.class);
    private final RouteService routeService;
    private final RouteSyncService routeSyncService;
    private final AirportReference airportReference;
    private final Map<String,Route> mappedRoutes;
    private final List<Route> civilRouteList;

    public RouteProcessorImpl(
            RouteService routeService, RouteSyncService routeSyncService, AirportReference airportReference){
        this.routeService = routeService;
        this.routeSyncService = routeSyncService;
        this.airportReference = airportReference;
        this.civilRouteList = fetchCivilRoutesToProcess();
        this.mappedRoutes = loadSavedRouteMap(civilRouteList);
    }

    @Override
    public Either<ServiceError, Route> fetchOrCreateRoute(Airport originAirport, Airport destinationAirport) {
        Either<ServiceError, Route> either = routeService.getRoute(
                originAirport.getIata(),destinationAirport.getIata());
        if (either.isRight()) return either;

        ServiceError serviceError = either.getLeft();
        if (!serviceError.getHttpStatus().equals(HttpStatus.NOT_FOUND)) return either;

        Double distanceKM = Airport.calculateDistanceKm(originAirport.getLatitude(),originAirport.getLongitude(),
                destinationAirport.getLatitude(),destinationAirport.getLongitude());

        RouteForm routeForm = RouteForm.builder().origin(originAirport.getIata())
                .destination(destinationAirport.getIata()).distanceKm(distanceKM).build();
        Either<ServiceError,Route> createEither = routeService.createRoute(routeForm);
        if (createEither.isLeft()) return createEither;
        Route route = createEither.get();
        Either<ServiceError,RouteSync> routeSyncEither = routeSyncService.getByRouteId(route.getId());
        if (routeSyncEither.isLeft()) {
            LOGGER.error("failed to confirm route sync created, returning error: {}",
                    routeSyncEither.getLeft().getException().getMessage());
            return Either.left(routeSyncEither.getLeft());
        }
        LOGGER.info("successfully created route + sync record: {}",route);
        return createEither;
    }

    @Override
    public List<Route> fetchRoutesToProcess() {
        Either<ServiceError,List<RouteSync>> pendingEither = routeSyncService.getByStatus(Status.PENDING);
        if (pendingEither.isLeft()) {
            Exception exception = pendingEither.getLeft().getException();
            throw new RuntimeException(String.format("failed to fetching pending route syncs, error: %s",
                    exception.getMessage()),exception);
        }
        List<RouteSync> routeSyncList = pendingEither.get();
        if (routeSyncList.isEmpty()) {
            return confirmUserInputForCivilRouteList();
        }

        // convert pending to routes
        Map<Integer,Route> mappedCivilRoutes = new HashMap<>();
        civilRouteList.forEach(route->mappedCivilRoutes.put(route.getId(), route));
        List<Route> routeList = new ArrayList<>(routeSyncList.stream().map(routeSync ->
                mappedCivilRoutes.get(routeSync.getId())).toList());
        routeList.sort(Comparator.comparing(Route::getOrigin));
        return routeList;
    }

    private List<Route> confirmUserInputForCivilRouteList() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("no pending routes to process, load all civil routes for processing? y/n");
        String input = scanner.nextLine().trim();
        String valid = "yn";
        while (valid.indexOf(input.toLowerCase().charAt(0)) == -1) {
            System.out.println("please enter valid response, y/n?");
            input = scanner.nextLine().trim();
        }
        if (input.charAt(0) == 'n') {
            return List.of();
        }
        RouteSyncBatchUpdate routeSyncBatchUpdate = RouteSyncBatchUpdate.builder()
                .routeIdList(civilRouteList.stream().map(Route::getId).toList())
                .status(Status.PENDING)
                .build();
        Either<ServiceError, Integer> batchPending = routeSyncService.batchUpdate(routeSyncBatchUpdate);
        if (batchPending.isLeft()) {
            Exception exception = batchPending.getLeft().getException();
            throw new RuntimeException(String.format("failed to batch update %d routes to PENDING, error: %s",
                    routeSyncBatchUpdate.getRouteIdList().size(),exception.getMessage()),exception);
        }
        int updatedBatchCount = batchPending.get();
        LOGGER.info("successfully set {} route syncs to PENDING",updatedBatchCount);
        civilRouteList.sort(Comparator.comparing(Route::getOrigin));
        return civilRouteList;
    }

    @Override
    public Route fetchSavedCivilRoute(String origin, String destination) {
        return mappedRoutes.get(getRouteMapKey(origin,destination));
    }

    private String getRouteMapKey(String origin,String destination) {
        return String.format("%s:%s",origin,destination);
    }

    private List<Route> fetchCivilRoutesToProcess() {
        if (civilRouteList != null && !civilRouteList.isEmpty()) return civilRouteList;
        Either<ServiceError, List<Route>> routesEither = routeService.getRoutes();
        if (routesEither.isLeft()) {
            Exception exception = routesEither.getLeft().getException();
            throw new RuntimeException(String.format("failed to fetching all saved routes, error: %s",
                    exception.getMessage()),exception);
        }
        List<Route> routeList = routesEither.get();
        return new ArrayList<>(routeList.stream().filter(route ->
                airportReference.getCivilAirportOption(route.getOrigin()).isDefined() &&
                airportReference.getCivilAirportOption(route.getDestination()).isDefined())
                .toList());
    }

    private Map<String, Route> loadSavedRouteMap(List<Route> civilRouteList) {
        Map<String, Route> routeMap = new HashMap<>();
        civilRouteList.forEach(route -> {
            routeMap.put(getRouteMapKey(route.getOrigin(),route.getDestination()), route);
        });
        LOGGER.info("loaded {} routes from voyager", routeMap.size());
        return routeMap;
    }
}
