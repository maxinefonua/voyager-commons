package org.voyager.commons.model.geoname.query;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.voyager.commons.constants.Regex;

@Builder
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class GeoTimezoneQuery {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    // TODO: update to language datatype
    @Pattern(regexp = Regex.NONEMPTY_TRIMMED)
    private String language; // for country name
    private Integer radius; // radius (buffer in km for closest timezone in coastal areas)

    // TODO: update to date datatype
    private String date; // date (date for sunrise/sunset);
}
