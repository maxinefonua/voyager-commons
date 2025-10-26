package org.voyager.commons.model.geoname.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder @AllArgsConstructor
@Setter @Getter @NoArgsConstructor
@ToString(includeFieldNames = false)
public class GeoResponse<T> {
    Integer totalResultsCount;
    @JsonProperty("geonames")
    List<T> results;
    @JsonProperty("status")
    GeoStatus geoStatus;
}
