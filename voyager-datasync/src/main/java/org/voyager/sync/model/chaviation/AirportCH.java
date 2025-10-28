package org.voyager.sync.model.chaviation;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.voyager.commons.model.airport.AirportType;

@Builder @Data @ToString
public class AirportCH {
    String iata;
    String name;
    String countryCode;
    Double longitude;
    Double latitude;
    AirportType type;
}
