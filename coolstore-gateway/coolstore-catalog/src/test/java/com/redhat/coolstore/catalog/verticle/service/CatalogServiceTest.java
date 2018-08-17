package com.redhat.coolstore.catalog.verticle.service;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.redhat.coolstore.catalog.model.Product;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CatalogServiceTest extends MongoTestBase {

    private Vertx vertx;

    @Before
    public void setup(TestContext context) throws Exception {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
        JsonObject config = getConfig();
        mongoClient = MongoClient.createNonShared(vertx, config);
        Async async = context.async();
        dropCollection(mongoClient, "products", async, context);
        async.await(10000);
    }

    @After
    public void tearDown() throws Exception {
        mongoClient.close();
        vertx.close();
    }

    @Test
    public void testAddProduct(TestContext context) throws Exception {
        String itemId = "999999";
        String name = "productName";
        Product product = new Product();
        product.setItemId(itemId);
        product.setName(name);
        product.setDesc("productDescription");
        product.setPrice(100.0);

        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

        Async async = context.async();

        service.addProduct(product, ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                JsonObject query = new JsonObject().put("_id", itemId);
                mongoClient.findOne("products", query, null, ar1 -> {
                    if (ar1.failed()) {
                        context.fail(ar1.cause().getMessage());
                    } else {
                        assertThat(ar1.result().getString("name"), equalTo(name));
                        async.complete();
                    }
                });
            }
        });
    }

    @Test
    public void testGetProducts(TestContext context) throws Exception {
        Async saveAsync = context.async(2);
        String itemId1 = "111111";
        JsonObject json1 = new JsonObject()
                .put("itemId", itemId1)
                .put("name", "productName1")
                .put("desc", "productDescription1")
                .put("price", new Double(100.0));

        mongoClient.save("products", json1, ar -> {
            if (ar.failed()) {
                context.fail();
            }
            saveAsync.countDown();
        });

        String itemId2 = "222222";
        JsonObject json2 = new JsonObject()
                .put("itemId", itemId2)
                .put("name", "productName2")
                .put("desc", "productDescription2")
                .put("price", new Double(100.0));

        mongoClient.save("products", json2, ar -> {
            if (ar.failed()) {
                context.fail();
            }
            saveAsync.countDown();
        });

        saveAsync.await();

        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

        Async async = context.async();

        service.getProducts(ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                assertThat(ar.result(), notNullValue());
                assertThat(ar.result().size(), equalTo(2));
                Set<String> itemIds = ar.result().stream().map(p -> p.getItemId()).collect(Collectors.toSet());
                assertThat(itemIds.size(), equalTo(2));
                assertThat(itemIds, allOf(hasItem(itemId1),hasItem(itemId2)));
                async.complete();
            }
        });
    }

    @Test
    public void testGetProduct(TestContext context) throws Exception {
        Async saveAsync = context.async(2);
        String itemId1 = "111111";
        JsonObject json1 = new JsonObject()
                .put("itemId", itemId1)
                .put("name", "productName1")
                .put("desc", "productDescription1")
                .put("price", new Double(100.0));

        mongoClient.save("products", json1, ar -> {
            if (ar.failed()) {
                context.fail();
            }
            saveAsync.countDown();
        });

        String itemId2 = "222222";
        JsonObject json2 = new JsonObject()
                .put("itemId", itemId2)
                .put("name", "productName2")
                .put("desc", "productDescription2")
                .put("price", new Double(100.0));

        mongoClient.save("products", json2, ar -> {
            if (ar.failed()) {
                context.fail();
            }
            saveAsync.countDown();
        });

        saveAsync.await();

        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

        Async async = context.async();

        service.getProduct("111111", ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                assertThat(ar.result(), notNullValue());
                assertThat(ar.result().getItemId(), equalTo("111111"));
                assertThat(ar.result().getName(), equalTo("productName1"));
                async.complete();
            }
        });
    }

    @Test
    public void testGetNonExistingProduct(TestContext context) throws Exception {
        Async saveAsync = context.async(1);
        String itemId1 = "111111";
        JsonObject json1 = new JsonObject()
                .put("itemId", itemId1)
                .put("name", "productName1")
                .put("desc", "productDescription1")
                .put("price", new Double(100.0));

        mongoClient.save("products", json1, ar -> {
            if (ar.failed()) {
                context.fail();
            }
            saveAsync.countDown();
        });

        saveAsync.await();

        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

        Async async = context.async();

        service.getProduct("222222", ar -> {
            if (ar.failed()) {
                context.fail(ar.cause().getMessage());
            } else {
                assertThat(ar.result(), nullValue());
                async.complete();
            }
        });
    }

    @Test
    public void testPing(TestContext context) throws Exception {
        CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

        Async async = context.async();
        service.ping(ar -> {
            assertThat(ar.succeeded(), equalTo(true));
            async.complete();
        });
    }

}
