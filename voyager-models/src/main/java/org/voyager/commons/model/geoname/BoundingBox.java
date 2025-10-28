package org.voyager.commons.model.geoname;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = false)
public class BoundingBox {
    private Double east;
    private Double south;
    private Double north;
    private Double west;
    private Integer accuracyLevel;
}