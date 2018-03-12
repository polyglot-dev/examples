package com.service;

import akka.Done;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import com.api.Config;
import com.api.DataStore;
import com.datastax.driver.core.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class DataStoreCassandraImpl implements DataStore {

    @Override
    public CompletableFuture<String> getContent(String key, String defaultValue) {
        final Statement stmt = new SimpleStatement(
                "SELECT * FROM akka_stream_java_test.testb WHERE id = '" + key + "'"
        ).setFetchSize(1);
        final CompletionStage<List<Row>> rows = CassandraSource.create(stmt, session)
                .runWith(Sink.seq(), materializer);

        CompletableFuture<List<Row>> listCompletableFuture = rows.toCompletableFuture();

        CompletableFuture<String> value = listCompletableFuture
                .thenApply(l -> (l.size() > 0) ? l.get(0).getString("value") : defaultValue);

        return value;
    }

    @Override
    public CompletableFuture<Boolean> setContent(String key, String value) {

        String tableName = config.getString("tableName");

        final Statement stmt = new SimpleStatement(
                String.format("INSERT INTO %s (id, value) VALUES ('%s', '%s')", tableName, key, value)
        );

        CompletionStage<Done> doneCompletionStage = CassandraSource.create(stmt, session)
                .runWith(Sink.ignore(), materializer);
        return doneCompletionStage.toCompletableFuture().thenApply(u -> Boolean.TRUE);
    }

    ActorSystem system;
    Materializer materializer;
    Session session;

    public Session setupSession() {
        final Session session = Cluster
                .builder()
                .addContactPoint(config.getString("cassandra-cluster-ip"))
                .withPort(config.getInt("cassandra-cluster-port"))
                .build().connect();

        return session;
    }

    public Pair<ActorSystem, Materializer> setupMaterializer() {
        final ActorSystem system = ActorSystem.create();
        final Materializer materializer = ActorMaterializer.create(system);
        return Pair.create(system, materializer);
    }

    Config config = new Config();


    @Override
    public void initialize() {
        final Pair<ActorSystem, Materializer> sysmat = setupMaterializer();
        system = sysmat.first();
        materializer = sysmat.second();
        session = setupSession();
    }

    @Override
    public void terminate() {
        system.terminate();
        System.exit(0);
    }

}
