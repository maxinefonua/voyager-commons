package org.voyager.commons.model.geoname;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.commons.model.country.Continent;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class GeoFull {
    private Long geonameId;
    private String name;
    private String asciiName;
    private String toponymName;
    private String countryName;
    private String countryCode;
    private String countryId;

    @JsonProperty("tag")
    private String sourceTag;

    @JsonProperty("continentCode")
    private Continent continent;

    @JsonProperty("cc2")
    private String additionalCountryCode;

    @SuppressWarnings("SpellCheckingInspection")
    @JsonProperty("astergdem")
    private Integer elevationAGDEM;
    private Integer elevation;

    private Long population;

    private String wikipediaURL;

    @JsonProperty("fcl")
    private String featureClass;

    @JsonProperty("fclName")
    private String featureClassName;

    @SuppressWarnings("SpellCheckingInspection")
    @JsonProperty("fcode")
    private String featureCode;

    @SuppressWarnings("SpellCheckingInspection")
    @JsonProperty("fcodeName")
    private String featureCodeName;

    @SuppressWarnings("SpellCheckingInspection")
    @JsonProperty("srtm3")
    private Integer elevationSRTM;

    @JsonProperty("lat")
    private String latitudeInt;

    @JsonProperty("lng")
    private String longitudeInt;

    @JsonProperty("timezone")
    private TimezoneGN timezoneGN;

    @SuppressWarnings("SpellCheckingInspection")
    @JsonProperty("bbox")
    private BoundingBox boundingBox;

    private String adminCode1;
    private String adminId1;
    private String adminName1;
    private String adminName2;
    private String adminName3;
    private String adminName4;
    private String adminName5;
    private List<NameMap> alternateNames;
}
