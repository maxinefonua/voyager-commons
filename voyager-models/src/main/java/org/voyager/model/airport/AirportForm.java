package org.voyager.model.airport;

import jakarta.validation.constraints.*;
import lombok.*;
import org.voyager.model.validate.annotations.ValidEnum;
import org.voyager.model.validate.annotations.ValidLatitude;
import org.voyager.model.validate.annotations.ValidLongitude;
import org.voyager.model.validate.annotations.ValidZoneId;
import org.voyager.utils.Constants;

@Builder(toBuilder = true) @Getter
@ToString @NoArgsConstructor @AllArgsConstructor
public class AirportForm {
    @NotBlank
    @Pattern(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3_CASE_SENSITIVE,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE_CASE_SENSITIVE)
    private String iata;

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String subdivision;

    @NotBlank
    @Pattern(regexp = Constants.Voyager.Regex.COUNTRY_CODE_ALPHA2_CASE_SENSITIVE,
            message = Constants.Voyager.ConstraintMessage.COUNTRY_CODE_CASE_SENSITIVE)
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
