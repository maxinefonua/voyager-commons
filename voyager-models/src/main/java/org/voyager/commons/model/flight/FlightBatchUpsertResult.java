package org.voyager.commons.model.flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class FlightBatchUpsertResult {
    private int updatedCount;
    private int createdCount;
    private int skippedCount;
}
