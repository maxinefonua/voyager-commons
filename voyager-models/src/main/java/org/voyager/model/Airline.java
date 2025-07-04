package org.voyager.model;

import lombok.Getter;

public enum Airline {
    DELTA("dl-dal"),
    JAPAN("jl-jal"),
    NORWEGIAN("dy-noz"),
    SOUTHWEST("wn-swa"),
    FINNAIR("ay-fin"),
    AIRNZ("nz-anz"),
    HAWAIIAN("ha-hal"),
    ALASKA("as-asa"),
    UNITED("ua-ual");

    @Getter
    private String pathVariableFR;

    Airline(String pathVariableFR) {
        this.pathVariableFR = pathVariableFR;
    }
}
