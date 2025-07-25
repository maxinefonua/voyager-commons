package org.voyager.model.nominatim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter
@ToString(includeFieldNames = false)
public class FeatureSearch {
    String name;

    @JsonProperty("addresstype")
    String addressType;

    String type;
    String category;

    @JsonProperty("display_name")
    String displayName;

    @JsonProperty("place_id")
    Long placeId;

    @JsonProperty("osm_id")
    Long openStreetMapId;

    @JsonProperty("osm_type")
    String openStreetMapType;

    @JsonProperty("lat")
    Double latitude;

    @JsonProperty("lon")
    Double longitude;

    Double importance;

    @JsonProperty("place_rank")
    Integer placeRank;

    String licence;
    Double[] boundingbox;
}