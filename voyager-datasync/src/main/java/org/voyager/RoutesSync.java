package org.voyager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.datasync.RouteFR;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.service.RouteService;
import org.voyager.service.Voyager;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.DatasyncProgramArguments;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.voyager.utils.ConstantsLocal.ROUTE_AIRPORTS_FILE;
import static org.voyager.utils.HttpRequestUtils.fetchDocumentFromURL;

public class RoutesSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesSync.class);
    private static final Map<Airline,String> flightRadarSourceLive = Map.of(
            Airline.DELTA, "https://www.flightradar24.com/data/airlines/dl-dal/routes",
            Airline.JAPAN, "https://www.flightradar24.com/data/airlines/jl-jal/routes",
            Airline.NORWEGIAN,"https://www.flightradar24.com/data/airlines/dy-noz/routes"
    );
    private static RouteService routeService;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        int processLimit = datasyncProgramArguments.getProcessLimit();
        Airline airline = datasyncProgramArguments.getAirline();

        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);
        Voyager voyager = new Voyager(voyagerConfig);
        routeService = voyager.getRouteService();

        Document document = fetchDocumentFromURL(flightRadarSourceLive.get(airline));
        Object routesObject = getRoutesObjectFromDocument(document);
        String routesJson = convertRoutesObjectToRoutesJson(routesObject);
        List<RouteFR> docRoutes = extractRoutes(routesJson).stream()
                .limit(processLimit).toList();

        processRoutes(docRoutes);
        Set<String> airlineAirports = new HashSet<>();
        docRoutes.forEach(routeFR -> airlineAirports.add(routeFR.getAirport1().getIata()));
        ConstantsLocal.writeSetLineByLine(airlineAirports,ROUTE_AIRPORTS_FILE);
    }

    private static void processRoutes(List<RouteFR> docRoutes) {
        Either<ServiceError,List<Route>> getEither = routeService.getRoutes();
        if (getEither.isLeft()) {
            Exception exception = getEither.getLeft().getException();
            throw new RuntimeException(exception.getMessage(),exception);
        }

        List<Route> existingRoutes = getEither.get();
        Map<String,Route> exists = new HashMap<>();
        existingRoutes.forEach(route ->
                exists.put(String.format("%s:%s",route.getOrigin(),route.getDestination()),route));
        AtomicReference<Integer> created = new AtomicReference<>(0);
        docRoutes.forEach(routeFR -> processRouteFR(routeFR,exists,created));
        LOGGER.info(String.format("completed with existingRoutes count: %d and created count: %d",
                existingRoutes.size(),created.get()));
    }

    private static void processRouteFR(RouteFR routeFR,Map<String,Route> exists,AtomicReference<Integer> created) {
        String origin = routeFR.getAirport1().getIata();
        String destination = routeFR.getAirport2().getIata();
        String key = String.format("%s:%s",origin,destination);
        if (exists.containsKey(key)) {
            LOGGER.info("skipped existing route: " + exists.get(key));
        } else {
            RouteForm routeForm = RouteForm.builder()
                    .origin(origin)
                    .destination(destination)
                    .build();
            Either<ServiceError,Route> createEither = routeService.createRoute(routeForm);
            if (createEither.isLeft()) {
                Exception exception = createEither.getLeft().getException();
                LOGGER.error(exception.getMessage(),exception);
            } else {
                LOGGER.info("successfully created route: " + createEither.get());
                created.getAndSet(created.get() + 1);
            }
        }
    }

    private static Object getRoutesObjectFromDocument(Document document) {
        Element element = document.select("script").stream().filter(script -> script.html().contains("var arrRoutes=")).findFirst().orElse(null);
        if (element != null) {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("graal.js");
            try {
                engine.eval(element.html());
                Object arrRoutes = engine.get("arrRoutes");
                if (arrRoutes == null) {
                    String errorMessage = "'arrRoutes' returned null from script.";
                    LOGGER.error(errorMessage);
                    throw new RuntimeException(errorMessage);
                }
                return arrRoutes;
            } catch (ScriptException e) {
                LOGGER.error(String.format("Error while evaluating script. Message: %s. Script: %s", e.getMessage(), element.html()), e);
                throw new RuntimeException(e);
            }
        } else {
            String errorMessage = "Error selecting script from document. No match for 'var arrRoutes='";
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private static String convertRoutesObjectToRoutesJson(Object arrRoutes) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(arrRoutes);
        } catch (JsonProcessingException e) {
            LOGGER.error(String.format("Error while writing object as json string. Message: %s. Object: %s", e.getMessage(), arrRoutes.toString()), e);
            throw new RuntimeException(e);
        }
    }

    private static List<RouteFR> extractRoutes(String arrRoutesJsonString) {
        LOGGER.debug(arrRoutesJsonString);
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(arrRoutesJsonString, new TypeReference<List<RouteFR>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
