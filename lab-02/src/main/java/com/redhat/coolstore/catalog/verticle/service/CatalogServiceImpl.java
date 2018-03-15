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
    		
    		// TODO: Replace this method
    	
    }

    @Override
    public void getProduct(String itemId, Handler<AsyncResult<Product>> resulthandler) {
    	
		// TODO: Replace this method

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
    	
		// TODO: Replace this method
    		return null;

    }

}
