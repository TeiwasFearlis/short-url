package ru.test.shorturl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import kotlin.String


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



