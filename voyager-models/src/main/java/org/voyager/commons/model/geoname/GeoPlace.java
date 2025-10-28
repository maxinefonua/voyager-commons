package org.voyager.commons.model.geoname;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Map;

@Builder @NoArgsConstructor
@AllArgsConstructor
@Data @ToString
public class GeoPlace {
    private String adminCode1;
    private String lng;
    private String distance;
    private Long geonameId;
    private String toponymName;
    private String countryId;
    private String fcl;
    private Long population;
    private String countryCode;
    private String name;
    private String fclName;
    private Map<String,String> adminCodes1;
    private String countryName;
    @SuppressWarnings("SpellCheckingInspection")
    private String fcodeName;
    private String adminName1;
    private String lat;
    @SuppressWarnings("SpellCheckingInspection")
    private String fcode;
}
