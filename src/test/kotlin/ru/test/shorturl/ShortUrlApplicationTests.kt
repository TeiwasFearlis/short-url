package ru.test.shorturl

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.ConnectionFactoryOptions
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.postgresql.ds.common.BaseDataSource
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import javax.sql.DataSource


@SpringBootTest(classes = [BlogApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(EmbeddedPostgresConfiguration::class)
@AutoConfigureEmbeddedDatabase(
    provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.OPENTABLE,
    type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES
)
@ActiveProfiles("test")
class ShortUrlApplicationTests(/*@Autowired urlRepo: UrlRepo*/) {

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var template: JdbcTemplate


    @Test
     fun `save and redirect Url`() {
        val url = "https://www.google.com"
        var key = ""
        webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/save")
                    .queryParam("url", url)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody().consumeWith {
                if (it.responseBody == null) {
                    throw IllegalStateException("Empty body")
                } else {
                    val body = String(it.responseBody as ByteArray)
                    //localhost:8080/go/h42h4h2
                    key = body.substring(body.indexOf("go/") + 3)
                    //TODO check log row exist
                }
            }
        webClient.get().uri("/go/{key}", key)
                .exchange()
                .expectStatus().isTemporaryRedirect
        Assertions.assertNotNull(template.queryForObject("SELECT url From public.url_table Where id=?", String::class.java,key.toLong(36)))
    }

    //
    //    @Test
    //    fun badResultTest() {
    //
    //        val url: String = "ttps://www.google.com"// нарушен протокол (ошибка синтаксиса)
    //        val exception = Assertions.assertThrows(ResponseStatusException::class.java) { urlRepo.addInDB(url) }
    //        Assertions.assertEquals("400 BAD_REQUEST", exception.message)//TODO возможно это одно и тоже, что и вебклиент ниже
    //
    //        webClient.get()
    //                .uri { uriBuilder: UriBuilder ->
    //                    uriBuilder
    //                            .path("/saveUrl/")
    //                            .queryParam("url", url)
    //                            .build()
    //                }
    //                .exchange()
    //                .expectStatus().isBadRequest
    //
    //    }
    //
    //    @Test
    //    fun badResultTest2() {
    //
    //        val url: String = "https/www.google.com"// нарушен протокол (ошибка синтаксиса)
    //        val exception = Assertions.assertThrows(ResponseStatusException::class.java) { urlRepo.addInDB(url) }
    //        Assertions.assertEquals("400 BAD_REQUEST", exception.message)
    //
    //        webClient.get()
    //                .uri { uriBuilder: UriBuilder ->
    //                    uriBuilder
    //                            .path("/saveUrl/")
    //                            .queryParam("url", url)
    //                            .build()
    //                }
    //                .exchange()
    //                .expectStatus().isBadRequest
    //    }

    @Test
    fun `test failed url`() {

        val url: String = "https://mister11.github.io/posts/testing_spring_webflux_application/"

        webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/save/")
                    .queryParam("url", url)
                    .build()
            }.exchange()


        webClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/go/{key}") // правильный ключ 7ps
                    .build("hghj7ps") //ложный ключ
            }
            .exchange()
            .expectStatus().isBadRequest
    }
}

@Configuration
class EmbeddedPostgresConfiguration {
    @Bean
    fun connectionFactory(dataSource: DataSource): ConnectionFactory = EmbeddedPostgresConnectionFactory(dataSource)

    @Bean
    @LiquibaseDataSource
    fun liquibaseDataDs(dataSource: DataSource): DataSource = dataSource

    private class EmbeddedPostgresConnectionFactory(
        private val dataSource: DataSource,
    ) : ConnectionFactory {
        @Volatile
        private var latestPgDs: BaseDataSource? = null
        @Volatile
        private var factory: ConnectionFactory? = null

        private fun connectionFactory(): ConnectionFactory {
            val freshConfig: BaseDataSource = dataSource.unwrap(BaseDataSource::class.java)
            // checks if the target database has changed or not
            if (factory == null || latestPgDs !== freshConfig) {
                latestPgDs = freshConfig
                factory = ConnectionFactoryBuilder.withOptions(
                    ConnectionFactoryOptions.builder().apply {
                        option(ConnectionFactoryOptions.DRIVER, "postgresql")
                        option(ConnectionFactoryOptions.PROTOCOL, "postgresql")
                        option(ConnectionFactoryOptions.HOST, freshConfig.serverNames[0])
                        option(ConnectionFactoryOptions.PORT, freshConfig.portNumbers[0])
                        val databaseName = freshConfig.databaseName
                        if (databaseName != null) {
                            option(ConnectionFactoryOptions.DATABASE, databaseName)
                        }
                        val user = freshConfig.user
                        if (user != null) {
                            option(ConnectionFactoryOptions.USER, user)
                        }
                        val password = freshConfig.password
                        if (password != null) {
                            option(ConnectionFactoryOptions.PASSWORD, password)
                        }
                    }
                ).build()
            }
            return factory!!
        }

        override fun create(): Publisher<out Connection> = connectionFactory().create()

        override fun getMetadata(): ConnectionFactoryMetadata = connectionFactory().metadata
    }
}