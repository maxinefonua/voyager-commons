package org.voyager.commons.model.geoname;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Builder
@Data @ToString @NoArgsConstructor @AllArgsConstructor
public class GeoNameResponse {
    private List<GeoPlace> geonames;
}
