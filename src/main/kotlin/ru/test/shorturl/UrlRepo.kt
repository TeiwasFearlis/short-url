package ru.test.shorturl


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.server.ResponseStatusException
import java.net.MalformedURLException
import java.net.URL

class UrlRepo(val hostName: String, val schema: String) : Repo {

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate


    override fun addInDB(url: String): String {
        //val ur = URL(url)//TODO почему не предлагает try/catch
        var ur: URL? = null
        try {
            ur = URL(url)
        } catch (e: MalformedURLException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        val searchInColumnUrl = "SELECT url From $schema.url_table Where url='$url'"
        val stringList: List<String> = jdbcTemplate.queryForList(searchInColumnUrl, String::class.java)
        if (stringList.isNotEmpty()) {
            val returnKey = jdbcTemplate
                    .queryForObject("SELECT id From $schema.url_table Where url='$url'", Int::class.java)

            if (returnKey != null)
                return "$hostName${returnKey.toString(36)}"
        }
        jdbcTemplate.update("INSERT INTO $schema.url_table(url)" +
                " VALUES('$url')")
        val sqlCheckUrl = "SELECT id From $schema.url_table Where url='$url'"
        val list = jdbcTemplate.queryForList(sqlCheckUrl, Int::class.java)
        return "$hostName${list[0].toString(36)}"
    }


    override fun saveRedirect(url: String, headers: String) {
        jdbcTemplate.update("INSERT INTO $schema.save_redirect(url,date,headers)" +
                " VALUES('${url}', CURRENT_TIMESTAMP,'${headers}')")
    }

    override fun getUrl(id: String): String? {
        val returnKeyInId: Long = id.toLong(36)
        return try {
            jdbcTemplate.queryForObject("SELECT url From $schema.url_table Where id='$returnKeyInId'",
                    String::class.java)
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }


}
