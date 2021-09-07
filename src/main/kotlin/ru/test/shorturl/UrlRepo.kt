package ru.test.shorturl


import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingleOrNull


open class UrlRepo(private val schema: String, connectionFactory: ConnectionFactory, private val cacheManager: CacheManager) : Repo {


    private val client = DatabaseClient.create(connectionFactory)

   // private val hashMap:HashMap<String,String?> = HashMap()


    override suspend fun addInDB(url: String): String? {

//       val cacheManager  = HashMap<String, HashMap<String,Any>>()
//       val integerCache = HashMap<String,Int>()
//       val stringCache = HashMap<String,String>()
//        cacheManager["string"] = stringCache as  HashMap<String,Any>
//        cacheManager["int"] = integerCache as  HashMap<String,Any>

        //////

//        val string = cacheManager.get("string")
//        string?.put("","")


//        val existUrl: Long? =
//                client.sql("SELECT id From public.url_table where url=:url")
//                        .bind("url", url)
//                        .map { row: Row ->
//                            row.get(0) as Long?
//                        }
//                        .awaitOneOrNull()
//        println("return 1")//todo удалить
//        if (existUrl != null)
//            return existUrl.toString(36)


       // val cache: Cache = cacheManager.getCache("url")!!
        if (cacheManager.cacheNames.add(url)) {
           // return cache.get(url).toString()
        } //lse {

            println("return key")//todo удалить
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
            //hashMap.put(url, key)
           // cache.put(url,key)
            return key
        //}
    }


    override suspend fun saveRedirect(url: String, headers: String) {
        client.sql("INSERT INTO $schema.save_redirect(url,date,headers)" +
                " VALUES(:url, CURRENT_TIMESTAMP,:headers)")
                .bind("url", url)
                .bind("headers", headers)
                .await()
    }



    override suspend fun getUrl(id: String): String? {
        // cacheManager.getCache()
        println("return redirect")
        val returnKeyInId: Long = id.toLong(36)
        return try {
            client.sql("SELECT url From $schema.url_table Where id=:key")
                    .bind("key", returnKeyInId)
                    .map { row: Row ->
                        row.get("url") as String
                    }
                    .awaitSingleOrNull()
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

}
