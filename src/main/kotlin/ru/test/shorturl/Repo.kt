package ru.test.shorturl

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono

interface Repo {
    suspend fun addInDB(url: String): String?
    suspend fun saveRedirect(url: String, headers: String)
    suspend fun getUrl(id: String): String?
}
