package org.voyager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.model.Airline;
import org.voyager.model.RouteJson;
import org.voyager.model.delta.DeltaDisplay;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.delta.DeltaStatus;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.VoyagerAPI;
import org.voyager.service.impl.VoyagerAPIService;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.ConstantsUtils;
import org.voyager.utils.HttpRequestUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.voyager.utils.ConstantsLocal.ROUTES_HTML_FILE;

public class RoutesAndAirlineSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesAndAirlineSync.class);
    private static final Map<Airline,String> airlineToURLMap = Map.of(
            Airline.DELTA, "https://www.flightradar24.com/data/airlines/dl-dal/routes"
    );
    private static VoyagerAPI voyagerAPI;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        Airline airline = extractAirlineFromArgs(args);
        Integer maxConcurrentRequests = extractMaxConcurrentRequestsFromArgs(args);
        voyagerAPI = new VoyagerAPIService(maxConcurrentRequests);
        String routesURL = airlineToURLMap.get(airline);
        Document document = extractDocumentFromURLOrLocal(routesURL,ROUTES_HTML_FILE);
        Object routesObject = getRoutesObjectFromDocument(document);
        String routesJson = convertRoutesObjectToRoutesJson(routesObject);
        List<RouteForm> routeForms = buildRouteFormsFromRoutesJson(routesJson,airline);
        processRouteFormsAndDBRoutes(routeForms,getRoutesFromAPI());
        processAirlineCodes(airline);
    }

    private static void processAirlineCodes(Airline airline) {
        List<String> civilIataCodes =  new ArrayList<>();
        List<DeltaDisplay> addedDeltas = Collections.synchronizedList(new ArrayList<>());
        List<DeltaDisplay> patchedDeltasToActive = Collections.synchronizedList(new ArrayList<>());
        List<DeltaDisplay> patchedDeltasToInactive = Collections.synchronizedList(new ArrayList<>());
        List<String> interruptedIata = Collections.synchronizedList(new ArrayList<>());

        try {
            civilIataCodes.addAll(voyagerAPI.getAllCivilIata());
            civilIataCodes.forEach(iata -> {
                try {
                    Boolean isActiveAirlineOrigin = voyagerAPI.hasActiveRoutesFrom(iata,airline);
                    DeltaDisplay deltaDisplay = voyagerAPI.getDelta(iata);
                    if (isActiveAirlineOrigin) {
                        if (deltaDisplay == null) {
                            DeltaForm deltaForm = DeltaForm.builder()
                                    .iata(iata)
                                    .status(DeltaStatus.ACTIVE.name())
                                    .isHub(false)
                                    .build();
                            try {
                                deltaDisplay = voyagerAPI.addDelta(deltaForm);
                                addedDeltas.add(deltaDisplay);
                                LOGGER.debug("added: " + deltaDisplay);
                            } catch (Exception e) {
                                LOGGER.error(String.format("InterrupedException thrown while processing %s. Error message: %s", deltaForm, e.getMessage()), e);
                                interruptedIata.add(iata);
                            }
                        } else if (!Set.of(DeltaStatus.ACTIVE,DeltaStatus.SEASONAL).contains(deltaDisplay.getStatus())){
                            DeltaPatch deltaPatch = DeltaPatch.builder().isHub(false).status(DeltaStatus.ACTIVE.name()).build();
                            deltaDisplay = voyagerAPI.patchDelta(iata,deltaPatch);
                            patchedDeltasToActive.add(deltaDisplay);
                        }
                    } else if (deltaDisplay != null && Set.of(DeltaStatus.ACTIVE,DeltaStatus.SEASONAL).contains(deltaDisplay.getStatus())) {
                        DeltaPatch deltaPatch = DeltaPatch.builder().isHub(deltaDisplay.getIsHub()).status(DeltaStatus.TERMINATED.name()).build();
                        deltaDisplay = voyagerAPI.patchDelta(iata,deltaPatch);
                        patchedDeltasToInactive.add(deltaDisplay);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error(String.format("InterrupedException thrown while processing %s. Error message: %s", iata, e.getMessage()), e);
                    interruptedIata.add(iata);
                }
            });
        } catch (InterruptedException e) {
            LOGGER.error(String.format("InterrupedException thrown while fetching all detla displays. Error: %s", e.getMessage()), e);
        }

        LOGGER.info("************");
        LOGGER.info(String.format("total iata civil codes: %d, addedDeltas: %d, patchedDeltasToActive: %d, patchedDeltasToInactive: %d ",civilIataCodes.size(),addedDeltas.size(),patchedDeltasToActive.size(),patchedDeltasToInactive.size()));
    }

    private static List<RouteDisplay> getRoutesFromAPI() {
        List<RouteDisplay> existing = new ArrayList<>();
        try {
            HttpResponse<String> response = voyagerAPI.getRoutes();
            if (response.statusCode() == 200) {
                String json = response.body();
                ObjectMapper om = new ObjectMapper();
                try {
                    RouteDisplay[] results = om.readValue(json, RouteDisplay[].class);
                    existing.addAll(Arrays.stream(results).toList());
                } catch (JsonProcessingException e) {
                    LOGGER.error(String.format("Error reading json of existing routes: %s. Message: %s. Returning empty list for existing routes", json, e.getMessage()), e);
                }
            } else {
                LOGGER.error(String.format("Non-200 returned from URI: '%s'. Returning empty list for existing routes.",response.uri().toString()));
            }
        } catch (InterruptedException e) {
            LOGGER.info("Error w fetching existing routes from Voyager API. Returning empty list for existing routes.",e);
        }
        return existing;
    }

    public static Document extractDocumentFromURLOrLocal(String routesURL, String routesHtmlFile) {
        Document document = null;
        try {
            document = getDocumentFromURL(routesURL,Map.of());
            LOGGER.info("Successfully fetched document from url.");
            saveToResources(document,routesHtmlFile);
        } catch (RuntimeException e) {
            LOGGER.info("Error w fetching document from URL. Loading last saved document from resource file.");
            document = fetchDocumentFromResourceFile(routesHtmlFile);
        }
        return document;
    }

    private static Integer extractMaxConcurrentRequestsFromArgs(String[] args) {
        try {
            return Integer.valueOf(args[1].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            String errorMessage = "Missing or invalid second argument for maxConcurrentRequests. Provide a valid integer value";
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private static Airline extractAirlineFromArgs(String[] args) {
        try {
            return Airline.valueOf(args[0].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            String errorMessage = "Missing or invalid first argument for airline. Currently available airlines are: 'delta'";
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
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

    private static Document getDocumentFromURL(String routesURL, Map<Object, Object> of) {
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

    private static List<RouteForm> buildRouteFormsFromRoutesJson(String arrRoutesJsonString, Airline airline) {
        LOGGER.debug(arrRoutesJsonString);
        ObjectMapper om = new ObjectMapper();
        try {
            RouteJson[] routeJsons = om.readValue(arrRoutesJsonString, RouteJson[].class);
            return Arrays.stream(routeJsons).map(
                    routeJson -> RouteForm.builder()
                            .origin(routeJson.getAirport1().getIata())
                            .destination(routeJson.getAirport2().getIata())
                            .airline(airline.name())
                            .isActive(true)
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processRouteFormsAndDBRoutes(List<RouteForm> routeForms, List<RouteDisplay> dbRoutes) {
        List<Integer> addedIds = Collections.synchronizedList(new ArrayList<>());
        List<Integer> patchedToInactiveIds = Collections.synchronizedList(new ArrayList<>());
        List<Integer> existingIds = Collections.synchronizedList(new ArrayList<>());
        List<Integer> skippedActiveIds = Collections.synchronizedList(new ArrayList<>());
        List<Integer> skippedInactiveIds = Collections.synchronizedList(new ArrayList<>());
        List<RouteForm> interruptedRouteFormsOnPatchCreate = Collections.synchronizedList(new ArrayList<>());
        List<RouteDisplay> interruptedRouteDisplaysOnPatchInactive = Collections.synchronizedList(new ArrayList<>());
        AtomicReference<Integer> successfullyProcessedForms = new AtomicReference<>(0);
        AtomicReference<Integer> successfullyProcessedDisplays = new AtomicReference<>(0);
        int startingDbRoutes = dbRoutes.size();

        List<Integer> patchedToActiveIds = Collections.synchronizedList(new ArrayList<>());
        routeForms.forEach(routeForm -> {
                try {
                    RouteDisplay existingRoute = voyagerAPI.getRoute(routeForm);
                    if (existingRoute != null) {
                        if (existingRoute.getIsActive()) {
                            LOGGER.debug("skipping already active: " + existingRoute);
                            skippedActiveIds.add(existingRoute.getId());
                        } else {
                            RouteDisplay patched = voyagerAPI.patchRoute(existingRoute, RoutePatch.builder().isActive(true).build());
                            LOGGER.debug("patched to active: " + patched);
                            patchedToActiveIds.add(patched.getId());
                        }
                        existingIds.add(existingRoute.getId());
                        dbRoutes.remove(existingRoute);
                        successfullyProcessedDisplays.getAndSet(successfullyProcessedDisplays.get()+1);
                    } else {
                        RouteDisplay created = voyagerAPI.addRoute(routeForm);
                        LOGGER.debug("created non-existing route: " + created);
                        addedIds.add(created.getId());
                    }
                    successfullyProcessedForms.getAndSet(successfullyProcessedForms.get()+1);
                } catch (InterruptedException e) {
                    LOGGER.error(String.format("InterrupedException thrown while processing %s. Error message: %s",routeForm,e.getMessage()),e);
                    interruptedRouteFormsOnPatchCreate.add(routeForm);
                }
        });

        dbRoutes.forEach(routeDisplay -> {
            if (routeDisplay.getIsActive()) {
                LOGGER.debug(String.format("Patching as inactive: %s", routeDisplay));
                RoutePatch routePatch = RoutePatch.builder().isActive(false).build();
                try {
                    RouteDisplay patchedRoute = voyagerAPI.patchRoute(routeDisplay, routePatch);
                    LOGGER.debug(String.format("Patched route: %s", patchedRoute.toString()));
                    patchedToInactiveIds.add(patchedRoute.getId());
                } catch (InterruptedException e) {
                    LOGGER.error(String.format("InterrupedException thrown while processing %s. Error message: %s", routeDisplay, e.getMessage()), e);
                    interruptedRouteDisplaysOnPatchInactive.add(routeDisplay);
                }
            } else {
                LOGGER.debug(String.format("Skipping patch to inactive, since already inactive: %s", routeDisplay));
                skippedInactiveIds.add(routeDisplay.getId());
            }
        });

        LOGGER.info("************");
        voyagerAPI.printProcessingErrorCounts();
        LOGGER.info("************");
        LOGGER.info(String.format("starting routeForms: %d, successfully processed: %d",routeForms.size(),successfullyProcessedForms.get()));
        LOGGER.info(String.format("skipped patch of active routes: %d, patched to active routes: %d, added new routes: %d",skippedActiveIds.size(),patchedToActiveIds.size(),addedIds.size()));
        LOGGER.info(String.format("Interrupted route forms on patch/create: %d",interruptedRouteFormsOnPatchCreate.size()));
        LOGGER.info("************");
        LOGGER.info(String.format("starting dbRoutes: %d, removed successful processed displays: %d, remaining dbRoutes to patch: %d",startingDbRoutes,successfullyProcessedDisplays.get(),dbRoutes.size()));
        LOGGER.info(String.format("skipped patch of inactive routes: %d, patched routes to inactive: %d",skippedInactiveIds.size(),patchedToInactiveIds.size()));
        LOGGER.info(String.format("Interrupted route displays on patch to inactive: %d",interruptedRouteDisplaysOnPatchInactive.size()));
    }
}
