package ru.test.shorturl


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.ResponseStatusException
import java.net.MalformedURLException
import java.net.URL
import java.sql.Timestamp

class ExtensionFunc(val hostName: String, val schema: String) : DataBase {

    @Autowired
    var jdbcTemplate: JdbcTemplate? = null


    private fun getAllHeadersAsString(request: ServerRequest): String {
        val builder = StringBuilder()
        for ((key, value) in request.headers().asHttpHeaders()) {
            builder.append(key).append("=").append(value).append(";")
        }
        return builder.toString()
    }


    override fun addInDB(url: String): String {
        //val ur = URL(url)//TODO почему не предлагает try/catch
        var ur: URL? = null
        try {
            ur = URL(url)
        } catch (e: MalformedURLException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        val searchInColumnUrl = "SELECT url From $schema.url_table Where url='$url'"
        val stringList: List<String> = jdbcTemplate!!.queryForList(searchInColumnUrl, String::class.java)
        if (stringList.isNotEmpty()) {
            val returnKey = jdbcTemplate!!
                    .queryForObject("SELECT id From $schema.url_table Where url='$url'", Int::class.java)

            if (returnKey != null)
                return "$hostName${returnKey.toString(36)}"
        }
        jdbcTemplate!!.update("INSERT INTO $schema.url_table(url)" +
                " VALUES('$url')")
        val sqlCheckUrl = "SELECT id From $schema.url_table Where url='$url'"
        val list = jdbcTemplate!!.queryForList(sqlCheckUrl, Int::class.java)
        return "$hostName${list[0].toString(36)}"
    }


    override fun returnUrl(id: String, request: ServerRequest): String {
        try {
            val returnKeyInId: Long = id.toLong(36)
            val url = "SELECT url From $schema.url_table Where id='$returnKeyInId'"
            val stringList = jdbcTemplate!!.queryForList(url, String::class.java)
            if (stringList.isNotEmpty()) {
                val date = Timestamp(System.currentTimeMillis())
                jdbcTemplate!!.update("INSERT INTO $schema.save_redirect(url,date,headers)" +
                        " VALUES('${stringList[0]}','$date ','${getAllHeadersAsString(request)}')")
                return stringList[0]
            }
        } catch (e: NumberFormatException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        throw ResponseStatusException(HttpStatus.BAD_REQUEST)//TODO как избавиться от лишнего BAD_REQUEST
    }

}
