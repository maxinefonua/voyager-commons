package org.voyager.model.nominatim;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = false)
public class Geometry {
    String type;
    Double[] coordinates;
}
