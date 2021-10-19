package ru.test.shorturl

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ResponseStatusException
import java.net.URI


fun getAllHeadersAsString(request: ServerRequest): String {
    val builder = StringBuilder()
    for ((key, value) in request.headers().asHttpHeaders()) {
        builder.append(key).append("=").append(value).append(";")
    }
    return builder.toString()
}

internal fun router(hostName: String, urlRepo: Repo) = coRouter {

    POST("/save") { request ->
        val url = request.queryParam("url").get()
        val key = urlRepo.getKey(request.queryParam("url").get())
        if (url.validate()) {
            ServerResponse.ok()
                    .body(BodyInserters.fromValue(hostName + key))
                    .awaitSingle()
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }
    GET("/{key}") { req: ServerRequest ->
        val composeUrl = urlRepo.getUrl(req.pathVariable("key"))
        val allHeadersAsString = getAllHeadersAsString(req)
        urlRepo.saveRedirect(composeUrl.targetUrl ?: composeUrl.redirectUrl, allHeadersAsString)
        ServerResponse.temporaryRedirect(URI.create(composeUrl.redirectUrl))
                .build()
                .awaitSingle()
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
            val decodeFromString = Json.decodeFromString<ImportRequest>(postBody)
            val group = decodeFromString.group.trim()
            decodeFromString.urls.forEach { entry ->
                val shortUrl = entry.readyKey.trim()
                val value = entry.targetUrl.trim()
                if (shortUrl.validate() && value.validate()) {
                    val key = shortUrl.substring(shortUrl.lastIndexOf("/") + 1).trim()
                    urlRepo.saveImport(key, value, group)
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                }
            }
            ServerResponse.ok()
                    .build()
                    .awaitSingle()
        }
    }
}

fun String.validate(): Boolean = this.startsWith("http://") || this.startsWith("https://")


@Serializable
data class ImportRequest(val group: String, val urls: List<ReadyKeyWithUrl>)

@Serializable
data class ReadyKeyWithUrl(val readyKey: String, val targetUrl: String)


data class ComposeUrl(val redirectUrl: String, val targetUrl: String?)

data class ImpUrl(val shortKey: String?, val url: String?)
