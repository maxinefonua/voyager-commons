package org.voyager.commons.model.nominatim;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = false)
@SuppressWarnings("unused")
public class Geometry {
    String type;
    Double[] coordinates;
}
