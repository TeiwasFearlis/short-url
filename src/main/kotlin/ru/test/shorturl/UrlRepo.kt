package ru.test.shorturl


import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.Row
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.r2dbc.core.*



class UrlRepo(private val schema: String, connectionFactory: ConnectionFactory) : Repo {

    private val client = DatabaseClient.create(connectionFactory)



    override suspend fun addInDB(url: String): String? {
        try {
            val existUrl: Long? =
                   client.sql("SELECT id From $schema.url_table where url=:url")
                    .bind("url", url)
                    .map { row: Row->
                        row.get(0) as Long?
                    }
                           .awaitOneOrNull()
            if (existUrl != null )
                return existUrl.toString(36)
        }catch (e: EmptyResultDataAccessException ){}
        client.sql("INSERT INTO $schema.url_table(url)" +
                " VALUES(:url)")
                .bind("url", url)
                .await()
          return  client
                    .sql("SELECT id From $schema.url_table" +
                            " Where url=:url")
                    .bind("url", url)
                    .map { row: Row ->
                        row.get("id") as Long
                    }
                    .awaitSingleOrNull()?.toString(36)
    }




    override suspend fun saveRedirect(url: String, headers: String) {
        client.sql("INSERT INTO $schema.save_redirect(url,date,headers)" +
                " VALUES(:url, CURRENT_TIMESTAMP,:headers)")
                .bind("url",url)
                .bind("headers",headers)
                .await()
    }




    override suspend fun getUrl(id: String): String? {
        val returnKeyInId: Long = id.toLong(36)
        return   try {
        client.sql("SELECT url From $schema.url_table Where id='$returnKeyInId'")
                    //.bind("id", returnKeyInId)//Todo почему не работает с этим параметром?
                    .map { row: Row ->
                        row.get("url") as String
                    }
                    .awaitSingleOrNull()
         } catch (e: EmptyResultDataAccessException) {
           null
       }
    }

}
