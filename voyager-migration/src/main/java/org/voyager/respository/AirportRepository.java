package org.voyager.respository;

import org.springframework.data.repository.CrudRepository;
import org.voyager.entity.Airport;

public interface AirportRepository extends CrudRepository<Airport,String> {
}