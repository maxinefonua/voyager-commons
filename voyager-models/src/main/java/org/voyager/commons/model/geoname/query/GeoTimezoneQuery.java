package org.voyager.commons.model.geoname.query;

import jakarta.validation.constraints.NotNull;
import lombok.*;

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
    private String language; // for country name
    private Integer radius; // radius (buffer in km for closest timezone in coastal areas)

    // TODO: update to date datatype
    private String date; // date (date for sunrise/sunset);
}
