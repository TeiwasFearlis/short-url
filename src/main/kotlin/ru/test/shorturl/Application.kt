package ru.test.shorturl


import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ResponseStatusException
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

@SpringBootApplication
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
        UrlRepo(env.getRequiredProperty("db.schema"), ref(),ref())
    }

    bean {
        router(env.getRequiredProperty("hostName"), ref())
    }
}


private fun router(hostName: String, urlRepo: Repo) = coRouter {
    GET("/save") { request ->
        val url = request.queryParam("url").get()
        try {
            URL(url)
        } catch (e: MalformedURLException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        ServerResponse.ok()
                .body(BodyInserters.fromValue(hostName + urlRepo.addInDB(request.queryParam("url").get())))
                .awaitSingle()
    }
    GET("/go/{key}") { req: ServerRequest ->
        val url = urlRepo.getUrl(req.pathVariable("key")) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val allHeadersAsString = getAllHeadersAsString(req)
        urlRepo.saveRedirect(url, allHeadersAsString)
        ServerResponse.temporaryRedirect(URI.create(url))
                .build().awaitSingle()
    }
   }