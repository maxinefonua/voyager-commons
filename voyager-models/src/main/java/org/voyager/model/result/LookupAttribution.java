package org.voyager.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder @NoArgsConstructor
@AllArgsConstructor @Getter
public class LookupAttribution {
    String name;
    String link;
}
