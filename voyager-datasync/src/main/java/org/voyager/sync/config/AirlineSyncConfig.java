package org.voyager.sync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.model.airline.Airline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AirlineSyncConfig extends DatasyncConfig{
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineSyncConfig.class);

    public static class Flag extends DatasyncConfig.Flag {
        public static String AIRLINE_LIST = "-al";
    }

    public AirlineSyncConfig(String[] args) {
        super(args);
        processAirlineList();
    }

    private void processAirlineList() {
        if (!this.additionalOptions.containsKey(Flag.AIRLINE_LIST)) {
            LOGGER.info("no airlines given for processing, defaulting to all airlines");
            this.additionalOptions.put(Flag.AIRLINE_LIST, Arrays.stream(Airline.values()).toList());
        } else {
            String listString = (String) this.additionalOptions.get(Flag.AIRLINE_LIST);
            String[] tokens = listString.split(",");
            if (tokens.length == 0) {
                throw new RuntimeException(DatasyncConfig.Messages.getEmptyListConstraintElements("airline list",
                        Flag.AIRLINE_LIST, "is a valid airline enum"));
            }
            List<Airline> airlineList = new ArrayList<>();
            for (String token : tokens) {
                try {
                    airlineList.add(Airline.valueOf(token.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(DatasyncConfig.Messages.getInvalidListMessage("airline list",
                            Flag.AIRLINE_LIST, Arrays.stream(Airline.values()).map(Airline::name).toList()));
                }
            }
            this.additionalOptions.put(Flag.AIRLINE_LIST, airlineList);
        }
    }


    @SuppressWarnings("unchecked")
    public List<Airline> getAirlineList() {
        return (List<Airline>) this.additionalOptions.get(Flag.AIRLINE_LIST);
    }
}
