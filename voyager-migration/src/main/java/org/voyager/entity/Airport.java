package org.voyager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.voyager.model.AirportType;

@Entity
@Table(name="airports")
@Getter @Setter @Builder(toBuilder = true)
@NoArgsConstructor @AllArgsConstructor
public class Airport {
    @Id @Column(length = 3,
            columnDefinition = "bpchar")
    String iata;
    String name;
    @Column(length = 50)
    String city;
    @Column(name="subd",length = 50)
    String subdivision;
    @Column(name="country",length = 2,
            columnDefinition = "bpchar")
    String countryCode;
    @Column(name = "lon")
    Double longitude;
    @Column(name = "lat")
    Double latitude;
    @Column(name = "tz",length = 50)
    String timezoneName;
    @Enumerated(EnumType.STRING)
    AirportType type;
}