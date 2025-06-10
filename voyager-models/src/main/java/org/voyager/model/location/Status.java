package org.voyager.model.location;

import lombok.Getter;

public enum Status {
    NEW("Save"),
    SAVED("Saved"),
    ARCHIVED("Archived");

    @Getter
    String display;
    Status(String display) {
        this.display = display;
    }
}