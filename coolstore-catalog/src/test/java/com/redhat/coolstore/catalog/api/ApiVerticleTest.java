package com.redhat.coolstore.catalog.api;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redhat.coolstore.catalog.model.Product;
import com.redhat.coolstore.catalog.verticle.service.CatalogService;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class ApiVerticleTest {

    private Vertx vertx;
    private Integer port;
    private CatalogService catalogService;

    /**
     * Before executing our test, let's deploy our verticle.
     * <p/>
     * This method instantiates a new Vertx and deploy the verticle. Then, it waits in the verticle has successfully
     * completed its start sequence (thanks to `context.asyncAssertSuccess`).
     *
     * @param context the test context.
     */
    @Before
    public void setUp(TestContext context) throws IOException {
      vertx = Vertx.vertx();

      // Register the context exception handler
      vertx.exceptionHandler(context.exceptionHandler());

      // Let's configure the verticle to listen on the 'test' port (randomly picked).
      // We create deployment options and set the _configuration_ json object:
      ServerSocket socket = new ServerSocket(0);
      port = socket.getLocalPort();
      socket.close();

      DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("catalog.http.port", port));

      //Mock the catalog Service
      catalogService = mock(CatalogService.class);

      // We pass the options as the second parameter of the deployVerticle method.
      vertx.deployVerticle(new ApiVerticle(catalogService), options, context.asyncAssertSuccess());
    }

    /**
     * This method, called after our test, just cleanup everything by closing
     * the vert.x instance
     *
     * @param context
     *            the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetProducts(TestContext context) throws Exception {
        String itemId1 = "111111";
        JsonObject json1 = new JsonObject()
                .put("itemId", itemId1)
                .put("name", "productName1")
                .put("desc", "productDescription1")
                .put("price", new Double(100.0));
        String itemId2 = "222222";
        JsonObject json2 = new JsonObject()
                .put("itemId", itemId2)
                .put("name", "productName2")
                .put("desc", "productDescription2")
                .put("price", new Double(100.0));
        List<Product> products = new ArrayList<>();
        products.add(new Product(json1));
        products.add(new Product(json2));
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation){
                Handler<AsyncResult<List<Product>>> handler = invocation.getArgument(0);
                handler.handle(Future.succeededFuture(products));
                return null;
             }
         }).when(catalogService).getProducts(any());

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/products", response -> {
                assertThat(response.statusCode(), equalTo(200));
                assertThat(response.headers().get("Content-type"), equalTo("application/json"));
                response.bodyHandler(body -> {
                    JsonArray json = body.toJsonObject().getJsonArray("data");
                    Set<String> itemIds =  json.stream()
                            .map(j -> new Product((JsonObject)j))
                            .map(p -> p.getItemId())
                            .collect(Collectors.toSet());
                    assertThat(itemIds.size(), equalTo(2));
                    assertThat(itemIds, allOf(hasItem(itemId1),hasItem(itemId2)));
                    verify(catalogService).getProducts(any());
                    async.complete();
                })
                .exceptionHandler(context.exceptionHandler());
            })
            .exceptionHandler(context.exceptionHandler())
            .end();
    }

    @Test
    public void testGetProduct(TestContext context) throws Exception {
        String itemId = "111111";
        JsonObject json = new JsonObject()
                .put("itemId", itemId)
                .put("name", "productName1")
                .put("desc", "productDescription1")
                .put("price", new Double(100.0));
        Product product = new Product(json);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation){
                Handler<AsyncResult<Product>> handler = invocation.getArgument(1);
                handler.handle(Future.succeededFuture(product));
                return null;
             }
         }).when(catalogService).getProduct(eq("111111"),any());

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/product/111111", response -> {
                assertThat(response.statusCode(), equalTo(200));
                assertThat(response.headers().get("Content-type"), equalTo("application/json"));
                response.bodyHandler(body -> {
                    JsonObject result = body.toJsonObject().getJsonObject("data");
                    assertThat(result, notNullValue());
                    assertThat(result.containsKey("itemId"), is(true));
                    assertThat(result.getString("itemId"), equalTo("111111"));
                    verify(catalogService).getProduct(eq("111111"),any());
                    async.complete();
                })
                .exceptionHandler(context.exceptionHandler());
            })
            .exceptionHandler(context.exceptionHandler())
            .end();
    }

    @Test
    public void testGetNonExistingProduct(TestContext context) throws Exception {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation){
                Handler<AsyncResult<Product>> handler = invocation.getArgument(1);
                handler.handle(Future.succeededFuture(null));
                return null;
             }
         }).when(catalogService).getProduct(eq("111111"),any());

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/product/111111", response -> {
                assertThat(response.statusCode(), equalTo(404));
                async.complete();
            })
            .exceptionHandler(context.exceptionHandler())
            .end();
    }

    @Test
    public void testLivenessHealthCheck(TestContext context) {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation){
                Handler<AsyncResult<String>> handler = invocation.getArgument(0);
                handler.handle(Future.succeededFuture("ok"));
                return null;
             }
        }).when(catalogService).ping(any());

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/health/liveness", response -> {
                assertThat(response.statusCode(), equalTo(200));
                response.bodyHandler(body -> {
                    JsonObject json = body.toJsonObject();
                    assertThat(json.toString(), containsString("\"outcome\":\"UP\""));
                    async.complete();
                }).exceptionHandler(context.exceptionHandler());
            })
            .exceptionHandler(context.exceptionHandler())
            .end();
    }

    @Test
    public void testFailingLivenessHealthCheck(TestContext context) {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation){
                Handler<AsyncResult<Long>> handler = invocation.getArgument(0);
                handler.handle(Future.failedFuture("error"));
                return null;
             }
         }).when(catalogService).ping(any());

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/health/liveness", response -> {
                assertThat(response.statusCode(), equalTo(503));
                async.complete();
            })
            .exceptionHandler(context.exceptionHandler())
            .end();
    }

    @Test
    public void testLivenessHealthCheckTimeOut(TestContext context) {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Handler<AsyncResult<String>> handler = invocation.getArgument(0);
                // Simulate long operation to force timeout - default timeout = 1000ms
                vertx.<String>executeBlocking(f -> {
                    try {
                        Thread.sleep(1100);
                    } catch (InterruptedException e) {}
                    f.complete();
                }, res -> handler.handle(Future.succeededFuture("OK")));
                return null;
            }
        }).when(catalogService).ping(any());

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/health/liveness", response -> {
                // HealthCheck Timeout returns a 500 status code
                assertThat(response.statusCode(), equalTo(500));
                async.complete();
            })
            .exceptionHandler(context.exceptionHandler())
            .end();
    }

    @Test
    public void testReadinessHealthCheck(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/health/readiness", response -> {
                assertThat(response.statusCode(), equalTo(200));
                async.complete();
            })
            .exceptionHandler(context.exceptionHandler())
            .end();
    }

}
