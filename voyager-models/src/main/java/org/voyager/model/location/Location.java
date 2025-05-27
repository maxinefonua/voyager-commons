package org.voyager.model.location;

import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Location {
    private Integer id;
    private String name;
    private String subdivision;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private Double[] bbox;
    private Status status;
    private Source source;
    private String sourceId;
    private List<String> airports;

    public boolean hasAirport(String iata) {
        if (airports == null) return false;
        return airports.contains(iata.toUpperCase());
    }

    public void addAirport(String iata) {
        if (airports == null) airports = new ArrayList<>();
        else if (!airports.contains(iata.toUpperCase())) airports.add(iata.toUpperCase());
    }

    public void removeAirport(String iata) {
        if (airports != null) airports.remove(iata.toUpperCase());
    }
}