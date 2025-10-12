package org.voyager.config;

import org.junit.platform.commons.util.StringUtils;
import org.voyager.model.Airline;
import java.util.Arrays;

public class AirlineSyncConfig extends DatasyncConfig {

    public AirlineSyncConfig(String[] args) {
        super(args);
        processAirline();
    }

    public static class Flag {
        public static final String AIRLINE = "-a";
    }

    public static class Defaults {
        public static int THREAD_COUNT = 10;
    }

    private void processAirline() {
        if (!this.addtionalOptions.containsKey(Flag.AIRLINE)) {
            throw new RuntimeException(Messages.getMissingMessage("airline",Flag.AIRLINE));
        }
        String airlineString = (String) this.addtionalOptions.get(Flag.AIRLINE);
        if (StringUtils.isBlank(airlineString)) {
            throw new RuntimeException(Messages.getInvalidValueMessage(
                    "airline", Flag.AIRLINE,airlineString,
                    Arrays.stream(Airline.values()).map(Airline::name).toList()));
        }
        try {
            Airline airline = Airline.valueOf(airlineString.toUpperCase());
            this.addtionalOptions.put(Flag.AIRLINE,airline);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(Messages.getInvalidValueMessage(
                    "airline", Flag.AIRLINE,airlineString,
                    Arrays.stream(Airline.values()).map(Airline::name).toList()));
        }
    }

    @Override
    public int getThreadCount() {
        int fromArgs = (int) this.optionMap.get(DatasyncConfig.Flag.THREAD_COUNT);
        if (fromArgs == DatasyncConfig.Defaults.THREAD_COUNT) return Defaults.THREAD_COUNT;
        return fromArgs;
    }

    public Airline getAirline() {
        return (Airline) this.addtionalOptions.get(Flag.AIRLINE);
    }
}
