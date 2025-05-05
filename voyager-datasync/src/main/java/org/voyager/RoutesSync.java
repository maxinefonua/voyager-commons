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
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
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

public class RoutesSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesSync.class);
    private static final Map<Airline,String> airlineToURLMap = Map.of(
            Airline.DELTA, "https://www.flightradar24.com/data/airlines/dl-dal/routes"
    );
    private static VoyagerAPI voyagerAPI;

    public static void main(String[] args) {
        System.out.println("printing from routes sync main");
        Airline airline = null;
        try {
            airline = Airline.valueOf(args[0].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            String errorMessage = "Missing or invalid first argument for airline. Currently available airlines are: 'delta'";
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        Integer maxConcurrentRequests = null;
        try {
            maxConcurrentRequests = Integer.valueOf(args[1].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            String errorMessage = "Missing or invalid second argument for maxConcurrentRequests. Provide a valid integer value";
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        voyagerAPI = new VoyagerAPIService(maxConcurrentRequests);
        String routesURL = airlineToURLMap.get(airline);
        Document document = null;
        try {
            document = getDocumentFromURL(routesURL,Map.of());
            LOGGER.info("Successfully fetched document from url.");
            saveToResources(document,ROUTES_HTML_FILE);
        } catch (RuntimeException e) {
            LOGGER.info("Error w fetching document from URL. Loading last saved document from resource file.");
            document = fetchDocumentFromResourceFile(ROUTES_HTML_FILE);
        }
        Object routesObject = getRoutesObjectFromDocument(document);
        String routesJson = convertRoutesObjectToRoutesJson(routesObject);
        List<RouteForm> routeForms = buildRouteFormsFromRoutesJson(routesJson,airline);
        processRouteForms(routeForms);
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

    private static Document fetchDocumentFromResourceFile(String fileName) {
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
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processRouteForms(List<RouteForm> routeForms) {
        List<Integer> addedIds = new ArrayList<>();
        List<Integer> existingIds = new ArrayList<>();

        AtomicReference<Integer> processedCounter = new AtomicReference<>(0);
        AtomicReference<Integer> non200Counter = new AtomicReference<>(0);
        AtomicReference<Integer> jsonExceptionCounter = new AtomicReference<>(0);
        AtomicReference<Integer> interruptedCounter = new AtomicReference<>(0);
        AtomicReference<Integer> skipCounter = new AtomicReference<>(0);
        AtomicReference<Integer> createCounter = new AtomicReference<>(0);
        AtomicReference<Integer> existingCounter = new AtomicReference<>(0);
        AtomicReference<Integer> multiCounter = new AtomicReference<>(0);
        // TODO: limit threads
        routeForms.forEach(routeForm -> {
                try {
                    HttpResponse<String> response = voyagerAPI.getRoute(routeForm.getOrigin(), routeForm.getDestination(), Airline.valueOf(routeForm.getAirline()));
                    if (response.statusCode() == 200) {
                        String json = response.body();
                        ObjectMapper om = new ObjectMapper();
                        try {
                            RouteDisplay[] results = om.readValue(json, RouteDisplay[].class);
                            if (results.length == 0) {
                                LOGGER.debug(String.format("create new route: %s", routeForm));
                                String jsonForm = om.writeValueAsString(routeForm);
                                HttpResponse<String> addResponse = voyagerAPI.addRoute(jsonForm);
                                if (addResponse.statusCode() == 200) {
                                    String jsonAdded = addResponse.body();
                                    RouteDisplay routeDisplay = om.readValue(jsonAdded, RouteDisplay.class);
                                    addedIds.add(routeDisplay.getId());
                                    createCounter.getAndSet(createCounter.get() + 1);
                                } else {
                                    LOGGER.error("Non-200 returned from URI: " + response.uri().toString());
                                    non200Counter.getAndSet(non200Counter.get() + 1);
                                }
                            } else if (results.length == 1) {
                                LOGGER.debug(String.format("existing route: %s", routeForm));
                                RouteDisplay routeDisplay = results[0];
                                existingIds.add(routeDisplay.getId());
                                existingCounter.getAndSet(existingCounter.get() + 1);
                            } else {
                                LOGGER.info(String.format("more than one route returned for routeForm: %s", routeForm));
                                multiCounter.getAndSet(multiCounter.get() + 1);
                            }
                            processedCounter.getAndSet(processedCounter.get() + 1);
                        } catch (JsonProcessingException e) {
                            LOGGER.error(String.format("Error reading json: %s. Message: %s", json, e.getMessage()), e);
                            jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
                        }
                    } else {
                        LOGGER.error("Non-200 returned from URI: " + response.uri().toString());
                        non200Counter.getAndSet(non200Counter.get() + 1);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error(String.format("InterrupedException thrown. Error message: %s",e.getMessage()),e);
                    interruptedCounter.getAndSet(interruptedCounter.get() + 1);
                }
        });
        LOGGER.info("processedCounter: " + processedCounter.get() + ", skipCounter: " + skipCounter.get());
        LOGGER.info("non200Counter: " + non200Counter.get() + ", jsonExceptionCounter: " + jsonExceptionCounter.get() + ", interruptedCounter: " + interruptedCounter.get());
        LOGGER.info("createCounter: " + createCounter.get()+ ", existingCounter: " + existingCounter.get() + ", multiCounter: " + multiCounter.get());
        LOGGER.info("addedIds: " + addedIds.size() + ", existingIds: " + existingIds.size());
    }
}
