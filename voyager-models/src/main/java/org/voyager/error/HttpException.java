package org.voyager.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor @NoArgsConstructor
@Data @Builder @EqualsAndHashCode(callSuper = false)
public class HttpException extends RuntimeException {
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
