package ru.test.shorturl

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@Configuration
class ShortUrlApplication {

    @Value("\${hostName}")
    lateinit var hostName: String

    @Value("\${db.schema}")
    lateinit var dbschema: String

    @Bean
    fun urlRepo(): Repo {
        return UrlRepo(hostName, dbschema)
    }


    @Bean
    fun routerFunction(): RouterFunction<ServerResponse>? {
        return RouterFunctions.route(RequestPredicates.GET("/go/{key}"), { req: ServerRequest ->
            val urlRepo = urlRepo()
            val url = urlRepo.getUrl(req.pathVariable("key")) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            val allHeadersAsString = getAllHeadersAsString(req)
            urlRepo.saveRedirect(url, allHeadersAsString)
            ServerResponse.temporaryRedirect(URI.create(url))
                    .build()
        }).andRoute(RequestPredicates.GET("/saveUrl"), { serverRequest: ServerRequest ->
            val url = serverRequest.queryParam("url").get()
            //check url
            ServerResponse
                    .ok()
                    .body(BodyInserters.fromValue(urlRepo().addInDB(url)))
        })
    }

    private fun getAllHeadersAsString(request: ServerRequest): String {
        val builder = StringBuilder()
        for ((key, value) in request.headers().asHttpHeaders()) {
            builder.append(key).append("=").append(value).append(";")
        }
        return builder.toString()
    }

}