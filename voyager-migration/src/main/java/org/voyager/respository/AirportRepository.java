package org.voyager.respository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.entity.Airport;
import java.util.List;
import java.util.Set;

public interface AirportRepository extends JpaRepository<Airport,String> {
    @Query("SELECT DISTINCT a.iata FROM Airport a")
    Set<String> selectDistinctIataSet();
    List<Airport> findByCountryCode(String countryCode, Limit limit);
    List<Airport> findByIata(String iata);
}