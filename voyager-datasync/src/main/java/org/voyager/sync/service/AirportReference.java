package org.voyager.sync.service;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airport.Airport;
import org.voyager.sdk.service.AirportService;
import org.voyager.sdk.service.GeoService;
import org.voyager.sync.model.flightradar.AirportFR;

public interface AirportReference {
    void refreshReference(AirportService airportService);
    void addCivilAirport(Airport airport);
    Either<ServiceError,Airport> addMissingAirport(String airportCode, AirportFR airportFR, GeoService geoService);
    void addNonCivilAirport(String airportCode);
    boolean isSavedAirport(String airportCode);
    Option<Airport> getCivilAirportOption(String airportCode);
}
