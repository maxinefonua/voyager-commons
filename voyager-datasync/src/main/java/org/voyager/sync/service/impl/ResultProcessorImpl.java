package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.sdk.service.AirlineService;
import org.voyager.sdk.service.FlightService;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import org.voyager.sync.service.AirlineAirportProcessor;
import org.voyager.sync.service.ResultProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultProcessorImpl implements ResultProcessor {
    private final FlightSyncConfig.SyncMode syncMode;
    private final List<Airline> airlineList;
    private final FlightService flightService;
    private final AirlineAirportProcessor airlineAirportProcessor;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultProcessorImpl.class);

    ResultProcessorImpl(FlightSyncConfig flightSyncConfig, FlightService flightService, AirlineService airlineService) {
        this.syncMode = flightSyncConfig.getSyncMode();
        this.airlineList = flightSyncConfig.getAirlineList();
        this.flightService = flightService;
        this.airlineAirportProcessor = new AirlineAirportProcessorImpl(airlineService,flightSyncConfig);
    }

    @Override
    public void process(List<AirportScheduleFailure> failureList) {
        // TODO: write to a file?
//        Set<String> failedRoutes = new HashSet<>();
        failureList.forEach(airportScheduleFailure -> {
            LOGGER.error("{}:{} failed with error: {}",airportScheduleFailure.airportCode1,
                    airportScheduleFailure.airportCode2,
                    airportScheduleFailure.serviceError.getException().getMessage());
//            failedRoutes.add(String.format("%s:%s",
//                    airportScheduleFailure.airportCode1,airportScheduleFailure.airportCode2));
        });
        }
}
