package ru.test.shorturl

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryMetadata
import io.r2dbc.spi.ConnectionFactoryOptions
import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.assertj.db.api.Assertions.assertThat
import org.assertj.db.api.ChangesAssert
import org.assertj.db.type.Changes
import org.assertj.db.type.Request
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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import javax.sql.DataSource

@SpringBootTest(classes = [BlogApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(EmbeddedPostgresConfiguration::class)
@AutoConfigureEmbeddedDatabase(
        provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.OPENTABLE,
        type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ShortUrlApplicationTests() {

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var ds: DataSource

    //https://github.com/assertj/assertj-examples/blob/main/assertions-examples

    @Test
    fun `save and redirect Url good Test`() {
        var key = ""
        val url = "https://www.google.com"
        assertChanges(ds) {
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
                        }
                    }
        }.hasNumberOfChanges(1)
                .ofCreation().hasNumberOfChanges(1)
                .onTable("url_table").hasNumberOfChanges(1)

        val request = Request(ds, "SELECT url From public.url_table Where id=?", key.toLong(36))
        assertThat(request).row().hasNumberOfColumns(1).hasOnlyNotNullValues().hasValues(url)

        assertChanges(ds) {
            webClient.get().uri("/go/{key}", key)
                    .exchange()
                    .expectStatus().isTemporaryRedirect
        }.hasNumberOfChanges(1)
                .ofCreation().hasNumberOfChanges(1)
                .onTable("save_redirect").hasNumberOfChanges(1)
    }


    @Test
    fun `error 400`() {
        val url = "htps://www.google.com"
        assertChanges(ds) {
            webClient.get()
                    .uri { uriBuilder: UriBuilder ->
                        uriBuilder
                                .path("/save/")
                                .queryParam("url", url)
                                .build()
                    }
                    .exchange()
                    .expectStatus().isBadRequest
        }.hasNumberOfChanges(0)
    }

    @Test
    fun `test failed redirect`() {
        val url = "https://www.google.com"
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
                            .path("/go/{key}")
                            .build("hghj7ps") //ложный ключ
                }
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun `Good test post method package url`() {
        val url = "[ \" https://www.google.com\" , \"https://yandex.ru \" ,\" https://www.yahoo.com \" ]"
        var key = ""
        assertChanges(ds) {
            webClient.post()
                    .uri { uriBuilder: UriBuilder ->
                        uriBuilder
                                .path("/import")
                                .build()
                    }
                    .body(Mono.just(url), String::class.java)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody().consumeWith {
                        if (it.responseBody == null) {
                            throw IllegalStateException("Empty body")
                        } else {
                            val body = String(it.responseBody as ByteArray)
                            val decodeFromString = Json.decodeFromString<ArrayList<String>>(body)
                            val get = decodeFromString.get(0)
                            key = get.substring(get.indexOf("go/") + 3)
                        }
                    }
        }.hasNumberOfChanges(3).ofCreation().hasNumberOfChanges(3).onTable("url_table").hasNumberOfChanges(3)
        val request = Request(ds, "SELECT url From public.url_table Where id=?", key.toLong(36))
        assertThat(request).row().hasNumberOfColumns(1).hasOnlyNotNullValues().hasValues("https://www.google.com")
    }

    @Test
    fun `http syntax test`() {
        val url = "[ \"hts://www.google.com\",\" https://yandex.ru \", \"https//www.yahoo.com\" ]"
        assertChanges(ds) {
            webClient.post()
                    .uri { uriBuilder: UriBuilder ->
                        uriBuilder
                                .path("/import")
                                .build()
                    }
                    .body(Mono.just(url), String::class.java)
                    .exchange()
                    .expectStatus()
                    .isBadRequest
        }.hasNumberOfChanges(0)
    }

    @Test
    fun `Good test post method package short url and full url, and test cache`() {
        val url = "[{\"ready\":\" https://goo.su/7xbP \",\"target\":\" https://www.google.com\"},{\"ready\":\" https://goo.su/7XbP\",\"target\":\" https://yandex.ru\"}, " +
                "{\"ready\":\"https://goo.su/7xBq\",\"target\": \" https://www.yahoo.com\"}] "
        var key = ""
        assertChanges(ds) {
            webClient.post()
                    .uri { uriBuilder: UriBuilder ->
                        uriBuilder
                                .path("/import/ready")
                                .build()
                    }
                    .body(Mono.just(url), String::class.java)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody().consumeWith {
                        if (it.responseBody == null) {
                            throw IllegalStateException("Empty body")
                        } else {
                            val body = String(it.responseBody as ByteArray)
                            val decodeFromString = Json.decodeFromString<ArrayList<String>>(body)
                            val get = decodeFromString.get(0)
                            key = get.substring(get.indexOf("go/") + 3)
                        }
                    }
        }.hasNumberOfChanges(3).ofCreation().hasNumberOfChanges(3).onTable("url_table").hasNumberOfChanges(3)
        assertChanges(ds) {
            webClient.post()
                    .uri { uriBuilder: UriBuilder ->
                        uriBuilder
                                .path("/import/ready")
                                .build()
                    }
                    .body(Mono.just(url), String::class.java)
                    .exchange()
                    .expectStatus()
                    .isOk
        }.hasNumberOfChanges(0).ofCreation().hasNumberOfChanges(0).onTable("url_table")
                .hasNumberOfChanges(0)
        val request = Request(ds, "SELECT url From public.url_table Where id=?", key.toLong(36))
        assertThat(request).row().hasNumberOfColumns(1).hasOnlyNotNullValues().hasValues("https://goo.su/7xbP")
        val request2 = Request(ds, "SELECT external_url From public.url_table Where id=?", key.toLong(36))
        assertThat(request2).row().hasNumberOfColumns(1).hasOnlyNotNullValues().hasValues("https://www.google.com")
    }

}

fun assertChanges(ds: DataSource, call: () -> Unit): ChangesAssert {
    return Changes(ds).let {
        it.setStartPointNow()
        call.invoke()
        it.setEndPointNow()
        assertThat(it)
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