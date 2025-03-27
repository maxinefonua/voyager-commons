package org.voyager.respository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.voyager.entity.Airport;

import java.util.List;

public interface AirportRepository extends JpaRepository<Airport,String> {
    @Query("SELECT DISTINCT a.iata FROM Airport a")
    List<String> findDistinctIatas();
    @Query("SELECT a.iata,a.name FROM Airport a")
    List<String[]> findAllIataCodesAndNames();
    List<Airport> findByCountryCode(String countryCode, Limit limit);
    List<Airport> findByIata(String iata);
}