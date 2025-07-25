package org.voyager.model.country;

import lombok.*;

import java.util.List;

@Builder @Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class Country {
    String code;
    String name;
    Long population;
    String capitalCity;
    List<String> languages;
    Double areaInSqKm;
    Continent continent;
    String currencyCode;
    Double[] bounds;
}
