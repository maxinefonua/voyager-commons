package org.voyager.service;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface AirportsService<T> {
    public Set<String> getIataCodes();
    public List<T> getAirports();
    public List<T> getSortedByDistance(double latitude, double longitude, int limit);
    public List<T> getByCountryCode(String countryCode, int limit);
    public List<T> getByCountryCode(String countryCode);
    public Optional<T> getByIata(String iata);
}