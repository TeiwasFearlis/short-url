package ru.test.shorturl

interface Repo {
    suspend fun getKey(url: String): String
    suspend fun saveRedirect(url: String, headers: String)
    suspend fun getUrl(id: String): String
    suspend fun saveImport(key: String, fullUrl: String, group: String)
    suspend fun getGroupUrl(group: String, key: String): String
}
