package org.voyager.model;

import lombok.Data;
import lombok.NonNull;
import java.util.TimeZone;

@Data
public class AirportDisplay {
    @NonNull
    String name;
    @NonNull
    String iata;
    String city;
    String subdivision;
    @NonNull
    String countryCode;
    Double latitude;
    Double longitude;
    @NonNull
    TimeZone timeZone;
}
