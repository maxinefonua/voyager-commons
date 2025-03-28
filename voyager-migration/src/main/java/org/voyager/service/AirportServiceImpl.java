package org.voyager.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.voyager.entity.Airport;
import org.voyager.model.AirportDisplay;
import org.voyager.respository.AirportRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AirportServiceImpl implements AirportService<AirportDisplay> {
    @Autowired
    AirportRepository airportRepository;
    List<AirportDisplay> airportDisplayList;

    @PostConstruct
    public void loadAirportList() {
        airportDisplayList = airportRepository.findAll().stream().map(airport ->
            new AirportDisplay(airport.getName(),airport.getIata(),airport.getCity(),
                    airport.getSubdivision(),airport.getCountryCode(),airport.getLatitude(),
                    airport.getLongitude(),0.0)).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllIataCodes() {
        return airportRepository.findDistinctIatas();
    }

    @Override
    public List<String[]> getAlliataCodesAndNames() {
        return airportRepository.findAllIataCodesAndNames();
    }

    @Override
    public List<AirportDisplay> getByCountryCode(String countryCode, int limit) {
        return airportRepository.findByCountryCode(countryCode, Limit.of(limit)).stream().map(airport ->
                new AirportDisplay(airport.getName(),airport.getIata(),airport.getCountryCode(),
                        airport.getLatitude(),airport.getLongitude())).collect(Collectors.toList());
    }

    // TODO: some kind of calculation
    // ORDER BY LAT, THEN ORDER BY LON TOP 5?
    // WHAT IF LON, LAT is BETTER RESULTS?
    @Override
    public List<AirportDisplay> getClosest(double latitude, double longitude, int limit) {
        airportDisplayList.forEach(airportDisplay -> {
            airportDisplay.setDistance(AirportDisplay.calculateDistance(
                    latitude, longitude, airportDisplay.getLatitude(),airportDisplay.getLongitude()
            ));});
        return airportDisplayList.stream().sorted(Comparator.comparingDouble(AirportDisplay::getDistance))
                .limit(limit).collect(Collectors.toList());
    }

    // TODO: implement either
    @Override
    public AirportDisplay getByIATA(String iata) {
        List<Airport> results = airportRepository.findByIata(iata);
        if (results.size() != 1) return null;
        return results.stream().map(airport -> new AirportDisplay(airport.getName(),
                airport.getIata(),airport.getCountryCode(),airport.getLatitude(),airport.getLongitude()))
                .collect(Collectors.toList()).get(0);
    }
}
