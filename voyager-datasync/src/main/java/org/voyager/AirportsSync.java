package org.voyager;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportType;
import org.voyager.model.airport.AirportCH;
import org.voyager.service.ChAviationService;
import org.voyager.service.airport.AirportService;
import org.voyager.utils.ConstantsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

import static org.voyager.RoutesAndAirlineSync.extractMaxConcurrentRequests;
import static org.voyager.utils.ConstantsUtils.VOYAGER_API_KEY;

public class AirportsSync {
    private static Voyager voyager;
    private static AirportService airportService;
    private static ExecutorService executorService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportsSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from airports sync main");
        Integer maxConcurrentRequests = extractMaxConcurrentRequests(args[0]);
        executorService = Executors.newFixedThreadPool(maxConcurrentRequests);
        String host = "localhost";
        int port = 3000;
        String voyagerAPIkey = System.getenv(VOYAGER_API_KEY);

        VoyagerConfig voyagerConfig = new VoyagerConfig(VoyagerConfig.Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAPIkey);
        voyager = new Voyager(voyagerConfig);
        airportService = voyager.getAirportService();
        long start = System.currentTimeMillis();
        List<Airport> voyagerAirports = fetchVoyagerAirports();
        LOGGER.info(String.format("Milliseconds elapsed while fetching airports from voyager: %d",System.currentTimeMillis()-start));
        if (voyagerAirports.size() < 6000) throw new RuntimeException("airports from voyager has a size of %d. Needs investigation or reload of airport data");
        else {
            long prepatch = System.currentTimeMillis();
            int successes = patchAirportsFromVoyager(voyagerAirports);
            LOGGER.info(String.format("patch duration: %d seconds for %d successful airports",
                    (System.currentTimeMillis()-prepatch)/1000,successes));
        }
    }

    private static void createAirportsFromLocalFile(String fileName) {
        List<Airport> airports = loadAirports(fileName);
        airports.forEach(airport -> {
            LOGGER.info(airport.toString());
        });
    }

    private static List<Airport> loadAirports(String fileName) {
        InputStream is = ConstantsUtils.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new MissingResourceException(String.format("Required file missing from resources directory: %s",fileName),ConstantsUtils.class.getName(),fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            List<Airport> airports = new ArrayList<>();
            br.readLine(); // skip header line
            String line = br.readLine();
            while (line != null) {
                String[] tokens = line.split(",");

                String icao = tokens[0].replace("\"","");
                String iata = tokens[1].replace("\"","");
                String name = tokens[2].replace("\"","");
                String city = tokens[3].replace("\"","");
                String subd = tokens[4].replace("\"","");
                String countryCode = tokens[5].replace("\"","");
                String elevation = tokens[6].replace("\"","");
                Double latitude = Double.parseDouble(tokens[7].replace("\"",""));
                Double longitude = Double.parseDouble(tokens[8].replace("\"",""));
                String tz = tokens[9].replace("\"","");
                String lid = tokens[10].replace("\"","");

                Airport airport = Airport.builder().iata(iata).name(name).city(city)
                        .subdivision(subd).countryCode(countryCode)
                        .latitude(latitude).longitude(longitude).type(AirportType.OTHER).build();
                airports.add(airport);
                line = br.readLine();
            }
            return airports;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int patchAirportsFromVoyager(List<Airport> voyagerAirports) {
        List<String> iataList = voyagerAirports.stream().limit(100).map(Airport::getIata).toList();
        List<AirportCH> sourceAirports = getAirports(iataList);
        sourceAirports.forEach(airportCH -> LOGGER.info(airportCH.toString()));
        return sourceAirports.size();
    }

    private static List<AirportCH> getAirports(List<String> iataList) {
        List<AirportCH> airportCHList = new ArrayList<>();
        LOGGER.info("processing " + iataList.size() + " IATA codes");
        List<Future<Either<String,AirportCH>>> futureList = Collections.synchronizedList(new ArrayList<>());
        iataList.forEach(iata -> {
            Callable<Either<String,AirportCH>> taskWithResult = () -> {
                return ChAviationService.getAirportCH(iata).mapLeft(serviceError -> {
                    LOGGER.error(String.format("IATA lookup of '%s' failed with error: %s",
                            iata, serviceError.getException().getMessage())
                    );
                    return iata;
                });
            };
            Future<Either<String,AirportCH>> future = executorService.submit(taskWithResult);
            futureList.add(future);
        });
        List<String> failedIataCodes = Collections.synchronizedList(new ArrayList<>());
        futureList.forEach(future -> {
            try {
                Either<String,AirportCH> result = future.get();
                if (result.isRight()) airportCHList.add(result.get());
                else failedIataCodes.add(result.getLeft());
            } catch (InterruptedException | ExecutionException e) {
                failedIataCodes.add(future.toString());
            }
        });
        executorService.shutdown();
        failedIataCodes.forEach(LOGGER::info);
        return airportCHList;
    }

    private static  List<Future<Either<ServiceError,AirportCH>>> fetchSourceAirports(List<String> iataList) {
        List<Future<Either<ServiceError,AirportCH>>> airportCHList = Collections.synchronizedList(new ArrayList<>());
        List<String> failList = Collections.synchronizedList(new ArrayList<>());
        iataList.forEach(iata -> {
            Future<Either<ServiceError,AirportCH>> future = executorService.submit(
                    () -> ChAviationService.getAirportCH(iata)
            );
            airportCHList.add(future);
        });
        return airportCHList;
    }

    private static List<Airport> fetchVoyagerAirports() {
        Either<ServiceError, List<Airport>> result = airportService.getAirports();
        if (result.isLeft()) throw new RuntimeException(result.getLeft().getMessage(),result.getLeft().getException());
        return result.get();
    }
}
