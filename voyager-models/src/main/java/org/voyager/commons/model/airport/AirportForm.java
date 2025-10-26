package org.voyager.commons.model.airport;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.voyager.commons.validate.annotations.*;

@Builder @Getter
@ToString @NoArgsConstructor @AllArgsConstructor
public class AirportForm {
    @ValidAirportCode
    private String iata;

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String subdivision;

    @ValidCountryCode
    private String countryCode;

    @ValidLatitude
    private String latitude;

    @ValidLongitude
    private String longitude;

    @ValidEnum(enumClass = AirportType.class)
    private String airportType;

    @ValidZoneId
    private String zoneId;
}
