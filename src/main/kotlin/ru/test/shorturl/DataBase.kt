package ru.test.shorturl

import org.springframework.web.reactive.function.server.ServerRequest

interface DataBase {
    fun addInDB(url: String):String
    fun returnUrl(id: String, request: ServerRequest):String
}
