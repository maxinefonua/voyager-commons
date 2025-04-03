package org.voyager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@Builder @Getter @NoArgsConstructor
@AllArgsConstructor
public class AirportMin {
    @NonNull
    String iata;
    @NonNull
    String name;
    String city;
    String subdivision;
    @NonNull
    String countryCode;
    @NonNull
    Double latitude;
    @NonNull
    Double longitude;
    @JsonIgnore
    Double distance;
}
