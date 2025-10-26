package org.voyager.sdk.error;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor @NoArgsConstructor
@Data
@Builder @EqualsAndHashCode(callSuper = false)
public class VoyagerServiceException extends RuntimeException {
    String timestamp;
    int status;
    String error;
    String message;
    String path;

    @Override
    public String getMessage() {
        return this.message;
    }
}