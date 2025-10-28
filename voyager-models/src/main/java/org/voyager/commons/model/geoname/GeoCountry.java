package org.voyager.commons.model.geoname;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class GeoCountry {
    private String continent;
    private String capital;
    private String languages;
    private Long geonameId;
    private Double south;
    private String isoAlpha3;
    private Double north;
    private String fipsCode;
    private String population;
    private Double east;
    private String isoNumeric;
    private String areaInSqKm;
    private String countryCode;
    private Double west;
    private String countryName;
    private String postalCodeFormat;
    private String continentName;
    private String currencyCode;
}
