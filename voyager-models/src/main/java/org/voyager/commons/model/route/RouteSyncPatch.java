package org.voyager.commons.model.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.commons.validate.annotations.ValidNonNullField;

@ValidNonNullField @Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class RouteSyncPatch {
    private Status status;
    private String error;
}
