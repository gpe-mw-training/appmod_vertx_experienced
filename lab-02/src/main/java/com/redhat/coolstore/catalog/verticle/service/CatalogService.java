package com.redhat.coolstore.catalog.verticle.service;

import java.util.List;

import com.redhat.coolstore.catalog.model.Product;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface CatalogService {

    public final static String ADDRESS = "catalog-service";

    public void getProducts(Handler<AsyncResult<List<Product>>> resulthandler);

    public void getProduct(String itemId, Handler<AsyncResult<Product>> resulthandler);

    public void addProduct(Product product, Handler<AsyncResult<String>> resulthandler);

    public void ping(Handler<AsyncResult<String>> resultHandler);

}
