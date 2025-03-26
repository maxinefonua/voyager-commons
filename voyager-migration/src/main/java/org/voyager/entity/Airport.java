package org.voyager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="airports")
@Getter @Setter
@NoArgsConstructor
public class Airport {
    @Id
    String icao;
    String iata;
    String name;
    String city;
    @Column(name="subd")
    String subdivision;
    @Column(name="country")
    String countryCode;
    Float elevation;
    @Column(name = "lon")
    Double longitude;
    @Column(name = "lat")
    Double latitude;
    @Column(name = "tz")
    String timezoneName;
    @Column(name = "lid")
    String locationIdFAA;
}