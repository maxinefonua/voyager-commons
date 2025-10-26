package org.voyager.commons.model.location;

import lombok.Getter;

public enum
Status {
    NEW("Save"),
    SAVED("Saved"),
    ARCHIVED("Archived"),
    DELETE("Deleted");

    @Getter
    String display;
    Status(String display) {
        this.display = display;
    }
}