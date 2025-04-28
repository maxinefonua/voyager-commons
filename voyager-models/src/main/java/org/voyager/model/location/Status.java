package org.voyager.model.location;

import lombok.Getter;

public enum Status {
    NEW("Add"),
    SAVED("Added"),
    ARCHIVED("Archived"),
    DELETED("Deleted");

    @Getter
    String display;
    Status(String display) {
        this.display = display;
    }
}