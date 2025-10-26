package org.voyager.commons.model.geoname.query;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Builder @NoArgsConstructor
@Getter @Setter @AllArgsConstructor
public class GeoNearbyQuery {
    @NotNull
    Double latitude;
    @NotNull
    Double longitude;
    Integer radius;
}
