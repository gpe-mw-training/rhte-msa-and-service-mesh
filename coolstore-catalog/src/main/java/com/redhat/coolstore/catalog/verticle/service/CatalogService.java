package com.redhat.coolstore.catalog.verticle.service;

import java.util.List;

import com.redhat.coolstore.catalog.model.Product;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

@ProxyGen
public interface CatalogService {

    final static String ADDRESS = "catalog-service";

    static CatalogService create(Vertx vertx, JsonObject config, MongoClient client) {
        return new CatalogServiceImpl(vertx, config, client);
    }

    static CatalogService createProxy(Vertx vertx) {
        return new CatalogServiceVertxEBProxy(vertx, ADDRESS);
    }

    void getProducts(Handler<AsyncResult<List<Product>>> resulthandler);

    void getProduct(String itemId, Handler<AsyncResult<Product>> resulthandler);

    void addProduct(Product product, Handler<AsyncResult<String>> resulthandler);

    void ping(Handler<AsyncResult<String>> resultHandler);

}
