package org.voyager.model.location;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class LocationForm {
    @NotBlank
    String source = "MANUAL";

    @NotBlank
    String sourceId;

    @NotBlank
    String name;

    @NotBlank
    String subdivision;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{2}$", message = "must be a valid two-letter ISO 3166-1 alpha-2 country code")
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

    public void setSource(String source) {
        if (source == null) {
            this.source = "MANUAL";
        } else {
            this.source = source;
        }

    }
}
