package org.voyager.commons.model.country;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.commons.validate.annotations.ValidCountryCode;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidLatitude;
import org.voyager.commons.validate.annotations.ValidLongitude;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class CountryForm {
    @ValidCountryCode
    private String countryCode;

    @NotBlank
    private String countryName;

    @NotNull
    @Min(0)
    private Long population;

    @NotNull
    @Min(0)
    private Double areaInSqKm;

    @ValidEnum(enumClass = Continent.class)
    private String continent;

    @NotBlank
    private String capitalCity;

    @NotBlank
    private String currencyCode;

    @ValidLongitude
    private Double west;

    @ValidLatitude
    private Double south;

    @ValidLatitude
    private Double east;

    @ValidLongitude
    private Double north;
}
