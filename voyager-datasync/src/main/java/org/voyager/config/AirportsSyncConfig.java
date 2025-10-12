package org.voyager.config;

import org.voyager.model.airport.AirportType;
import java.util.Arrays;
import java.util.List;

public class AirportsSyncConfig extends DatasyncConfig{
    public static class Flag {
        public static final String AIRPORT_TYPES = "-tp";
    }

    public static class Defaults {
        public static final int THREAD_COUNT = 1000;
    }

    public AirportsSyncConfig(String[] args) {
        super(args);
        processAirportTypes();
    }

    private void processAirportTypes() {
        if (!this.addtionalOptions.containsKey(Flag.AIRPORT_TYPES)) {
            throw new RuntimeException(Messages.getMissingMessage("airport types", Flag.AIRPORT_TYPES));
        }
        String airportTypeFlagValue = (String) this.addtionalOptions.get(Flag.AIRPORT_TYPES);
        List<String> airportTypeStringList = Arrays.stream(airportTypeFlagValue.split(",")).toList();
        if (airportTypeStringList.isEmpty()) {
            throw new RuntimeException(Messages.getInvalidListMessage(
                    "airport types", Flag.AIRPORT_TYPES,
                    Arrays.stream(AirportType.values()).map(AirportType::name).toList()));
        }
        try {
            List<AirportType> airportTypeList = airportTypeStringList.stream()
                    .map(str -> AirportType.valueOf(str.toUpperCase())).toList();
            this.addtionalOptions.put(Flag.AIRPORT_TYPES,airportTypeList);
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
        return (List<AirportType>) this.addtionalOptions.get(Flag.AIRPORT_TYPES);
    }
}
