package com.redhat.coolstore.catalog.verticle.service;

import java.util.List;

import com.redhat.coolstore.catalog.model.Product;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public interface CatalogService {

    public final static String ADDRESS = "catalog-service";

    public void getProducts(Handler<AsyncResult<List<Product>>> resulthandler);

    public void getProduct(String itemId, Handler<AsyncResult<Product>> resulthandler);

    public void addProduct(Product product, Handler<AsyncResult<String>> resulthandler);

    public void ping(Handler<AsyncResult<String>> resultHandler);

    //
    // TODO: Add code here
    //

}
