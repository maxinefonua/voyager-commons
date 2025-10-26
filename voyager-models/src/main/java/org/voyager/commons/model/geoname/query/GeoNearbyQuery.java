package org.voyager.commons.model.geoname.query;

import jakarta.validation.constraints.NotNull;
import lombok.*;
@Builder @NoArgsConstructor
@Getter @Setter @AllArgsConstructor
public class GeoNearbyQuery {
    @NotNull
    Double latitude;
    @NotNull
    Double longitude;
    Integer radius;
}
