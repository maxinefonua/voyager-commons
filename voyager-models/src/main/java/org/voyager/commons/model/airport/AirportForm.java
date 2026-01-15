package org.voyager.commons.model.airport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCode;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidCountryCode;
import org.voyager.commons.validate.annotations.ValidLatitude;
import org.voyager.commons.validate.annotations.ValidLongitude;
import org.voyager.commons.validate.annotations.ValidZoneId;

@Builder @Getter
@ToString @NoArgsConstructor @AllArgsConstructor
public class AirportForm {
    @ValidAirportCode
    private String iata;

    @Pattern(regexp = Regex.NONEMPTY_TRIMMED,
            message = Regex.ConstraintMessage.NONEMPTY_TRIMMED)
    @NotNull
    private String name;

    @Pattern(regexp = Regex.NONEMPTY_TRIMMED,
            message = Regex.ConstraintMessage.NONEMPTY_TRIMMED)
    @NotNull
    private String city;

    @Pattern(regexp = Regex.NONEMPTY_TRIMMED,
            message = Regex.ConstraintMessage.NONEMPTY_TRIMMED)
    @NotNull
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
