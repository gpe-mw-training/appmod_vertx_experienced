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

        // create router
        Router router = Router.router(vertx);

        // define routes
        router.get("/products").handler(this::getProducts);
        router.get("/products/:itemId").handler(this::getProduct);
        router.route("/products").handler(BodyHandler.create());
        router.post("/products").handler(this::addProduct);

        // create http server and listen for incoming requests
        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("catalog.http.port", 8080), result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
    }
    
    private void addProduct(RoutingContext routingContext) {

        // get body content as JSON
        JsonObject json = routingContext.getBodyAsJson();

        // transform JSON payload to a Product object and add to service
        catalogService.addProduct(new Product(json), ar -> {
            if (ar.succeeded()) {
            		routingContext.response().setStatusCode(201).end();
            } else {
            		routingContext.fail(ar.cause());
            }
        });
    }
    
    private void getProducts(RoutingContext routingContext) {

        // get the products
        catalogService.getProducts(ar -> {

            if (ar.succeeded()) {

                // get the result
                List<Product> products = ar.result();

                // transform the List<Product> result to a JsonArray object
                JsonArray json = new JsonArray();
                products.stream()
                    .map(p -> p.toJson())
                    .forEach(p -> json.add(p));

                // Write the JsonArray to the HttpServerResponse
                routingContext.response()
                    .putHeader("Content-type", "application/json")
                    .end(json.encodePrettily());
            } else {
                routingContext.fail(ar.cause());
            }
        });

    }
    
    private void getProduct(RoutingContext routingContext) {
    	
        String itemId = routingContext.request().getParam("itemid");
    
        catalogService.getProduct(itemId, ar -> {
            if (ar.succeeded()) {
                Product product = ar.result();
                JsonObject json;
                if (product != null) {
                    json = product.toJson();
                    routingContext.response()
                        .putHeader("Content-type", "application/json")
                        .end(json.encodePrettily());
                } else {
                    routingContext.fail(404);
                }
            } else {
                routingContext.fail(ar.cause());
            }
        });
    }

}