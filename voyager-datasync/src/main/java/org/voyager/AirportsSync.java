package org.voyager;

import org.voyager.service.VerifyType;
import org.voyager.service.impl.VerifyTypeLocalImpl;

public class AirportsSync {

    public static void main(String[] args) {
        VerifyType verifyType = new VerifyTypeLocalImpl(20);
        verifyType.run();
    }
}
