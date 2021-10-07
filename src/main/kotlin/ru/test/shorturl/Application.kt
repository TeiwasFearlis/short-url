package ru.test.shorturl

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ResponseStatusException
import java.net.URI


@SpringBootApplication
@EnableCaching
class BlogApplication

object AppInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext) {
        apiInitializer.initialize(applicationContext)
    }
}

fun main(args: Array<String>) {
    runApplication<BlogApplication>(*args)
}


fun getAllHeadersAsString(request: ServerRequest): String {
    val builder = StringBuilder()
    for ((key, value) in request.headers().asHttpHeaders()) {
        builder.append(key).append("=").append(value).append(";")
    }
    return builder.toString()
}


val apiInitializer: ApplicationContextInitializer<GenericApplicationContext> = beans {

    bean<Repo> {
        UrlRepo(env.getRequiredProperty("db.schema"), ref(), ref())
    }

    bean {
        router(env.getRequiredProperty("host_name"), ref())
    }
}


private fun router(hostName: String, urlRepo: Repo) = coRouter {

    GET("/save") { request ->
        val url = request.queryParam("url").get()
        if (url.validate()) {
            ServerResponse.ok()
                    .body(BodyInserters.fromValue(hostName + urlRepo.getKey(request.queryParam("url").get())))
                    .awaitSingle()
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }
    GET("/go/{key}") { req: ServerRequest ->
        val composeUrl = urlRepo.getUrl(req.pathVariable("key"))
        val allHeadersAsString = getAllHeadersAsString(req)
        urlRepo.saveRedirect(composeUrl.targetUrl ?: composeUrl.redirectUrl, allHeadersAsString)
        ServerResponse.temporaryRedirect(URI.create(composeUrl.redirectUrl))
                .build().awaitSingle()
    }
    contentType(MediaType.APPLICATION_JSON).nest {
        POST("/import") { request: ServerRequest ->
            val postBody = request.awaitBody(String::class)
            val decodeFromString = Json.decodeFromString<List<String>>(postBody)
            val array = ArrayList<String>()
            decodeFromString.forEach { x ->
                val url = x.trim()
                if (url.validate()) {
                    array.add(hostName + urlRepo.getKey(url))
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                }
            }
            ServerResponse.ok()
                    .body(BodyInserters.fromValue(array))
                    .awaitSingle()
        }
        POST("/import/ready") { request: ServerRequest ->
            val postBody = request.awaitBody(String::class)
            val decodeFromString = Json.decodeFromString<List<ImportRequest>>(postBody)
            var shortKey: String
            val array = ArrayList<String>()
            decodeFromString.forEach { entry ->
                val key = entry.ready.trim()
                val value = entry.target.trim()
                if (key.validate() && value.validate()) {
                    shortKey = urlRepo.getKey(key, value)
                    array.add(hostName + shortKey)
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                }
            }
            ServerResponse.ok()
                    .body(BodyInserters.fromValue(array))
                    .awaitSingle()
        }
    }
}

fun String.validate(): Boolean = this.startsWith("http://") || this.startsWith("https://")


@Serializable
data class ImportRequest(val ready: String, val target: String)

data class ComposeUrl(val redirectUrl: String, val targetUrl: String?)


