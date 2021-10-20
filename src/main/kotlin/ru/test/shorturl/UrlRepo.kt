package ru.test.shorturl


import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.cache.CacheManager
import org.springframework.http.HttpStatus
import org.springframework.r2dbc.core.*
import org.springframework.web.server.ResponseStatusException
import java.lang.IllegalStateException


open class UrlRepo(private val schema: String, connectionFactory: ConnectionFactory, private var cacheManager: CacheManager) : Repo {


    private val client = DatabaseClient.create(connectionFactory)


    override suspend fun getKey(url: String): String {
        val cache = cacheManager.getCache("url")
        if (cache == null) {
            throw IllegalStateException("Mandatory cache 'url' not found!")
        } else {
            val result = cache.get(url, String::class.java)
            if (result != null) {
                return result
            } else {
                val existKey: Long? =
                        client.sql("SELECT id From $schema.url_table where url=:url")
                                .bind("url", url)
                                .map { row: Row ->
                                    row.get(0) as Long?
                                }
                                .awaitOneOrNull()
                if (existKey != null) {
                    val encodedKey = existKey.toString(36)
                    cache.put(url, encodedKey)
                    return encodedKey
                } else {
                    val key = client.sql("INSERT INTO $schema.url_table(url)" +
                            " VALUES(:url)")
                            .bind("url", url)
                            .filter { statement, _ -> statement.returnGeneratedValues("id").execute() }
                            .fetch()
                            .first()
                            .map { row ->
                                row["id"] as Long
                            }
                            .awaitSingle().toString(36)
                    cache.put(url, key)
                    return key
                }
            }
        }
    }

    override suspend fun saveImport(key: String, fullUrl: String, group: String) {
        val existGroup = client.sql("SELECT url From $schema.url_table where group_id=:group and external_key=:key ")
                .bind("group", group)
                .bind("key", key)
                .map { row: Row ->
                    row.get("url") as String
                }
                .awaitOneOrNull()
        if (existGroup != null) {
            throw DuplicateUrlException()
        } else {
            client.sql("INSERT INTO $schema.url_table(external_key,url,group_id)" +
                    " VALUES(:key,:fullUrl,:group)")
                    .bind("key", key)
                    .bind("fullUrl", fullUrl)
                    .bind("group", group)
                    .await()
        }
    }


    private fun DatabaseClient.GenericExecuteSpec.bindConditional(key: String, value: Any?): DatabaseClient.GenericExecuteSpec {
        return if (value == null) {
            this.bindNull(key, String::class.java)
        } else {
            this.bind(key, value)
        }
    }


    override suspend fun saveRedirect(url: String, headers: String) {
        client.sql("INSERT INTO $schema.save_redirect(url,date,headers)" +
                " VALUES(:url, CURRENT_TIMESTAMP,:headers)")
                .bind("url", url)
                .bind("headers", headers)
                .await()
    }

    override suspend fun getUrl(id: String): String {
        val returnKeyInId: Long = id.toLong(36)
        val cache = cacheManager.getCache("id")
        return if (cache == null) {
            throw IllegalStateException("Mandatory cache 'id' not found!")
        } else {
            val result = cache.get(id, String::class.java)
            if (result != null) {
                return result
            } else {
                val url = client.sql("SELECT url From $schema.url_table Where id=:key")
                        .bind("key", returnKeyInId)
                        .map { row: Row ->
                            row.get("url") as String
                        }
                        .awaitSingleOrNull()
                if (url != null) {
                    cache.put(id, url)
                    url
                } else {
                    throw InvalidParameter()
                }
            }
        }
    }

    override suspend fun getGroupUrl(group: String, key: String): String {
        val cache = cacheManager.getCache("group")
        return if (cache == null) {
            throw IllegalStateException("Mandatory cache 'group' not found!")
        } else {
            val result = cache.get(group, String::class.java)
            if (result != null) {
                return result
            } else {
                val url = client.sql("SELECT url From $schema.url_table Where external_key=:key and group_id=:group")
                        .bind("key", key)
                        .bind("group", group)
                        .map { row: Row ->
                            row.get("url") as String
                        }
                        .awaitSingleOrNull()
                if (url != null) {
                    cache.put(group, url)
                    url
                } else {
                    throw InvalidParameter()
                }
            }
        }
    }
}

class DuplicateUrlException : Exception()

class InvalidParameter : Exception()
