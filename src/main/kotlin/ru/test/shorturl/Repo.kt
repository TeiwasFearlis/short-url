package ru.test.shorturl

interface Repo {
    suspend fun addInDB(url: String): String?
    suspend fun saveRedirect(url: String, headers: String)
    suspend fun getUrl(id: String): String?
}
