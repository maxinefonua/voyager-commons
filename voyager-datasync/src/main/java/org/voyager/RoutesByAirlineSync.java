package org.voyager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.datasync.RouteFR;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.Voyager;
import org.voyager.service.RouteService;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.ConstantsUtils;
import org.voyager.utils.DatasyncProgramArguments;
import org.voyager.utils.HttpRequestUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.voyager.utils.ConstantsLocal.ROUTES_HTML_FILE;

public class RoutesByAirlineSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesByAirlineSync.class);
    private static final Map<Airline,String> flightRadarSourceLive = Map.of(
            Airline.DELTA, "https://www.flightradar24.com/data/airlines/dl-dal/routes"
    );
    private static Voyager voyager;
    private static RouteService routeService;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        int processLimit = datasyncProgramArguments.getProcessLimit();

        VoyagerConfig voyagerConfig = new VoyagerConfig(VoyagerConfig.Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);
        voyager = new Voyager(voyagerConfig);
        routeService = voyager.getRouteService();

        Airline airline = extractAirline(args);
        Document document = extractDocumentFromURLOrLocal(flightRadarSourceLive.get(airline),ROUTES_HTML_FILE);

        Object routesObject = getRoutesObjectFromDocument(document);
        String routesJson = convertRoutesObjectToRoutesJson(routesObject);
        List<RouteFR> routes = extractRoutes(routesJson).stream()
                .limit(processLimit).toList();

        List<Route> existing = routeService.getRoutes().get();
        Map<Integer,Route> existingMap = Collections.synchronizedMap(new HashMap<>());
        existing.forEach(route -> existingMap.put(route.getId(),route));
        Map<Integer,Route> processedMap = Collections.synchronizedMap(new HashMap<>());
        Map<Integer,Route> errorMap = Collections.synchronizedMap(new HashMap<>());
        List<RouteFR> errorFRList = Collections.synchronizedList(new ArrayList<>());

        routes.forEach(routeFR -> submitForCreateOrPatch(routeFR,airline,processedMap,errorMap,errorFRList));
