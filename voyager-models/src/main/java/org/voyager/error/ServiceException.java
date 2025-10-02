package org.voyager.error;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor @RequiredArgsConstructor
@Data @EqualsAndHashCode(callSuper = false)
@Builder
public class ServiceException extends Exception {
    int status;
    @NonNull
    String message;
    Exception cause;

    @Override
    public String getMessage() {
        return this.message;
    }
}
