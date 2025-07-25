package org.voyager.model.country;

import jakarta.validation.constraints.*;
import lombok.*;
import org.voyager.model.validate.ValidEnum;

import java.util.ArrayList;
import java.util.List;

import static org.voyager.utils.ConstantsUtils.COUNTRY_CODE_REGEX;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class CountryForm {
    @NotBlank
    @Pattern(regexp = COUNTRY_CODE_REGEX)
    String countryCode;

    @NotBlank
    String countryName;

    @NotNull
    @Min(0)
    Long population;

    @NotNull
    @Min(0)
    Double areaInSqKm;

    @ValidEnum(enumClass = Continent.class)
    String continent;

    @NotNull
    String capitalCity;

    @NotNull
    String currencyCode;

    @NotNull
    @Builder.Default
    List<String> languages = new ArrayList<>();

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double west;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double south;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double east;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double north;
}
