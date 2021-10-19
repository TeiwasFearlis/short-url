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




val apiInitializer: ApplicationContextInitializer<GenericApplicationContext> = beans {

    bean<Repo> {
        UrlRepo(env.getRequiredProperty("db.schema"), ref(), ref())
    }

    bean {
        router(env.getRequiredProperty("host_name"), ref())
    }
}



