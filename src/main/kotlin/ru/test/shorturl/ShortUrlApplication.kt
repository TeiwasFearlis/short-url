package ru.test.shorturl

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import java.net.URI

@Configuration
class ShortUrlApplication {

    @Value("\${hostName}")
    lateinit var hostName: String

    @Value("\${db.schema}")
    lateinit var dbschema: String

    @Bean
    fun extensionFunc () : DataBase {
      return  ExtensionFunc(hostName,dbschema)
    }


    @Bean
    fun routerFunction(): RouterFunction<ServerResponse>? {
        return RouterFunctions.route(RequestPredicates.GET("/go/{key}"),
                { req: ServerRequest ->
            ServerResponse.temporaryRedirect(URI.create(extensionFunc()
                    .returnUrl(req.pathVariable("key"), req)))
                    .build()
        }
        )
                .andRoute(RequestPredicates.GET("/saveUrl")
                        , { serverRequest: ServerRequest ->
                    val url = serverRequest.queryParam("url").get()
                    ServerResponse
                            .ok()
                            .body(BodyInserters.fromValue(extensionFunc().addInDB(url)))
                })
    }

}