package org.voyager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.voyager.service.VerifyType;
import org.voyager.service.impl.VerifyTypeLocalImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class AirportsSync {
    private static final String IATA_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/iata.txt";
    private static final String CIVIL_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/civil.txt";
    private static final String MILITARY_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/military.txt";
    private static final String HISTORICAL_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/historical.txt";
    private static final String ISSUES_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/issues.txt";
    private static final String SPECIAL_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/special.txt";

    private static final String CIVIL_AIRPORT = "Civil Airport";
    private static final String MILITARY_AIRPORT = "Military Airport";
    private static final String HISTORICAL_AIRPORT = "Airport no longer in use";

    public static void main(String[] args) {
        VerifyType verifyType = new VerifyTypeLocalImpl();
        verifyType.run(20);
//        System.out.println("printing from airports sync main");
//        Set<String> iataCodes = loadIataCodesFromFile(IATA_FILE_WITH_PATH);
//        System.out.println("iataCodes: " + iataCodes.size());
//
//        Set<String> civil = loadAirportsFromFile(CIVIL_FILE_WITH_PATH);
//        Set<String> military = loadAirportsFromFile(MILITARY_FILE_WITH_PATH);
//        Set<String> historical = loadAirportsFromFile(HISTORICAL_FILE_WITH_PATH);
//        Set<String> issues = loadAirportsFromFile(ISSUES_FILE_WITH_PATH);
//        Map<String,Set<String>> special = loadAirportsFromSpecialFile(SPECIAL_FILE_WITH_PATH);
//        int civilStartSize = civil.size();
//        int militaryStartSize = military.size();
//        int historicalStartSize = historical.size();
//        int issuesStartSize = issues.size();
//        int specialStartSize = special.size();
//        System.out.println("civil airports loaded: " + civilStartSize);
//        System.out.println("military airports loaded: " + militaryStartSize);
//        System.out.println("historical airports loaded: " + historicalStartSize);
//        System.out.println("issues airports loaded: " + issuesStartSize);
//        System.out.println("special airport keys loaded: " + specialStartSize);
//
//        civil.forEach(iataCodes::remove);
//        military.forEach(iataCodes::remove);
//        historical.forEach(iataCodes::remove);
//        issues.forEach(iataCodes::remove);
//        special.forEach((key, value) -> value.forEach(iataCodes::remove));
//        System.out.println("iataCodes left to process: " + iataCodes.size());
//
//        List<String> iataListToProcess =  iataCodes.stream().limit(1000).toList();
//        filterByType(iataListToProcess,civil,military,historical,issues,special);
//        System.out.println("civil airports added post processing: " + (civil.size()-civilStartSize));
//        System.out.println("military airports added post processing: " + (military.size()-militaryStartSize));
//        System.out.println("historical airports added post processing: " + (historical.size()-historicalStartSize));
//        System.out.println("issues airports added post processing: " + (issues.size()-issuesStartSize));
//        System.out.println("special added post processing: " + (special.size()-specialStartSize));
//
//        iataListToProcess.forEach(code -> {
//            if (military.contains(code) || civil.contains(code) || historical.contains(code)
//                    || issues.contains(code)) iataCodes.remove(code);
//            else { special.forEach((key, value) -> value.forEach(iata -> {
//                    if (iata.equals(code)) iataCodes.remove(code);
//                }));
//            }
//        });
//        System.out.println("iataCodes after processing: " + iataCodes.size());
//        writeSetToFile(military,MILITARY_FILE_WITH_PATH);
//        writeSetToFile(civil,CIVIL_FILE_WITH_PATH);
//        writeSetToFile(historical,HISTORICAL_FILE_WITH_PATH);
//        writeSetToFile(issues,ISSUES_FILE_WITH_PATH);
//        writeMapToFile(special, SPECIAL_FILE_WITH_PATH);
    }

    public static void writeSetToFile(Set<String> airports, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String data : airports) {
                writer.write(data);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    private static void filterByType(List<String> iataCodes, Set<String> civil, Set<String> military, Set<String> historical, Set<String> issues, Map<String, Set<String>> specialMap) {
        iataCodes.forEach(iata -> {
            try {
                String airportType = getAirportTypeFromAviation(iata);
                switch (airportType) {
                    case MILITARY_AIRPORT -> military.add(iata);
                    case CIVIL_AIRPORT -> civil.add(iata);
                    case HISTORICAL_AIRPORT -> historical.add(iata);
                    default -> {
                        System.out.println("adding special airport type [" + airportType + "] for iata " + iata);
                        Set<String> matches = specialMap.getOrDefault(airportType,new HashSet<>());
                        matches.add(iata);
                        specialMap.put(airportType,matches);
                    }
                }
            } catch (Exception e) {
                issues.add(iata);
            }
        });
    }

    private static String getAirportTypeFromAviation(String iata) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.ch-aviation.com/airports/");
        sb.append(iata);
        try {
            Document doc = Jsoup.connect(sb.toString())
                    .cookie("CHASESSID", "a509a5e7c8ed9b49ea205d5da9dfe54a")
                    .cookie("GUEST_SESSION_ID", "22e92641364d3d42cedbabb9f2db8367")
                    .get();
            String dataUrl  = doc.getElementsByAttributeValue("href","#overview").first().attr("data-url");
            return getAirportTypeFromOverview(dataUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getAirportTypeFromOverview(String dataUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.ch-aviation.com");
        sb.append(dataUrl);
        try {
            Document doc = Jsoup.connect(sb.toString())
                    .cookie("CHASESSID", "a509a5e7c8ed9b49ea205d5da9dfe54a")
                    .cookie("GUEST_SESSION_ID", "22e92641364d3d42cedbabb9f2db8367")
                    .get();
            return doc.select(".data-value").first().text();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeMapToFile(Map<String, Set<String>> special, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String,Set<String>> entry : special.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey());
                sb.append("=");
                StringJoiner joiner = new StringJoiner(",");
                entry.getValue().forEach(joiner::add);
                sb.append(joiner);
                writer.write(sb.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    private static Map<String, Set<String>> loadAirportsFromSpecialFile(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            Map<String,Set<String>> specialMap = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] keyVal = line.split("=");
                if (keyVal.length != 2) continue;
                String[] codes = keyVal[1].split(",");
                specialMap.put(keyVal[0],new HashSet<>(Arrays.asList(codes)));
            }
            return specialMap;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read special file",e);
        }
    }

    private static Set<String> loadAirportsFromFile(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            Set<String> codes = new HashSet<>();
            String line;
            while ((line = br.readLine()) != null) {
                codes.add(line);
            }
            return codes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read airports file",e);
        }
    }

    private static Set<String> loadIataCodesFromFile(String file) {
        try {
            Scanner scanner = new Scanner(new File(file));
            scanner.useDelimiter(",");
            Set<String> codes = new HashSet<>();
            while (scanner.hasNext()) {
                String token = scanner.next();
                codes.add(token.chars().filter(Character::isLetter)
                        .mapToObj(c -> String.valueOf((char) c))
                        .collect(Collectors.joining()));
            }
            return codes;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to load IATA file",e);
        }
    }
}
