package org.voyager.error;

import lombok.*;

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
