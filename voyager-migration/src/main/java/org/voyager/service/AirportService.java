package org.voyager.service;

import java.util.List;

public interface AirportService<T> {
    public List<T> getAll();
    public List<T> getByCountryCode(String countryCode);
    public List<T> getClosest(float latitude, float longitude, int limit);
    public List<T> getByIATA(String iata);
}