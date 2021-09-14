package ru.test.shorturl


import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.cache.CacheManager
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.awaitSingleOrNull


open class UrlRepo(private val schema: String, connectionFactory: ConnectionFactory, private var cacheManager: CacheManager) : Repo {


    private val client = DatabaseClient.create(connectionFactory)


    override suspend fun addInDB(url: String): String? {
        val cache = cacheManager.getCache("url")!!
        if (cache.get(url) != null) {
            return cache.get(url, String::class.java)
        } else if (cache.get(url) == null) {
            val existUrl: Long? =
                    client.sql("SELECT id From public.url_table where url=:url")
                            .bind("url", url)
                            .map { row: Row ->
                                row.get(0) as Long?
                            }
                            .awaitOneOrNull()
            if (existUrl != null) {
                val result = existUrl.toString(36)
                cache.put(url, result)
                return result
            }
        } else {//todo нужен ли тут else?
            val key = client.sql("INSERT INTO $schema.url_table(url)" +
                    " VALUES(:url)")
                    .bind("url", url)
                    .filter { statement, _ -> statement.returnGeneratedValues("id").execute() }
                    .fetch()
                    .first()
                    .map { row ->
                        row["id"] as Long
                    }
                    .awaitSingleOrNull()?.toString(36)
            cache.put(url, key)
            return key
        }
        return null
    }


    override suspend fun saveRedirect(url: String, headers: String) {
        client.sql("INSERT INTO $schema.save_redirect(url,date,headers)" +
                " VALUES(:url, CURRENT_TIMESTAMP,:headers)")
                .bind("url", url)
                .bind("headers", headers)
                .await()
    }


    override suspend fun getUrl(id: String): String? {
        val returnKeyInId: Long = id.toLong(36)
        val cache = cacheManager.getCache("id")!!
        if (cache.get(id) != null) {
            return cache.get(id, String::class.java)
        } else {
            return try {
                val returnUrl = client.sql("SELECT url From $schema.url_table Where id=:key")
                        .bind("key", returnKeyInId)
                        .map { row: Row ->
                            row.get("url") as String
                        }
                        .awaitSingleOrNull()
                cache.put(id, returnUrl)
                return returnUrl
            } catch (e: EmptyResultDataAccessException) {
                null
            }
        }
    }
}
