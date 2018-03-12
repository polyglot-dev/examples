package com.api;


import java.util.concurrent.CompletableFuture;

public interface DataStore {

    CompletableFuture<String> getContent(String key, String defaultValue);

    CompletableFuture<Boolean> setContent(String key, String value);

    void initialize();

    void terminate();

}
