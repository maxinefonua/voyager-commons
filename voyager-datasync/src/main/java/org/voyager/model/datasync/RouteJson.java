package org.voyager.model.datasync;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @NoArgsConstructor
@ToString(includeFieldNames = false)
public class RouteJson {
    Airport airport1;
    Airport airport2;
}
