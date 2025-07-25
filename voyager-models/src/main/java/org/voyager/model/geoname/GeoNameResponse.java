package org.voyager.model.geoname;

import lombok.*;

import java.util.List;
@Builder
@Data @ToString @NoArgsConstructor @AllArgsConstructor
public class GeoNameResponse {
    List<GeoName> geonames;
}
