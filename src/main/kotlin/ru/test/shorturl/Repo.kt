package ru.test.shorturl

interface Repo {
    suspend fun getKey(url: String, fullUrl: String? = null): String
    suspend fun saveRedirect(url: String, headers: String)
    suspend fun getUrl(id: String): ComposeUrl
}
