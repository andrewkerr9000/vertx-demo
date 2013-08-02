package com.unrulymedia.vertx_demo;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Map;

public class DemoServerVerticle extends Verticle
{

  public static final String MONITOR_DATA = "monitor";
  public static final String WEB_RESOURCE_DIR = "src/main/resources/web/";
  public static final String FILENAME_PARAM = "filename";
  public static final String ZIPCODE_PARAM = "zipcode";
  private int fileServes = 0;
  private int zipCode = 0;

  @Override
  public void start()
  {
    RouteMatcher routeMatcher = new RouteMatcher()
        .get("/resource/:"+FILENAME_PARAM, new Handler<HttpServerRequest>()
        {
          @Override
          public void handle(HttpServerRequest event)
          {
            fileServe(event);
          }
        })
        .get("/zip/:" + ZIPCODE_PARAM, new Handler<HttpServerRequest>()
        {
          @Override
          public void handle(HttpServerRequest event)
          {
            zipCode(event);
          }
        })
        .get("/monitor", new Handler<HttpServerRequest>()
        {
          @Override
          public void handle(HttpServerRequest event)
          {
            monitor(event);
          }
        });

    vertx.createHttpServer()
        .setAcceptBacklog(9000 + 1)
        .requestHandler(routeMatcher)
        .listen(8080);
  }

  private void monitor(HttpServerRequest event)
  {
    Map<String,Object> monitoring = vertx.sharedData().getMap(MONITOR_DATA);
    event.response().end(new JsonObject(monitoring).encode());
  }

  private void zipCode(final HttpServerRequest request)
  {
    vertx.sharedData().getMap(MONITOR_DATA).put(this.toString() + "#zipCode",++zipCode);
    String path = request.params().get(ZIPCODE_PARAM);

    JsonObject query = new JsonObject()
        .putString("action", "findone")
        .putString("collection", "zips")
        .putObject("matcher", new JsonObject().putString("_id", path));

    vertx.eventBus().send("demo.zips", query, new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> message) {
        request.response().putHeader("content-type", "application/json");
        JsonObject body = message.body();
        request.response().end(body.encode());
      }
    });
  }

  private void fileServe(final HttpServerRequest event)
  {
    vertx.sharedData().getMap(MONITOR_DATA).put(this.toString() + "#fileServe",++fileServes);
    event.response().sendFile(WEB_RESOURCE_DIR + event.params().get(FILENAME_PARAM));
  }
}
