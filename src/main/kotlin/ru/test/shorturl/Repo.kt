package ru.test.shorturl

interface Repo {
    suspend fun getKey(url: String): String?
    suspend fun saveRedirect(url: String, headers: String)
    suspend fun getUrl(id: String): String?
    suspend fun getPackageKey(severalUrl:String) : ArrayList<String?>
}
