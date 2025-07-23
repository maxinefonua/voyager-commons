package org.voyager.model;

import lombok.Getter;

public enum Airline {
    DELTA("Delta","dl-dal"),
    JAPAN("Japan Airlines","jl-jal"),
    NORWEGIAN("Norwegian","dy-noz"),
    SOUTHWEST("Southwest","wn-swa"),
    FINNAIR("Finnair","ay-fin"),
    AIRNZ("Air New Zealand","nz-anz"),
    HAWAIIAN("Hawaiian","ha-hal"),
    ALASKA("Alaska","as-asa"),
    UNITED("United","ua-ual"),
    ADVANCED("Advanced","an-wsn"),
    AEGEAN("Aegean","a3-aee"),
    AMERICAN("American","aa-aal"),
    FRONTIER("Frontier","f9-fft"),
    SPIRIT("Spirit","nk-nks"),
    VOLARIS("Volaris","y4-voi"),
    ZIPAIR("Zipair","zg-tzp"),
    AERLINGUS("Aer Lingus","ei-ein"),
    AEROMEXICO("Aeromexico","am-amx"),
    AIRCANADA("Air Canada","ac-aca"),
    AIRCHINA("Air China","ca-cca"),
    AIRFRANCE("Air France","af-afr"),
    ANA("All Nippon Airways","nh-ana"),
    ASIANA("Asiana","oz-aar"),
    EMIRATES("Emirates","ek-uae");

    @Getter
    private String pathVariableFR;

    @Getter
    private String displayText;

    Airline(String displayText, String pathVariableFR) {
        this.displayText = displayText;
        this.pathVariableFR = pathVariableFR;
    }
}
