package org.voyager.sync.utils;

import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.Airport;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ConstantsDatasync {
    public static final String FAILED_AIRPORT_SCHEDULE_FILE = "flights/failed-airport-schedules.txt";
    public static final String FAILED_FLIGHT_NUMBERS_FILE = "flights/failed-airline-batches.txt";
    public static final String NON_CIVIL_AIRPORTS_FILE = "flights/non-civil-airports.txt";
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

    public static List<String> loadStringListFromListFile(String fileName) {
        InputStream is = ConstantsDatasync.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new MissingResourceException(
                String.format("Required file missing from resources directory: %s",fileName),
                ConstantsDatasync.class.getName(),fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> stringList = new ArrayList<>();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String code = line.trim();
                if (code.isEmpty()) continue;
                stringList.add(code);
            }
            return stringList;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading list of strings from file: %s\nError message: %s",
                    fileName,e.getMessage()),e);
        }
    }

    public static List<String> loadStringListFromDirectFile(String fileName) {
        try {
            InputStream is = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> stringList = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String code = line.trim();
                if (code.isEmpty()) continue;
                stringList.add(code);
            }
            return stringList;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read list of strings from file: %s, error: %s",
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

    public static void writeSetLineByLineDirectFile(Set<String> airports, String file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            StringJoiner joiner = new StringJoiner("\n");
            airports.forEach(code -> joiner.add(String.format("%s",code)));
            writer.write(joiner.toString());
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Failed to write to file: %s, error: %s",file,
                    e.getMessage()),ConstantsDatasync.class.getName(),file);
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

    public static void writeAirlineListToFile(Map<Airline, List<String>> airlineListMap, FileWriter fileWriter) {
        try (BufferedWriter writer = new BufferedWriter(fileWriter)) {
            StringJoiner lineJoiner = new StringJoiner("\n");
            airlineListMap.forEach((airline, codes) -> {
                StringJoiner codeJoiner = new StringJoiner(",");
                codes.forEach(codeJoiner::add);
                lineJoiner.add(String.format("%s:%s", airline.name(), codeJoiner));
            });
            writer.write(lineJoiner.toString());
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Error writing airline data to file: %s", fileWriter), e);
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
