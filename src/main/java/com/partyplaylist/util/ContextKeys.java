package com.partyplaylist.util;

/** Имена атрибутов ServletContext, под которыми лежат общие объекты приложения. */
public final class ContextKeys {
    public static final String CONFIG = "appConfig";
    public static final String DATA_SOURCE = "dataSource";
    public static final String STORAGE_DIR = "storageDir";

    private ContextKeys() {
    }
}
