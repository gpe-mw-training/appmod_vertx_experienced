package com.redhat.coolstore.catalog.verticle.service;

import java.util.Optional;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ProxyHelper;

public class CatalogVerticle extends AbstractVerticle {

    private CatalogService service;

    private MongoClient client;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        // create a shared instance of the MongoDB client
        client = MongoClient.createShared(vertx, config());

        // create an instance of the catalog service
        service = CatalogService.create(vertx, config(), client);

        // register the catalog service
        ProxyHelper.registerService(CatalogService.class, vertx, service, CatalogService.ADDRESS);

        startFuture.complete();
        
    }

    @Override
    public void stop() throws Exception {
        Optional.ofNullable(client).ifPresent(c -> c.close());
    }

}
