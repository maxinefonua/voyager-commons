package org.voyager.model.country;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.voyager.model.validate.ValidEnum;
import org.voyager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class CountryForm {
    @NotBlank
    @Pattern(regexp = Constants.Voyager.Regex.ALPHA2_CODE_REGEX)
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

    @NotNull
    private String capitalCity;

    @NotNull
    private String currencyCode;

    @NotNull
    @Builder.Default
    private List<String> languages = new ArrayList<>();

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double west;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double south;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double east;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double north;
}
