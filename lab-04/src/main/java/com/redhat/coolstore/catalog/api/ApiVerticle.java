package com.redhat.coolstore.catalog.api;

import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.redhat.coolstore.catalog.model.Product;
import com.redhat.coolstore.catalog.verticle.service.CatalogService;

import io.opentracing.log.Fields;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.vertx.ext.web.TracingHandler;
import io.opentracing.tag.Tags;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.jaegertracing.Configuration;

public class ApiVerticle extends AbstractVerticle {

    private CatalogService catalogService;
    private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);
    
    private Tracer tracer;

    public ApiVerticle(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        
        Router router = Router.router(vertx);

        tracer = Configuration.fromEnv().getTracer();
        log.info("start() tracer service name = "+Configuration.fromEnv().getServiceName());
        TracingHandler tHandler = new TracingHandler(tracer);
        router.route().order(-1).handler(tHandler).failureHandler(tHandler);

        router.get("/products").handler(this::getProducts);
        router.get("/product/:itemId").handler(this::getProduct);
        router.route("/product").handler(BodyHandler.create());
        router.post("/product").handler(this::addProduct);


        // Health Checks

        // TODO: Add Health Check code here for
        //       - /health/readiness
        //       - /health/liveness

        // Health Checks
        router.get("/health/readiness").handler(rc -> rc.response().end("OK"));

        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx)
                .register("health", f -> health(f));
        router.get("/health/liveness").handler(healthCheckHandler);
        
        // Static content for swagger docs
        router.route().handler(StaticHandler.create());
        
        
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
    
    private void dumpMultiMap(MultiMap mmap) {
        Iterator<Entry<String, String>> mList = mmap.entries().iterator();
        while(mList.hasNext()) {
            Entry<String, String> mEntry = mList.next();
            System.out.println("\ndumpMultiMap() \tkey = "+mEntry.getKey()+"\t value = "+mEntry.getValue());
        }
    }

    
    private void getProducts(RoutingContext rc) {
        HttpServerRequest hRequest = rc.request();
        MultiMap headers = hRequest.headers();
        dumpMultiMap(headers);
       
        Span span = tracer.buildSpan("getProducts").start();
        /* 
                .asChildOf(TracingHandler.serverSpanContext(rc))
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                .startManual();
        */

        String itemId = rc.request().getParam("itemid");
        try (Scope scope = tracer.scopeManager().activate(span, false)) {
        
            catalogService.getProducts(ar -> {
                if (ar.succeeded()) {
                    List<Product> products = ar.result();
                    JsonArray json = new JsonArray();
                    products.stream()
                        .map(p -> p.toJson())
                        .forEach(p -> json.add(p));
                    rc.response()
                        .putHeader("Content-type", "application/json")
                        .end(json.encodePrettily());
                } else {
                    rc.fail(ar.cause());
                }
            });
        } catch(Exception ex) {
            Tags.ERROR.set(span, true);
            Map logMap = new HashMap();
            logMap.put(Fields.EVENT, "error");
            logMap.put(Fields.ERROR_OBJECT, ex);
            logMap.put(Fields.MESSAGE, ex.getMessage());
            span.log( logMap );
        } finally {
            span.finish();
            log.info("getProducts() span = "+span);
        }
    }

    private void getProduct(RoutingContext rc) {
        String itemId = rc.request().getParam("itemid");
        catalogService.getProduct(itemId, ar -> {
            if (ar.succeeded()) {
                Product product = ar.result();
                JsonObject json;
                if (product != null) {
                    json = product.toJson();
                    rc.response()
                        .putHeader("Content-type", "application/json")
                        .end(json.encodePrettily());
                } else {
                    rc.fail(404);
                }
            } else {
                rc.fail(ar.cause());
            }
        });
    }

    private void addProduct(RoutingContext rc) {
        JsonObject json = rc.getBodyAsJson();
        catalogService.addProduct(new Product(json), ar -> {
            if (ar.succeeded()) {
                rc.response().setStatusCode(201).end();
            } else {
                rc.fail(ar.cause());
            }
        });
    }
    
    private void health(Future<Status> future) {
        catalogService.ping(ar -> {
            if (ar.succeeded()) {
                // HealthCheckHandler has a timeout of 1000s. If timeout is exceeded, the future will be failed
                if (!future.isComplete()) {
                    future.complete(Status.OK());
                }
            } else {
                if (!future.isComplete()) {
                    future.complete(Status.KO());
                }
            }
        });
    }    

}
