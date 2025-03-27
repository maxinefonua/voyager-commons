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
}