package org.voyager.commons.model.geoname.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.voyager.commons.validate.annotations.ValidLatitude;
import org.voyager.commons.validate.annotations.ValidLongitude;

@Builder @NoArgsConstructor
@Setter @Getter
@AllArgsConstructor
public class GeoNearbyQuery {
    @ValidLatitude
    Double latitude;
    @ValidLongitude
    Double longitude;
    @Min(1) @Max(100)
    Integer radiusKm;
}
