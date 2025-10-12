package org.voyager.utils;

import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.Set;
import java.util.MissingResourceException;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ConstantsDatasync {
    public static final String IATA_FILE = "airports/iata.txt";
    public static final String CIVIL_FILE = "airports/civil.txt";
    public static final String MILITARY_FILE = "airports/military.txt";
    public static final String HISTORICAL_FILE = "airports/historical.txt";
    public static final String ISSUES_FILE = "airports/issues.txt";
    public static final String SPECIAL_FILE = "airports/special.txt";
    public static final String DELTA_ALL_FILE = "airports/delta.txt";
    public static final String DELTA_ALL_DB = "airports/delta-allDB.txt";
    public static final String DELTA_CURRENT_FILE = "airports/delta-current.txt";
    public static final String DELTA_HUB_FILE = "airports/delta-hub.txt";
    public static final String DELTA_SEASONAL_FILE = "airports/delta-seasonal.txt";
    public static final String DELTA_FORMER_FILE = "airports/delta-former.txt";
    public static final String AIRLINE_PROCESSED_FILE = "airline/airline-processed.txt";
    public static final String NON_AIRLINE_PROCESSED_FILE = "airline/non-airline-processed.txt";
    public static final String ROUTE_AIRPORTS_FILE = "airline/route-airports.txt";
    public static final String FAILED_FLIGHT_NUMS_FILE = "airline/failed-flights.txt";
    public static final String FLIGHT_AIRPORTS_FILE = "airline/flight-airports.sql";
    public static final String MISSING_AIRPORTS_FILE = "airline/missing-airports.sql";
    public static final String DELTA_FORMER_DB = "airports/delta-formerDB.txt";
    public static final String DELTA_FUTURE_FILE = "airports/delta-future.txt";
    public static final String DELTA_FOCUS_FILE = "airports/delta-focus.txt";
    public static final String CURRENCY_RETRIES_FILE = "currency/retries.txt";
    public static final String LANGUAGE_ISO_FILE = "countries/isolanguages.txt";

    public static final String CIVIL_AIRPORT = "Civil Airport";
    public static final String MILITARY_AIRPORT = "Military Airport";
    public static final String HISTORICAL_AIRPORT = "Airport no longer in use";

    public static Set<String> loadCodesFromFile(String fileName) {
        InputStream is = ConstantsDatasync.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new MissingResourceException(
                String.format("Required file missing from resources directory: %s",fileName),
                ConstantsDatasync.class.getName(),fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        Set<String> codes = new HashSet<>();
        try {
            String line = br.readLine();
            if (line == null) throw new IllegalArgumentException(
                    String.format("Incorrectly formatted input file: %s\n" +
                            "Must be a line of valid, comma-separated, 3-letter IATA codes.",fileName));
            String[] tokens = line.split(",");
            for (String token : tokens) {
                if (token.length() != 3) {
                    throw new IllegalArgumentException(String.format("Incorrectly formatted input file: %s\n" +
                                    "Must be a line of valid, comma-separated, 3-letter IATA codes.", fileName));
                }
                codes.add(token);
            }
            return codes;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading IATA codes from file: %s\nError message: %s",
                    fileName,e.getMessage()),e);
        }
    }

    public static Set<String> loadCodesFromListFile(String fileName) {
        InputStream is = ConstantsDatasync.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new MissingResourceException(
                String.format("Required file missing from resources directory: %s",fileName),
                ConstantsDatasync.class.getName(),fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        Set<String> codes = new HashSet<>();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String code = line.trim();
                if (code.isEmpty()) continue;
                if (code.length() != 3) {
                    throw new IllegalArgumentException(String.format(
                            "Incorrectly formatted input file: %s\nMust be a file of valid 3-letter IATA code on each line.", fileName));
                }
                codes.add(code);
            }
            return codes;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading IATA codes from file: %s\nError message: %s",
                    fileName,e.getMessage()),e);
        }
    }

    public static Set<String> loadAllCodesFromJson(String fileName) {
        InputStream is = ConstantsDatasync.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new MissingResourceException(
                String.format("Required file missing from resources directory: %s",fileName),
                ConstantsDatasync.class.getName(),fileName);
        Scanner scanner = new Scanner(new InputStreamReader(is));
        scanner.useDelimiter(",");
        Set<String> codes = new HashSet<>();
        while (scanner.hasNext()) {
            String token = scanner.next();
            codes.add(token.chars().filter(Character::isLetter)
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining()));
        }
        return codes;
    }

    public static void writeSetLineByLine(Set<String> airports, String file) {
        String filePath = ConstantsDatasync.class.getClassLoader().getResource(file).getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringJoiner joiner = new StringJoiner("\n");
            airports.forEach(code -> joiner.add(String.format("%s",code)));
            writer.write(joiner.toString());
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()), ConstantsDatasync.class.getName(),filePath);
        }
    }

    public static void writeSetToFile(Set<String> airports, String file) {
        String filePath = ConstantsDatasync.class.getClassLoader().getResource(file).getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringJoiner joiner = new StringJoiner(",");
            airports.forEach(code -> joiner.add(String.format("'%s'",code)));
            writer.write(joiner.toString());
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()), ConstantsDatasync.class.getName(),filePath);
        }
    }

    public static void writeSetToFileForDBInsertion(Set<String> airports, String file) {
        String filePath = ConstantsDatasync.class.getClassLoader().getResource(file).getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringJoiner joiner = new StringJoiner(",");
            airports.forEach(code -> joiner.add(String.format("('%s')",code)));
            writer.write(joiner.toString());
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()), ConstantsDatasync.class.getName(),filePath);
        }
    }

    public static void writeSetToFileForDBInsertionAirports(Set<Airport> missingAirports, String file) {
        String filePath = ConstantsDatasync.class.getClassLoader().getResource(file).getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringJoiner joiner = new StringJoiner(",");
            missingAirports.forEach(airport -> joiner.add(String.format("('%s','%s',%f,%f,'%s','%s','%s','%s','%s')",
                    airport.getIata(),airport.getCountryCode(), airport.getLongitude(),airport.getLatitude(),
                    airport.getZoneId(),airport.getName().replace("'","''"),
                    airport.getCity().replace("'","''"),
                    airport.getSubdivision().replace("'","''"),airport.getType())));
            writer.write(String.format("INSERT INTO airports(iata,country,lon,lat,tz,name,city,subd,type) VALUES %s",joiner));
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()), ConstantsDatasync.class.getName(),filePath);
        }
    }

    public static void writeSetToFileForDBInsertionWithAirline(Set<String> airports, Airline airline, boolean isActive, String file) {
        String filePath = ConstantsDatasync.class.getClassLoader().getResource(file).getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringJoiner joiner = new StringJoiner(",");
            airports.forEach(code -> joiner.add(String.format("('%s','%s',%s)",code,airline.name(),isActive)));
            writer.write(String.format("DELETE FROM airline_airports WHERE airline = '%s';\n",airline.name()));
            writer.write(String.format("INSERT INTO airline_airports(iata,airline,active) VALUES %s;\n",joiner));
            writer.write("SELECT orgn,dstn FROM flights INNER JOIN routes ON flights.route_id = routes.id WHERE (flights.departure_zdt IS NULL OR flights.arrival_zdt IS NULL) AND flights.active = true;\n");
            writer.write("-- UPDATE flights SET active = false WHERE (flights.departure_zdt IS NULL OR flights.arrival_zdt IS NULL) AND flights.active = true;");
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()), ConstantsDatasync.class.getName(),filePath);
        }
    }
}
