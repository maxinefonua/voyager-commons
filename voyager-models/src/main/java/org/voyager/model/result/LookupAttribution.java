package org.voyager.model.result;

import lombok.*;

@Builder @NoArgsConstructor
@AllArgsConstructor @Getter @ToString
public class LookupAttribution {
    String name;
    String link;
}
