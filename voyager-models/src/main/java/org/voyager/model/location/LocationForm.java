package org.voyager.model.location;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.model.validate.ValidEnum;
import org.voyager.utils.Constants;
import java.util.ArrayList;
import java.util.List;

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
    @Pattern(regexp = Constants.Voyager.Regex.COUNTRY_CODE_ALPHA2, message = "must be a valid two-letter ISO 3166-1 alpha-2 country code")
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
}
