package com.redhat.coolstore.catalog.api;

import java.util.List;

import com.redhat.coolstore.catalog.model.Product;
import com.redhat.coolstore.catalog.verticle.service.CatalogService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ApiVerticle extends AbstractVerticle {

    private CatalogService catalogService;

    public ApiVerticle(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        // TODO: Replace this method

    }

    private void getProducts(RoutingContext rc) {

        // TODO: Replace this method

    }


}