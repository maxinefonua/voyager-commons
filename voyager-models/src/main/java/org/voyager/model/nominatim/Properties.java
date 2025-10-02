package org.voyager.model.nominatim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = false)
public class Properties {
    @JsonProperty("place_id")
    Long placeId;
    @JsonProperty("osm_type")
    String osmType;
    @JsonProperty("osm_id")
    Long osmId;
    @JsonProperty("place_rank")
    Integer placeRank;
    String category;
    String type;
    @JsonProperty("addresstype")
    String addressType;
    String name;
    @JsonProperty("display_name")
    String displayName;
    Address address;
}