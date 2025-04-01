package org.voyager.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.voyager.entity.Airport;
import org.voyager.model.AirportDisplay;
import org.voyager.respository.AirportRepository;

import java.util.List;
import java.util.Set;
import java.util.MissingResourceException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AirportsServiceImpl implements AirportsService<AirportDisplay> {
    @Autowired
    AirportRepository airportRepository;
    List<AirportDisplay> airportDisplayList;
    Set<String> iataCodes;

    @PostConstruct
    public void loadAirportLists() {
        List<Airport> airportsList = airportRepository.findAll();
        // TODO: Replace MissingResourceException with correct exception
        if (airportsList.isEmpty()) throw new MissingResourceException("Airports repository is empty. Check configuration for correct database instance.",
                this.getClass().getName(),"airports");
        airportDisplayList = airportsList.stream().map(airport -> AirportDisplay.builder()
                        .name(airport.getName()).iata(airport.getIata()).city(airport.getCity())
                        .subdivision(airport.getSubdivision()).countryCode(airport.getCountryCode())
                        .latitude(airport.getLatitude()).longitude(airport.getLongitude()).build())
                .sorted(Comparator.comparing(AirportDisplay::getIata)).collect(Collectors.toList());
        iataCodes = airportRepository.selectDistinctIataSet();
    }

    @Override
    public Set<String> getIataCodes() {
        return Set.copyOf(iataCodes);
    }

    @Override
    public List<AirportDisplay> getAirports() {
        return List.copyOf(airportDisplayList);
    }

    @Override
    public List<AirportDisplay> getByCountryCode(String countryCode, int limit) {
        return airportDisplayList.stream().filter(airport ->
                        airport.getCountryCode().equals(countryCode)).limit(limit).toList();
    }

    @Override
    public List<AirportDisplay> getByCountryCode(String countryCode) {
        return airportDisplayList.stream().filter(airport ->
                airport.getCountryCode().equals(countryCode)).collect(Collectors.toList());
    }

    @Override
    public List<AirportDisplay> getSortedByDistance(double latitude, double longitude, int limit) {
        return airportDisplayList.stream().map(airportDisplay -> airportDisplay.toBuilder().distance(
                AirportDisplay.calculateDistance(latitude,longitude, airportDisplay.getLatitude(),airportDisplay.getLongitude())).build())
                .sorted(Comparator.comparingDouble(AirportDisplay::getDistance)).limit(limit).collect(Collectors.toList());
    }

    @Override
    public Optional<AirportDisplay> getByIata(String iata) {
        List<AirportDisplay> results = airportDisplayList.stream().filter(airportDisplay ->
                airportDisplay.getIata().equals(iata)).toList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0).toBuilder().build());
    }
}
