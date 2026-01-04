package org.voyager.commons.model.route;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class RouteSyncBatchUpdate {
    @NotEmpty
    private List<Integer> routeIdList;
    @NotNull
    private Status status;
}