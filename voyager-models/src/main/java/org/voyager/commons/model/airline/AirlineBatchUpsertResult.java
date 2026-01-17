package org.voyager.commons.model.airline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirlineBatchUpsertResult {
    private int updatedCount;
    private int createdCount;
    private int skippedCount;
}