//        TODO: add API call to invalidate all caches using routes and airline services
        processedMap.keySet().forEach(existingMap::remove);
        errorMap.keySet().forEach(existingMap::remove);
        existingMap.forEach((routeId,route) -> {
            if (route.getIsActive()) patchInactiveRoute(route,errorMap);
            else LOGGER.debug(String.format("skipping patch to INACTIVE of already inactive route %s",route));
        });

        errorMap.forEach((routeId,route) -> LOGGER.error(
                String.format("error patching existing %s",route)));
        errorFRList.forEach(routeFR -> {
            LOGGER.error(String.format("error fetching route for %s",routeFR));
        });
    }


    private static void submitForCreateOrPatch(RouteFR routeFR, Airline airline, Map<Integer, Route> processedMap, Map<Integer, Route> errorMap, List<RouteFR> errorFRList) {
        String origin = routeFR.getAirport1().getIata();
        String destination = routeFR.getAirport2().getIata();
        Either<ServiceError,List<Route>> exists = routeService.getRoutes(
                origin,destination,airline.name());
        if (exists.isLeft()) {
            LOGGER.error(String.format("Unexpected service error returned when fetching existing route for %s\nError: %s",
                    routeFR,exists.getLeft().getException().getMessage()));
            errorFRList.add(routeFR);
        } else if (exists.get().size() > 1) {
            exists.get().forEach(route -> {
                errorMap.put(route.getId(),route);
            });
            LOGGER.error(String.format("Unexpected multiple results returned for route form %s\nInvestigation needed",
                    routeFR));
        } else if (exists.get().size() == 1) {
            Route route = exists.get().get(0);
            if (route.getIsActive()) {
                LOGGER.debug(String.format("skipping patch to ACTIVE of already active route %s",route));
                processedMap.put(route.getId(),route);
            } else {
                patchActiveRoute(route,processedMap,errorMap);
            }
        } else createRoute(routeFR,origin,destination,airline);
    }

    private static void patchInactiveRoute(Route route, Map<Integer, Route> errorMap) {
        RoutePatch routePatch = RoutePatch.builder().isActive(false).build();
        Either<ServiceError, Route> either = routeService.patchRoute(route, routePatch);
        if (either.isLeft()) {
            errorMap.put(route.getId(),route);
            LOGGER.error(String.format("error during patch to INACTIVE of currently active route %s", route));
            LOGGER.error(String.format("error returned %s", either.getLeft().getException().getMessage()));
        } else {
            LOGGER.info(String.format("successful patch to INACTIVE previously active route %s", either.get()));
        }
    }

    private static void createRoute(RouteFR routeFR,String origin,String destination,Airline airline) {
        RouteForm routeForm = RouteForm.builder().origin(origin).destination(destination)
                .airline(airline.name()).isActive(true).build();
        Either<ServiceError,Route> either = routeService.createRoute(routeForm);
        if (either.isLeft()) {
            LOGGER.error(String.format("error creating new route %s",routeForm));
            LOGGER.error(String.format("from flight radar %s",routeFR));
            LOGGER.error(String.format("error returned %s",either.getLeft().getException().getMessage()));
        } else {
            LOGGER.info(String.format("successfully created new route %s",either.get()));
        }
    }

    private static void patchActiveRoute(Route route, Map<Integer, Route> processedMap, Map<Integer, Route> errorMap) {
        RoutePatch routePatch = RoutePatch.builder().isActive(true).build();
        Either<ServiceError, Route> either = routeService.patchRoute(route, routePatch);
        if (either.isLeft()) {
            errorMap.put(route.getId(),route);
            LOGGER.error(String.format("error during patch to ACTIVE of inactive route %s", route));
            LOGGER.error(String.format("error returned %s", either.getLeft().getException().getMessage()));
        } else {
            processedMap.put(route.getId(),route);
            LOGGER.info(String.format("successful patch to ACTIVE existing inactive route %s", either.get()));
        }
    }

    public static Document extractDocumentFromURLOrLocal(String routesURL, String routesHtmlFile) {
        Document document = null;
        try {
            document = fetchDocumentFromURL(routesURL);
            LOGGER.info("Successfully fetched document from url.");
            saveToResources(document,routesHtmlFile);
        } catch (RuntimeException e) {
            LOGGER.info("Error w fetching document from URL. Loading last saved document from resource file.");
            document = fetchDocumentFromResourceFile(routesHtmlFile);
        }
        return document;
    }

    private static Airline extractAirline(String[] args) {
        Set<String> accepted = new HashSet<>();
        Arrays.stream(Airline.values()).forEach(airline -> accepted.add(airline.name()));
        for (String arg : args) {
            if (arg.contains("-")) continue;
            if (accepted.contains(arg.toUpperCase())) {
                return Airline.valueOf(arg.toUpperCase());
            }
        }
        String errorMessage = "Missing or invalid argument for airline. Currently available airlines are: 'delta'";
        LOGGER.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }

    private static void saveToResources(Document document,String fileName) {
        String filePath = ConstantsLocal.class.getClassLoader().getResource(fileName).getFile();
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(document.html());
            fileWriter.close();
            LOGGER.info(String.format("Successfully saved document to filePath: %s",filePath));
        } catch (IOException e) {
            LOGGER.error(String.format("Error writing to filePath: %s. Error message: %s", filePath,e.getMessage()),e);
            LOGGER.info("Continuing to process routes without saving fetched document to resources");
        }
    }

    public static Document fetchDocumentFromResourceFile(String fileName) {
        try (InputStream inputStream = ConstantsUtils.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                LOGGER.info(String.format("InputStream returned null with fileName: %s",fileName));
                throw new IOException("Resource not found: " + fileName);
            }
            return Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "");
        } catch (IOException e) {
            LOGGER.info(String.format("IOException thrown fetching resource fileName: %s. Error message: %s",fileName,e.getMessage()),e);
            throw new RuntimeException(e);
        }
    }

    private static Document fetchDocumentFromURL(String routesURL) {
        try {
            return HttpRequestUtils.getHTMLDocFromURL(routesURL, Map.of());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error(String.format("Error while getting response from URL '%s'. Message: %s",routesURL,e.getMessage()),e);
            throw new RuntimeException(e);
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
