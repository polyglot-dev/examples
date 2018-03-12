package com.service;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.dynamodb.impl.DynamoSettings;
import akka.stream.alpakka.dynamodb.javadsl.DynamoClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.api.Config;
import com.api.DataStore;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Future;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DataStoreDynamoDBImpl implements DataStore {

    static ActorSystem system;
    static ActorMaterializer materializer;
    static DynamoSettings settings;
    static DynamoClient client;

    public Pair<ActorSystem, ActorMaterializer> setupMaterializer() {
        //#init-client
        final ActorSystem system = ActorSystem.create();
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        //#init-client
        return Pair.create(system, materializer);
    }

    public Pair<DynamoSettings, DynamoClient> setupClient() {

        final DynamoClient client = DynamoClient.create(
                new DynamoSettings(
                        config.getString("region")
                        , config.getString("host")
                        , config.getInt("port")
                        , config.getInt("parallelism")
                ), system, materializer);

        return Pair.create(settings, client);
    }


    @Override
    public CompletableFuture<String> getContent(String key, String defaultValue) {

        Map<String, AttributeValue> item = new HashMap<>();

        item.put("id", new AttributeValue().withS(key));

        GetItemRequest itemRequest = new GetItemRequest()
                .withKey(item).withTableName(config.getString("tableName"));

        Future<GetItemResult> r = client.getItem(itemRequest);

        return FutureConverters.toJava(r)
                .toCompletableFuture()
                .thenApply(a -> {
                    if (a.getItem() == null)
                        return defaultValue;
                    return a.getItem().get("value").getS();
                });

    }

    @Override
    public CompletableFuture<Boolean> setContent(String key, String value) {

        Map<String, AttributeValue> item = new HashMap<>();

        item.put("id", new AttributeValue().withS(key));
        item.put("value", new AttributeValue().withS(value));

        PutItemRequest putItemR = new PutItemRequest()
                .withTableName(config.getString("tableName"))
                .withItem(item);

        final Future<PutItemResult> r = client.putItem(putItemR);

        return FutureConverters.toJava(r).toCompletableFuture().thenApply(a -> Boolean.TRUE);

    }

    Config config = new Config();

    @Override
    public void initialize() {

        System.out.println();


        System.setProperty("aws.accessKeyId", config.getString("aws.accessKeyId"));
        System.setProperty("aws.secretKey", config.getString("secret.key"));

        final Pair<ActorSystem, ActorMaterializer> sysmat = setupMaterializer();
        system = sysmat.first();
        materializer = sysmat.second();

        final Pair<DynamoSettings, DynamoClient> setclient = setupClient();
        settings = setclient.first();
        client = setclient.second();
    }

    @Override
    public void terminate() {
        system.terminate();
        System.exit(0);
    }

}
