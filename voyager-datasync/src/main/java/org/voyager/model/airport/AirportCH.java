package org.voyager.model.airport;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder @Data @ToString
public class AirportCH {
    String iata;
    String name;
    String countryCode;
    Double longitude;
    Double latitude;
    AirportType type;
}
