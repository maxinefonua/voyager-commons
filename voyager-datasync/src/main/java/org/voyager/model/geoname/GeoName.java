package org.voyager.model.geoname;

import lombok.*;

import java.util.Map;

@Builder @NoArgsConstructor
@AllArgsConstructor
@Data @ToString
public class GeoName {
    String adminCode1;
    String lng;
    String distance;
    Long geonameId;
    String toponymName;
    String countryId;
    String fcl;
    Long population;
    String countryCode;
    String name;
    String fclName;
    Map<String,String> adminCodes1;
    String countryName;
    String fcodeName;
    String adminName1;
    String lat;
    String fcode;
}
