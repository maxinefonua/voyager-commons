package org.voyager.commons.model.geoname.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
