package com.redhat.coolstore.catalog.api;

import java.util.List;

import com.redhat.coolstore.catalog.model.Product;
import com.redhat.coolstore.catalog.verticle.service.CatalogService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class ApiVerticle extends AbstractVerticle {

    private CatalogService catalogService;
    
    private String version = "v1";
    
    public ApiVerticle(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        Router router = Router.router(vertx);
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

    private void getProducts(RoutingContext rc) {
        catalogService.getProducts(ar -> {
            if (ar.succeeded()) {
                
                // setup the version
                JsonObject json = new JsonObject(); 
                json.put("version", version);

                // create json array of products
                List<Product> products = ar.result();
                JsonArray jsonArray = createProductJsonArray(products);
                
                // setup the data
                json.put("data", jsonArray);
                
                // send back the response
                rc.response()
                    .putHeader("Content-type", "application/json")
                    .end(json.encodePrettily());
                
            } else {
                rc.fail(ar.cause());
            }
        });
    }

	private JsonArray createProductJsonArray(List<Product> products) {
		JsonArray jsonArray = new JsonArray();
		
		products.stream()
		    .map(p -> p.toJson())
		    .forEach(p -> jsonArray.add(p));
		return jsonArray;
	}

    private void getProduct(RoutingContext rc) {
        String itemId = rc.request().getParam("itemid");
        catalogService.getProduct(itemId, ar -> {
            if (ar.succeeded()) {
            	
                // setup the version
                JsonObject json = new JsonObject(); 
                json.put("version", version);
            	
                Product product = ar.result();
                
                if (product != null) {
                    JsonObject jsonProduct = product.toJson();

                    // setup the data
                    json.put("data", jsonProduct);

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
