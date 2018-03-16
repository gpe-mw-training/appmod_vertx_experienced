package com.redhat.coolstore.catalog.verticle.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.redhat.coolstore.catalog.model.Product;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class CatalogServiceImpl implements CatalogService {

    private MongoClient client;

    public CatalogServiceImpl(Vertx vertx, JsonObject config, MongoClient client) {
        this.client = client;
    }

    @Override
    public void getProducts(Handler<AsyncResult<List<Product>>> resulthandler) {

            JsonObject query = new JsonObject();

            // run query on Mongo
            client.find("products", query, ar -> {

                if (ar.succeeded()) {
                    // get the products
                    List<Product> products = ar.result().stream()
                                           .map(json -> new Product(json))
                                           .collect(Collectors.toList());

                // handle for success
                resulthandler.handle(Future.succeededFuture(products));

            } else {

                // handle for failure
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }

        });
    }
    
    @Override
    public void getProduct(String itemId, Handler<AsyncResult<Product>> resulthandler) {

        JsonObject query = new JsonObject().put("itemId", itemId);

        // run query on Mongo
        client.find("products", query, ar -> {

                if (ar.succeeded()) {
                    // get the product
                    Optional<JsonObject> result = ar.result().stream().findFirst();

                    // handle for success
                    if (result.isPresent()) {
                    resulthandler.handle(Future.succeededFuture(new Product(result.get())));
                } else {
                    resulthandler.handle(Future.succeededFuture(null));
                }

            } else {
                    // handle for failure
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }

        });
    }
    
    @Override
    public void addProduct(Product product, Handler<AsyncResult<String>> resulthandler) {
        client.save("products", toDocument(product), resulthandler);
    }

    @Override
    public void ping(Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture("OK"));
    }

    private JsonObject toDocument(Product product) {

        // convert Product to JsonObject

        JsonObject document = product.toJson();
        document.put("_id", product.getItemId());
        return document;
    }
}
