package ru.test.shorturl


import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
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
        router(env.getRequiredProperty("host_Name"), ref())
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
                .body(BodyInserters.fromValue(hostName + urlRepo.getKey(request.queryParam("url").get())))
                .awaitSingle()
    }
    GET("/go/{key}") { req: ServerRequest ->
        val composeUrl = urlRepo.getUrl(req.pathVariable("key"))
        val allHeadersAsString = getAllHeadersAsString(req)
            urlRepo.saveRedirect(composeUrl.targetUrl?:composeUrl.redirectUrl, allHeadersAsString)//todo запись идет раньше редиректа
        ServerResponse.temporaryRedirect(URI.create(composeUrl.redirectUrl))
                .build().awaitSingle()
    }
    POST("/import"){ request: ServerRequest ->
        val postBody = request.bodyToMono(String::class.java).awaitSingle()
        val array = arrayListOf<String>()
        postBody.trim().split("\n").forEach { url ->
            url.trim().also {
                if (it.startsWith("http://") || it.startsWith("https://")) {
                    array.add(urlRepo.getKey(it))
                }
            }

        }
        val stringBuilder = StringBuilder()
        array.forEach { key ->
            stringBuilder.append(hostName).append(key).append("\n")
        }
        ServerResponse.ok()
                .body(BodyInserters.fromValue(stringBuilder))
                .awaitSingle()
    }
    POST("/import/ready"){ request: ServerRequest ->
        val postBody = request.bodyToMono(String::class.java).awaitSingle()
        val packageKey = getPackageUrlForOtherDomen(postBody)
        val stringBuilder = StringBuilder()
        var key:String
        packageKey.forEach { x ->
             key = urlRepo.getKey(x.key, x.value)
            stringBuilder.append(hostName).append(key).append("\n")
        }
        ServerResponse.ok()
                .body(BodyInserters.fromValue(stringBuilder))
                .awaitSingle()
    }
}

fun getPackageUrlForOtherDomen(severalUrl: String): HashMap<String, String> {
    val map = HashMap<String, String>()
    severalUrl.split("\n").forEach { urlPackage ->
        urlPackage.trim().also {
            if (it.startsWith("http://") || it.startsWith("https://")) {
            val split = it.split(";")
            map[split[0].trim()] = split[1].trim()
        }
        }
    }
    return map
}


data class ComposeUrl(val redirectUrl:String, val targetUrl:String?)


