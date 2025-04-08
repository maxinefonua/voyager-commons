package org.voyager.service;

import java.util.Map;
import java.util.Set;

public interface VerifyType {
    public void run();
    public void loadCodesToProcess();
    public void filterProcessed();
    public void processRemaining();
    public void saveProcessed();
}
