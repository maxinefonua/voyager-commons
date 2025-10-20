package org.voyager.config;

import org.voyager.model.airport.AirportType;
import org.voyager.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AirportSyncConfig extends DatasyncConfig{
    public static class Flag {
        public static final String AIRPORT_TYPES = "-tl";
        public static final String SYNC_MODE = "-sy";
        public static final String IATA_LIST = "-il";
    }

    public enum SyncMode {
        ADD_MISSING, // build out and insert missing airports from given iata list
        VERIFY_TYPE, // verify airport types using external service
        ENRICH_FIELDS // enrich missing airport fields using external service
    }

    public static class Defaults {
        public static final int THREAD_COUNT = 100;
        public static final SyncMode SYNC_MODE = SyncMode.VERIFY_TYPE;
    }

    public AirportSyncConfig(String[] args) {
        super(args);
        processSyncMode();
        if (this.additionalOptions.get(Flag.SYNC_MODE).equals(SyncMode.VERIFY_TYPE)) {
            processAirportTypes();
        } else {
            validateGeoNamesUser();
            processIataList();
        }
    }

    private void processIataList() {
        if (!this.additionalOptions.containsKey(Flag.IATA_LIST)) {
            throw new RuntimeException(Messages.getMissingMessage("iata list", Flag.IATA_LIST));
        }
        String iataListString = (String) this.additionalOptions.get(Flag.IATA_LIST);
        String[] codes = iataListString.split(",");
        if (codes.length == 0) {
            throw new RuntimeException(Messages.getEmptyListConstraintElems("iata list",
                    Flag.IATA_LIST,Constants.Voyager.ConstraintMessage.IATA_CODE));
        }
        List<String> iataList = new ArrayList<>();
        for (String code : codes) {
            if (!code.matches(Constants.Voyager.Regex.IATA_CODE_ALPHA3)) {
                throw new RuntimeException(Messages.getInvalidListConstraintViolation("iata list",
                    Flag.IATA_LIST, Constants.Voyager.ConstraintMessage.IATA_CODE,code));
            }
            iataList.add(code.toUpperCase());
        }
        this.additionalOptions.put(Flag.IATA_LIST,iataList);
    }

    private void processSyncMode() {
        if (!this.additionalOptions.containsKey(Flag.SYNC_MODE)) {
            this.additionalOptions.put(Flag.SYNC_MODE,Defaults.SYNC_MODE);
        } else {
            String syncModeValue = (String) this.additionalOptions.get(Flag.SYNC_MODE);
            try {
                this.additionalOptions.put(Flag.SYNC_MODE,SyncMode.valueOf(syncModeValue));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(Messages.getInvalidListMessage(
                        "sync mode", Flag.SYNC_MODE,
                        Arrays.stream(SyncMode.values()).map(SyncMode::name).toList()));
            }
        }
    }

    private void processAirportTypes() {
        if (!this.additionalOptions.containsKey(Flag.AIRPORT_TYPES)) {
            throw new RuntimeException(Messages.getMissingMessage("airport types", Flag.AIRPORT_TYPES));
        }
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
            this.additionalOptions.put(Flag.AIRPORT_TYPES,airportTypeList);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(Messages.getInvalidListMessage(
                    "airport types", Flag.AIRPORT_TYPES,
                    Arrays.stream(AirportType.values()).map(AirportType::name).toList()));
        }
    }

    @Override
    public int getThreadCount() {
        int fromArgs = (int) this.optionMap.get(DatasyncConfig.Flag.THREAD_COUNT);
        if (fromArgs == DatasyncConfig.Defaults.THREAD_COUNT) return Defaults.THREAD_COUNT;
        return fromArgs;
    }

    public List<AirportType> getAirportTypeList() {
        return (List<AirportType>) this.additionalOptions.get(Flag.AIRPORT_TYPES);
    }

    public SyncMode getSyncMode() {
        return (SyncMode) this.additionalOptions.get(Flag.SYNC_MODE);
    }

    public List<String> getIataList() {
        return (List<String>) this.additionalOptions.get(Flag.IATA_LIST);
    }
}
