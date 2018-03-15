package com.redhat.coolstore.catalog.verticle.service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class MongoTestBase {

    private static MongodExecutable exe;

    protected MongoClient mongoClient;

    @BeforeClass
    public static void startMongo() throws Exception {

        if (getConnectionString() == null) {
            // create mongodb configs
            IMongodConfig config = new MongodConfigBuilder().version(Version.Main.PRODUCTION).net(new Net(27018, Network.localhostIsIPv6())).build();

            // configure mongodb client
            exe = MongodStarter.getDefaultInstance().prepare(config);

            // start mongodb
            exe.start();
        
        }
    }

    @AfterClass
    public static void stopMongo() {
        if (exe != null) {
            exe.stop();
        }
    }

    protected static String getConnectionString() {
        return getProperty("connection_string");
    }

    protected static String getDatabaseName() {
        return getProperty("db_name");
    }

    protected static String getProperty(String name) {
        String s = System.getProperty(name);
        if (s != null) {
            s = s.trim();
            if (s.length() > 0) {
                return s;
            }
        }

        return null;
    }

    protected JsonObject getConfig() {

        // create json config object
        JsonObject config = new JsonObject();
        String connectionString = getConnectionString();
        
        // setup connection string
        if (connectionString != null) {
            config.put("connection_string", connectionString);
        } else {
            config.put("connection_string", "mongodb://localhost:27018");
        }

        // set up database name
        String databaseName = getDatabaseName();
        if (databaseName != null) {
            config.put("db_name", databaseName);
        }

        return config;
    }

    protected void awaitLatch(CountDownLatch latch, TestContext context) throws InterruptedException {
        context.assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    protected void dropCollection(MongoClient mongoClient, String name, Async async, TestContext context) {
        
        // drop a mongo collection
        mongoClient.getCollections(ar -> {
        
            if (ar.failed()) {
                ar.cause().printStackTrace();
                context.fail(ar.cause().getMessage());
            } else {
        
                // get a list of collections to drop
                AtomicInteger collCount = new AtomicInteger();
                List<String> toDrop = ar.result().stream().filter(l -> l.startsWith(name)).collect(Collectors.toList());
                int count = toDrop.size();
        
                // loop thru and drop collections
                if (!toDrop.isEmpty()) {
                    for (String collection : toDrop) {
                        mongoClient.dropCollection(collection, ar1 -> {
                            if (ar.failed()) {
                                context.fail(ar1.cause().getMessage());
                            } else {
                                if (collCount.incrementAndGet() == count) {
                                    async.complete();
                                }
                            }
                        });
                    }
                } else {
                    async.complete();
                }
            }
        });
    }
}
