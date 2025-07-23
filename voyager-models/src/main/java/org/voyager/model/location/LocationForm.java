package org.voyager.model.location;

import jakarta.validation.constraints.*;
import lombok.*;
import org.voyager.model.validate.ValidEnum;

import java.util.ArrayList;
import java.util.List;

import static org.voyager.utils.ConstantsUtils.COUNTRY_CODE_REGEX;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class LocationForm {

    @NotNull
    @ValidEnum(enumClass = Source.class)
    @Builder.Default
    String source = Source.MANUAL.name();

    @NotBlank
    String sourceId;

    @NotBlank
    String name;

    @NotBlank
    String subdivision;

    @NotBlank
    @Pattern(regexp = COUNTRY_CODE_REGEX, message = "must be a valid two-letter ISO 3166-1 alpha-2 country code")
    String countryCode;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double longitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double west;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double south;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    Double east;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    Double north;

    @NotNull
    @Builder.Default
    List<String> airports = new ArrayList<>();

    public void setSource(String source) {
        this.source = source;
        if (source == null) this.source = Source.MANUAL.name();
    }
}
