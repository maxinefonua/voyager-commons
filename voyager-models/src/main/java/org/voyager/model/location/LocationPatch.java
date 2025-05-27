package org.voyager.model.location;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder @ToString(includeFieldNames = false)
@AllArgsConstructor
public class LocationPatch {
    @NotNull  // can be empty
    List<String> airports = new ArrayList<>();
    Status status;

    public void addAirport(String iata) {
        if (airports == null) airports = new ArrayList<>();
        else if (!airports.contains(iata.toUpperCase())) airports.add(iata.toUpperCase());
    }

    public void removeAirport(String iata) {
        if (airports != null) airports.remove(iata.toUpperCase());
    }
}
