package org.voyager.model.geoname;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.voyager.model.country.Continent;

import java.util.List;
import java.util.Map;

@Builder @NoArgsConstructor
@AllArgsConstructor
@Data @ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoNameFull {
    Long geonameId;
    String name;
    String asciiName;
    String toponymName;
    String countryName;
    String countryCode;
    String countryId;

    @JsonProperty("tag")
    String sourceTag;

    @JsonProperty("continentCode")
    Continent continent;

    @JsonProperty("cc2")
    String additionalCountryCode;

    @JsonProperty("astergdem")
    Integer elevationAGDEM;
    Integer elevation;

    Long population;

    String wikipediaURL;

    @JsonProperty("fcl")
    String featureClass;

    @JsonProperty("fclName")
    String featureClassName;

    @JsonProperty("fcode")
    String featureCode;

    @JsonProperty("fcodeName")
    String featureCodeName;

    @JsonProperty("srtm3")
    Integer elevationSRTM;

    @JsonProperty("lat")
    String latitudeInt;

    @JsonProperty("lng")
    String longitudeInt;

    @JsonProperty("timezone")
    TimezoneGN timezoneGN;

    @JsonProperty("bbox")
    BoundingBox boundingBox;

    String adminCode1;
    String adminId1;
    String adminName1;
    String adminName2;
    String adminName3;
    String adminName4;
    String adminName5;
    List<NameMap> alternateNames;

    public void setContinent(String countryCode) {
        this.continent = Continent.valueOf(countryCode);
    }
}
