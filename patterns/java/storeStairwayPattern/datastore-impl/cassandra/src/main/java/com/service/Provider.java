package com.service;

import com.api.DataStore;

public class Provider {
    public static DataStore getDataStore() {
        return new DataStoreCassandraImpl();
    }
}
