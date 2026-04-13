package org.voyager.sync.service;

import org.voyager.commons.model.airline.Airline;

public interface AirlineProcessor {
    /*
    * Pulls routes from airline flight radar, creates missing routes in route table
    */
    void process(Airline airline);
}
