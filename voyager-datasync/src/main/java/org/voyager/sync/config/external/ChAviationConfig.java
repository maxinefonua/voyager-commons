package org.voyager.sync.config.external;

import lombok.Getter;

public class ChAviationConfig {
    @Getter
    private final String baseURL = "https://www.ch-aviation.com";

    @Getter
    private final String airportsPath = baseURL.concat("/airports");
}
