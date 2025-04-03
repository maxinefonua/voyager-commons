package org.voyager.service.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.service.VerifyType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import static org.voyager.utils.ConstantsLocal.IATA_FILE;
import static org.voyager.utils.ConstantsLocal.CIVIL_FILE;
import static org.voyager.utils.ConstantsLocal.MILITARY_FILE;
import static org.voyager.utils.ConstantsLocal.HISTORICAL_FILE;
import static org.voyager.utils.ConstantsLocal.ISSUES_FILE;
import static org.voyager.utils.ConstantsLocal.SPECIAL_FILE;
import static org.voyager.utils.ConstantsLocal.CIVIL_AIRPORT;
import static org.voyager.utils.ConstantsLocal.MILITARY_AIRPORT;
import static org.voyager.utils.ConstantsLocal.HISTORICAL_AIRPORT;

public class VerifyTypeLocalImpl implements VerifyType {
    private static Set<String> all, civil, military, historical,issue;
    private static Map<String,Set<String>> specialMap;
    private static int limit,allToProcessSize,civilStartSize,militaryStartSize,historicalStartSize,issueStartSize,specialTypeStartSize;
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyTypeLocalImpl.class);

    public VerifyTypeLocalImpl(int processLimit) {
        limit = processLimit;
        loadCodesToProcess();
        LOGGER.debug(String.format("loaded all codes: %d, civil codes: %d, military codes: %d, historical codes: %d, issue codes: %d, and special types: %d",
                all.size(),civilStartSize,militaryStartSize,historicalStartSize,issueStartSize,specialTypeStartSize));
        filterProcessed();
        allToProcessSize = all.size();
        LOGGER.info(String.format("after filtering, remaining codes to process: %d, process limit: %d",allToProcessSize,limit));
    }

    @Override
    public void run() {
        processRemaining();
        LOGGER.debug(String.format("post processing, all codes reamining: %d, additional civil codes: %d, additional military codes: %d, additional historical codes: %d, additional issue codes: %d, and additional special types: %d",
                all.size()-allToProcessSize,civil.size()-civilStartSize,military.size()-militaryStartSize,
                historical.size()-historicalStartSize,issue.size()-issueStartSize,specialMap.size()-specialTypeStartSize));
        saveProcessed();
        LOGGER.info("successfully saved to local files");
    }

    @Override
    public void loadCodesToProcess() {
        all = loadAllCodesFromJson(IATA_FILE);

        civil = loadCodesFromFile(CIVIL_FILE);
        civilStartSize = civil.size();

        military = loadCodesFromFile(MILITARY_FILE);
        militaryStartSize = military.size();

        historical = loadCodesFromFile(HISTORICAL_FILE);
        historicalStartSize = historical.size();

        issue = loadCodesFromFile(ISSUES_FILE);
        issueStartSize = issue.size();

        specialMap = loadSpecialMapFromFile();
        specialTypeStartSize = specialMap.size();
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
        if (limit >= all.size()) processCollection(all);
        else processCollection(all.stream().limit(limit).toList());
    }

    private void processCollection(Collection<String> collection) {
        collection.forEach(iata -> {
            try {
                String airportType = getAirportTypeFromAviation(iata);
                switch (airportType) {
                    case MILITARY_AIRPORT -> military.add(iata);
                    case CIVIL_AIRPORT -> civil.add(iata);
                    case HISTORICAL_AIRPORT -> historical.add(iata);
                    default -> {
                        // TODO: use logger
                        System.out.println(String.format("adding iata code [%s] to special airport type: %s", iata, airportType));
                        Set<String> matches = specialMap.getOrDefault(airportType, new HashSet<>());
                        matches.add(iata);
                        specialMap.put(airportType, matches);
                    }
                }
            } catch (Exception e) {
                System.out.println(String.format("%s\nAdding %s to issue codes", e.getMessage(), iata));
                issue.add(iata);
            }
        });
    }

    @Override
    public void saveProcessed() {
        writeSetToFile(civil, CIVIL_FILE);
        writeSetToFile(military, MILITARY_FILE);
        writeSetToFile(historical, HISTORICAL_FILE);
        writeSetToFile(issue, ISSUES_FILE);
        writeMapToFile(specialMap, SPECIAL_FILE);
    }

    private Map<String, Set<String>> loadSpecialMapFromFile() {
        InputStream is = VerifyTypeLocalImpl.class.getClassLoader().getResourceAsStream(SPECIAL_FILE);
        if (is == null) throw new MissingResourceException(String.format("Required file missing from resources directory: %s",SPECIAL_FILE),VerifyTypeLocalImpl.class.getName(),SPECIAL_FILE);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        Map<String,Set<String>> specialMap = new HashMap<>();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] keyVal = line.split("=");
                if (keyVal.length != 2) throw new IllegalArgumentException(String.format(
                        "Incorrectly formatted special map file: %s\nMust list key=val on each line where key is a unique airport type, and val is a comma-separated list of valid 3-letter IATA codes.", SPECIAL_FILE));
                String[] codes = keyVal[1].split(",");
                for (String code : codes) if (code.length() != 3) throw new IllegalArgumentException(String.format(
                        "Incorrectly formatted special map file: %s\nMust list key=val on each line where key is a unique airport type, and val is a comma-separated list of valid 3-letter IATA codes.", SPECIAL_FILE));
                specialMap.put(keyVal[0],new HashSet<>(Arrays.asList(codes)));
            }
            return specialMap;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading special map from file %s\nError message: %s", SPECIAL_FILE,e.getMessage()),e);
        }
    }

    private static Set<String> loadCodesFromFile(String fileName) {
        InputStream is = VerifyTypeLocalImpl.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new MissingResourceException(String.format("Required file missing from resources directory: %s",fileName),VerifyTypeLocalImpl.class.getName(),fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        Set<String> codes = new HashSet<>();
        try {
            String line = br.readLine();
            if (line == null) throw new IllegalArgumentException(String.format(
                    "Incorrectly formatted input file: %s\nMust be a line of valid, comma-separated, 3-letter IATA codes.",fileName));
            String[] tokens = line.split(",");
            for (String token : tokens) {
                if (token.length() != 3) {
                    throw new IllegalArgumentException(String.format(
                            "Incorrectly formatted input file: %s\nMust be a line of valid, comma-separated, 3-letter IATA codes.", fileName));
                }
                codes.add(token);
            }
            return codes;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading IATA codes from file: %s\nError message: %s",
                    fileName,e.getMessage()),e);
        }
    }

    private static Set<String> loadAllCodesFromJson(String fileName) {
        InputStream is = VerifyTypeLocalImpl.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) throw new MissingResourceException(String.format("Required file missing from resources directory: %s",fileName),VerifyTypeLocalImpl.class.getName(),fileName);
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

    private static void writeSetToFile(Set<String> airports, String file) {
        String filePath = VerifyTypeLocalImpl.class.getClassLoader().getResource(file).getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringJoiner joiner = new StringJoiner(",");
            airports.forEach(code -> joiner.add(String.format("'%s'",code)));
            writer.write(joiner.toString());
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()),VerifyTypeLocalImpl.class.getName(),filePath);
        }
    }

    private static void writeMapToFile(Map<String, Set<String>> special, String file) {
        String filePath = VerifyTypeLocalImpl.class.getClassLoader().getResource(file).getFile();
        boolean first = true;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String,Set<String>> entry : special.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey());
                sb.append("=");
                StringJoiner joiner = new StringJoiner(",");
                entry.getValue().forEach(code -> joiner.add(String.format("'%s'",code)));
                sb.append(joiner);
                if (!first) writer.newLine();
                else first = false;
                writer.write(sb.toString());
            }
        } catch (IOException e) {
            throw new MissingResourceException(String.format("Error writing to file: %s\nError message: %s",filePath,e.getMessage()),VerifyTypeLocalImpl.class.getName(),filePath);
        }
    }

    public static String getAirportTypeFromAviation(String iata) {
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
