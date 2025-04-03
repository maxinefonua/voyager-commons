package org.voyager.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.voyager.service.VerifyType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class VerifyTypeLocalImpl implements VerifyType {
    private static final String IATA_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/iata.txt";
    private static final String CIVIL_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/civil.txt";
    private static final String MILITARY_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/military.txt";
    private static final String HISTORICAL_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/historical.txt";
    private static final String ISSUES_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/issues.txt";
    private static final String SPECIAL_FILE_WITH_PATH = "/Users/maxinefonua/repos/dos/VoyagerCommons/voyager-datasync/src/main/resources/special.txt";

    private static final String CIVIL_AIRPORT = "Civil Airport";
    private static final String MILITARY_AIRPORT = "Military Airport";
    private static final String HISTORICAL_AIRPORT = "Airport no longer in use";

    private static Set<String> all, civil, military, historical,issue;
    private static Map<String,Set<String>> specialMap;

    @Override
    public void run(int processLimit) {
        loadCodesToProcess();
        int civilStartSize = civil.size(), militaryStartSize = military.size(),
                historicalStartSize = historical.size(), issueStartSize = issue.size(),
                specialTypeStartSize = specialMap.size();
        System.out.println(String.format("loaded all codes: %d, civil codes: %d, military codes: %d, historical codes: %d, issue codes: %d, and special types: %d",
                all.size(),civilStartSize,militaryStartSize,historicalStartSize,issueStartSize,specialTypeStartSize));
        filterProcessed();
        int allToProcessSize = all.size();
        System.out.println(String.format("after filtering, remaining codes to process: %d",allToProcessSize));
        processRemaining();
        System.out.println(String.format("post processing, all codes reamining: %d, additional civil codes: %d, additional military codes: %d, additional historical codes: %d, additional issue codes: %d, and additional special types: %d",
                all.size()-allToProcessSize,civil.size()-civilStartSize,military.size()-militaryStartSize,
                historical.size()-historicalStartSize,issue.size()-issueStartSize,specialMap.size()-specialTypeStartSize));
        saveProcessed();
        System.out.println("successfully saved to local files");
    }

    @Override
    public void loadCodesToProcess() {
        all = loadAllCodesFromJson(IATA_FILE_WITH_PATH);
        civil = loadCodesFromFile(CIVIL_FILE_WITH_PATH);
        military = loadCodesFromFile(MILITARY_FILE_WITH_PATH);
        historical = loadCodesFromFile(HISTORICAL_FILE_WITH_PATH);
        issue = loadCodesFromFile(ISSUES_FILE_WITH_PATH);
        specialMap = loadSpecialMapFromFile();
    }

    @Override
    public void filterProcessed() {
        civil.forEach(all::remove);
        military.forEach(all::remove);
        historical.forEach(all::remove);
        issue.forEach(all::remove);
        specialMap.forEach((key, value) -> value.forEach(all::remove));
    }

    @Override
    public void processRemaining() {
        all.forEach(iata -> {
            String airportType = getAirportTypeFromAviation(iata);
            switch (airportType) {
                case MILITARY_AIRPORT -> military.add(iata);
                case CIVIL_AIRPORT -> civil.add(iata);
                case HISTORICAL_AIRPORT -> historical.add(iata);
                default -> {
                    // TODO: use logger
                    System.out.println(String.format("adding iata code [%s] to special airport type: %s",iata,airportType));
                    Set<String> matches = specialMap.getOrDefault(airportType,new HashSet<>());
                    matches.add(iata);
                    specialMap.put(airportType,matches);
                }
            }
        });
    }

    @Override
    public void saveProcessed() {
        writeSetToFile(civil,CIVIL_FILE_WITH_PATH);
        writeSetToFile(military,MILITARY_FILE_WITH_PATH);
        writeSetToFile(historical,HISTORICAL_FILE_WITH_PATH);
        writeSetToFile(issue,ISSUES_FILE_WITH_PATH);
        writeMapToFile(specialMap,SPECIAL_FILE_WITH_PATH);
    }

    private Map<String, Set<String>> loadSpecialMapFromFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(SPECIAL_FILE_WITH_PATH));
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

    private static Set<String> loadCodesFromFile(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            Set<String> codes = new HashSet<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (StringUtils.isEmpty(line) || line.length() != 3) throw new IllegalArgumentException(String.format(
                        "Incorrectly formatted input file: %s\nMust list valid 3-letter IATA codes on each line.",file));
                codes.add(line);
            }
            return codes;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error loading IATA codes from file: %s\nError message: %s",
                    file,e.getMessage()),e);
        }
    }

    private static Set<String> loadAllCodesFromJson(String file) {
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
            throw new RuntimeException(String.format("Failed to load IATA file: %s\nError message: %s",file,e.getMessage()),e);
        }
    }

    // TODO: Logger
    private static void writeSetToFile(Set<String> airports, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String data : airports) {
                writer.write(data);
                writer.newLine();
            }
        } catch (IOException e) {
            // TODO: logger
            System.err.println(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()));
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
            // TODO: Logger
            System.err.println(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()));
        }
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
            throw new RuntimeException(String.format("Error fetching aviation page for IATA code [%s]\nError message: %s",iata,e.getMessage()),e);
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
            throw new RuntimeException(String.format("Error fetching overview data at endpoint: %s\nError message: %s",dataUrl,e.getMessage()),e);
        }
    }

}
