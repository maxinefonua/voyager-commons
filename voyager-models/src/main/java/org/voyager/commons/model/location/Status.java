package org.voyager.commons.model.location;

import lombok.Getter;

@Getter
public enum Status {
    NEW("Save"),
    SAVED("Saved"),
    ARCHIVED("Archived"),
    DELETE("Deleted");

    private final String display;
    Status(String display) {
        this.display = display;
    }
}