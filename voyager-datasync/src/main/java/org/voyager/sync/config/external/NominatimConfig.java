package org.voyager.sync.config.external;

import lombok.Getter;

@Getter
public class NominatimConfig {
    private final String baseURL = "https://nominatim.openstreetmap.org";
    private final String searchPath = "/search";
    private final String searchParams = "q=%s&format=jsonv2";
}
