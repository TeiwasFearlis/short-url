package ru.test.shorturl

import org.springframework.web.reactive.function.server.ServerRequest

interface Repo {
    fun addInDB(url: String):String
    fun saveRedirect(url: String, headers: String)
    fun getUrl(id: String):String?
}
