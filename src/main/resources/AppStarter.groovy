// Our application config

def persistorConf = [
        address: 'demo.zips',
        db_name: 'zips',
        host: 'localhost'
]

container.with {
    deployModule('io.vertx~mod-mongo-persistor~2.0.0-final', persistorConf, 4)
    deployVerticle('com.unrulymedia.vertx_demo.DemoServerVerticle', [:], 4)
}
