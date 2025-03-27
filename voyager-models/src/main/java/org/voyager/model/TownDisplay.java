package org.voyager.model;

import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class TownDisplay {
    @NonNull
    String name;
    @NonNull
    String country;
    @NonNull
    String regionName;
}
