package org.voyager;

import org.voyager.service.VerifyAirline;
import org.voyager.service.VerifyType;
import org.voyager.service.impl.VerifyAirlineLocalImpl;
import org.voyager.service.impl.VerifyTypeVoyagerAPI;

public class AirportsSync {

    public static void main(String[] args) {
        VerifyAirline verifyAirline = new VerifyAirlineLocalImpl(10);
        verifyAirline.run();
    }
}
