package org.voyager.sync.service;

import io.vavr.control.Option;
import org.voyager.commons.model.airport.Airport;
import org.voyager.sdk.service.AirportService;
import org.voyager.sync.model.flightradar.AirportFR;

public interface AirportReference {
    void refreshReference(AirportService airportService);
    void addCivilAirport(Airport airport);
    void addMissingAirport(String airportCode, AirportFR airportFR);
    void addNonCivilAirport(String airportCode);
    boolean isSavedAirport(String airportCode);
    Option<Airport> getCivilAirportOption(String airportCode);
}
