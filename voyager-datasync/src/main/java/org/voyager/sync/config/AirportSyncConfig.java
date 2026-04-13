package org.voyager.sync.config;

import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.airport.AirportType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AirportSyncConfig extends DatasyncConfig{
    public static class Flag extends DatasyncConfig.Flag {
        public static final String AIRPORT_TYPES = "-tl";
        public static final String IATA_LIST = "-il";
    }

    public static class Defaults extends DatasyncConfig.Defaults {
        public static final int THREAD_COUNT = 100;
        public static final List<AirportType> AIRPORT_TYPES = List.of(AirportType.UNVERIFIED,AirportType.OTHER);
    }

    public AirportSyncConfig(String[] args) {
        super(args);
        processThreadCount();
        validateGeoNamesUser();
        processAirportTypeList();
    }

    private void processThreadCount() {
        int fromProgramArgs = (int) this.optionMap.get(Flag.THREAD_COUNT);
        if (fromProgramArgs == DatasyncConfig.Defaults.THREAD_COUNT) {
            this.optionMap.put(Flag.THREAD_COUNT, Defaults.THREAD_COUNT);
        }
    }

    private void processIataList() {
        if (!this.additionalOptions.containsKey(Flag.IATA_LIST)) {
            throw new RuntimeException(Messages.getMissingMessage("iata list", Flag.IATA_LIST));
        }
        String iataListString = (String) this.additionalOptions.get(Flag.IATA_LIST);
        String[] codes = iataListString.split(",");
        if (codes.length == 0) {
            throw new RuntimeException(Messages.getEmptyListConstraintElements("iata list",
                    Flag.IATA_LIST, Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_NONEMPTY));
        }
        List<String> iataList = new ArrayList<>();
        for (String code : codes) {
            if (!code.matches(Regex.AIRPORT_CODE)) {
                throw new RuntimeException(Messages.getInvalidListConstraintViolation("iata list",
                    Flag.IATA_LIST, Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_NONEMPTY,code));
            }
            iataList.add(code.toUpperCase());
        }
        this.additionalOptions.put(Flag.IATA_LIST,iataList);
    }

    private void processAirportTypeList() {
        if (!this.additionalOptions.containsKey(Flag.AIRPORT_TYPES)) {
            this.additionalOptions.put(Flag.AIRPORT_TYPES,Defaults.AIRPORT_TYPES);
        } else {
            String airportTypeFlagValue = (String) this.additionalOptions.get(Flag.AIRPORT_TYPES);
            List<String> airportTypeStringList = Arrays.stream(airportTypeFlagValue.split(",")).toList();
            if (airportTypeStringList.isEmpty()) {
                throw new RuntimeException(Messages.getInvalidListMessage(
                        "airport types", Flag.AIRPORT_TYPES,
                        Arrays.stream(AirportType.values()).map(AirportType::name).toList()));
            }
            try {
                List<AirportType> airportTypeList = airportTypeStringList.stream()
                        .map(str -> AirportType.valueOf(str.toUpperCase())).toList();
                this.additionalOptions.put(Flag.AIRPORT_TYPES, airportTypeList);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(Messages.getInvalidListMessage(
                        "airport types", Flag.AIRPORT_TYPES,
                        Arrays.stream(AirportType.values()).map(AirportType::name).toList()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<AirportType> getAirportTypeList() {
        return (List<AirportType>) this.additionalOptions.get(Flag.AIRPORT_TYPES);
    }

    @SuppressWarnings("unchecked")
    public List<String> getIataList() {
        return (List<String>) this.additionalOptions.get(Flag.IATA_LIST);
    }
}
