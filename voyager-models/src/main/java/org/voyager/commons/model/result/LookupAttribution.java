package org.voyager.commons.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder @NoArgsConstructor
@AllArgsConstructor @Getter @ToString
public class LookupAttribution {
    String name;
    String link;
}
