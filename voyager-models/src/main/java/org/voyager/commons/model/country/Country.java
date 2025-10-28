package org.voyager.commons.model.country;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder @Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class Country {
    private String code;
    private String name;
    private Long population;
    private String capitalCity;
    private Double areaInSqKm;
    private Continent continent;
    private Double[] bounds;
}
