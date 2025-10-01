package org.voyager.model.geoname;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(includeFieldNames = false)
public class BoundingBox {
    Double east;
    Double south;
    Double north;
    Double west;
    Integer accuracyLevel;
}