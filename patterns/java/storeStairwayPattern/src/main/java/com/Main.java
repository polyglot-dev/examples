package com;

import com.api.DataStore;
import com.service.Provider;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws Exception {

        DataStore m = Provider.getDataStore();

        m.initialize();

        if (1 == 1) {
            CompletableFuture<Boolean> obj = m.setContent("key1", "value1");
            obj.thenAccept(r -> {
                System.out.println("ok");
                m.terminate();
            });

        } else {

            CompletableFuture<String> obj = m.getContent("key2", "non content");

            Optional<String> res = Stream.of(obj.get(30, TimeUnit.SECONDS)).findFirst();

            String r = res.get();

            System.out.println(r);

            m.terminate();

        }


    }

}
