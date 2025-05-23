package org.voyager.model.airport;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder @Data @ToString(includeFieldNames = false)
public class AirportCH {
    String iata;
    String countryCode;
    Double longitude;
    Double latitude;
    AirportType type;
}
