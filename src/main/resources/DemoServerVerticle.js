var vertx = require('vertx');

var constants = {
    MONITOR_DATA: "monitor",
    WEB_RESOURCE_DIR: "src/main/resources/web/",
    FILENAME_PARAM: "filename",
    ZIPCODE_PARAM: "zipcode"
};

var fileServes = 0;
var zipCodes = 0;

var monitor = function(request) {
    var monitoring = vertx.getMap(constants.MONITOR_DATA);
    request.response.putHeader("content-type", "application/json");
    request.response.end(new org.vertx.java.core.json.JsonObject(monitoring).encode());
};

var zipCode = function(request) {
    vertx.getMap(constants.MONITOR_DATA).put("DemoServerVerticle.js#zipCode",++zipCodes);
    var path = request.params().get(constants.ZIPCODE_PARAM);
    var query = {
        action: "findone",
        collection: "zips",
        matcher: {
            _id: path
        }
    };

    vertx.eventBus.send("demo.zips", query, function(message) {
        request.response.putHeader("content-type", "application/json");
        request.response.end(JSON.stringify(message));
      });
};

var fileServe = function(request) {
    vertx.getMap(constants.MONITOR_DATA).put("DemoServerVerticle.js#fileServe",++fileServes);
    request.response.sendFile(constants.WEB_RESOURCE_DIR + request.params().get(constants.FILENAME_PARAM));
};

var routeMatcher = new vertx.RouteMatcher()
        .get("/resource/:"+constants.FILENAME_PARAM, fileServe)
        .get("/zip/:" + constants.ZIPCODE_PARAM, zipCode)
        .get("/monitor", monitor);


vertx.createHttpServer()
        .requestHandler(routeMatcher)
        .requestHandler(routeMatcher)
        .listen(8080);


