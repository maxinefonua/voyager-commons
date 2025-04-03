package org.voyager;

import org.voyager.service.VerifyType;
import org.voyager.service.impl.VerifyTypeVoyagerAPI;

public class AirportsSync {

    public static void main(String[] args) {
        VerifyType verifyType = new VerifyTypeVoyagerAPI();
        verifyType.run();
    }
}
