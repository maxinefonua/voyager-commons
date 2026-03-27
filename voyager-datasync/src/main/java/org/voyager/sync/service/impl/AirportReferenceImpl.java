package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportQuery;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.response.PagedResponse;
import org.voyager.sdk.service.AirportService;
import org.voyager.sync.model.flightradar.AirportFR;
import org.voyager.sync.service.AirportReference;

import java.util.*;

public class AirportReferenceImpl implements AirportReference {
    private static final Set<String> airportCodes = new HashSet<>();
    private static final Map<String,Airport> civilAirports = new HashMap<>();
    private static final Map<String,AirportFR> missingAirports = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportReferenceImpl.class);

    public AirportReferenceImpl(AirportService airportService){
        loadAirports(airportService);
    }


    private void loadAirports(AirportService airportService) {
        // load all airport code set
        int initialAirportCodeSize = airportCodes.size();
        airportService.getIATACodes().fold(
                serviceError -> {
                    throw new RuntimeException(serviceError.getException().getMessage(),
                            serviceError.getException());
                },
                airportCodes::addAll
        );
        if (initialAirportCodeSize == 0) {
            LOGGER.info("voyager reference loaded {} codes in all airports set", airportCodes.size());
        } else {
            LOGGER.info("voyager reference refreshed with {} codes in all airports set from {} codes initially",
                    airportCodes.size(),initialAirportCodeSize);
        }

        // load civil codes set
        int initialCivilSize = civilAirports.size();
        AirportQuery airportQuery = AirportQuery.builder().airportTypeList(List.of(AirportType.CIVIL))
                .size(1000).page(0).build();
        Either<ServiceError, PagedResponse<Airport>> civilAirportsEither = airportService.getAirports(airportQuery);
        while (civilAirportsEither.isRight()) {
            PagedResponse<Airport> pagedResponse = civilAirportsEither.get();
            pagedResponse.getContent().forEach(airport ->
                    civilAirports.put(airport.getIata(),airport));
            if (pagedResponse.isLast()) {
                if (initialCivilSize == 0) {
                    LOGGER.info("voyager reference loaded {} airports into civil map",
                            civilAirports.size());
                } else {
                    LOGGER.info("voyager reference refreshed with {} airports into civil map from {} airports initially",
                            civilAirports.size(),initialCivilSize);
                }
                return;
            }
            airportQuery.setPage(airportQuery.getPage()+1);
            civilAirportsEither = airportService.getAirports(airportQuery);
        }
        Exception exception = civilAirportsEither.getLeft().getException();
        throw new RuntimeException(exception.getMessage(),exception);
    }

    @Override
    public void refreshReference(AirportService airportService) {
        loadAirports(airportService);
    }

    @Override
    public void addCivilAirport(Airport airport) {
        civilAirports.put(airport.getIata(),airport);
    }

    @Override
    public void addMissingAirport(String airportCode, AirportFR airportFR) {
        missingAirports.put(airportCode,airportFR);
    }

    @Override
    public void addNonCivilAirport(String airportCode) {
        airportCodes.add(airportCode);
    }

    @Override
    public boolean isSavedAirport(String airportCode) {
        return airportCodes.contains(airportCode);
    }

    @Override
    public Option<Airport> getCivilAirportOption(String airportCode) {
        return Option.of(civilAirports.get(airportCode));
    }
}