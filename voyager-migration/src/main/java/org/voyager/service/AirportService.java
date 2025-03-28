package org.voyager.service;

import java.util.List;


public interface AirportService<T> {
    public List<String> getAllIataCodes();
    public List<String[]> getAlliataCodesAndNames();
    public List<T> getByCountryCode(String countryCode, int limit);
    public List<T> getClosest(double latitude, double longitude, int limit);
    public T getByIATA(String iata);
}