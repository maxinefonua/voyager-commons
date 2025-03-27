package org.voyager.model;

import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AirportDisplay {
    @NonNull
    String name;
    @NonNull
    String iata;
    String city;
    String subdivision;
    @NonNull
    String countryCode;
    @NonNull
    Double latitude;
    @NonNull
    Double longitude;
}
