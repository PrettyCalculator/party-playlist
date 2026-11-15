package com.partyplaylist.model.enums;

public enum PartyStatus {
    DRAFT("Черновик"),
    LIVE("Идёт"),
    FINISHED("Завершена");

    private final String title;

    PartyStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
