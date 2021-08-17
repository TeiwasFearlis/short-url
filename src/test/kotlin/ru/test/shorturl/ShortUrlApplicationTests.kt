package ru.test.shorturl

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriBuilder
import java.util.function.Consumer


@SpringBootTest
@AutoConfigureWebTestClient
@Import(EmbeddedPostgresConfiguration::class)
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY, type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
class ShortUrlApplicationTests {

//    @MockBean
//    lateinit var extensionFunc : ExtensionFunc

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var extensionFunc: ExtensionFunc


    @Test
    fun saveAndRedirectUrlGoodTest() {

        val url: String = "https://mister11.github.io/posts/testing_spring_webflux_application/"
        val id: Int = 10000
        val result: String = "localhost:8080/go/${id.toString(36)}"


        Assertions.assertTrue(extensionFunc.addInDB(url).equals(result))

        webClient.get()
                .uri { uriBuilder: UriBuilder ->
                    uriBuilder
                            .path("/saveUrl/")
                            .queryParam("url", url)
                            .build()
                }
                .exchange()
                .expectStatus().isOk


        webClient.get().uri("/go/{key}", id.toString(36))
                .exchange()
                .expectStatus().isTemporaryRedirect


    }

    @Test
    fun badResultTest() {

        val url: String = "ttps://mister11.github.io/posts/testing_spring_webflux_application/"// нарушен протокол (ошибка синтаксиса)
        val exception = Assertions.assertThrows(ResponseStatusException::class.java) { extensionFunc.addInDB(url) }
        Assertions.assertEquals("400 BAD_REQUEST", exception.message)//TODO возможно это одно и тоже, что и вебклиент ниже

        webClient.get()
                .uri { uriBuilder: UriBuilder ->
                    uriBuilder
                            .path("/saveUrl/")
                            .queryParam("url", url)
                            .build()
                }
                .exchange()
                .expectStatus().isBadRequest

    }

    @Test
    fun badResultTest2() {

        val url: String = "https/mister11.github.io/posts/testing_spring_webflux_application/"// нарушен протокол (ошибка синтаксиса)
        val exception = Assertions.assertThrows(ResponseStatusException::class.java) { extensionFunc.addInDB(url) }
        Assertions.assertEquals("400 BAD_REQUEST", exception.message)

        webClient.get()
                .uri { uriBuilder: UriBuilder ->
                    uriBuilder
                            .path("/saveUrl/")
                            .queryParam("url", url)
                            .build()
                }
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun badResultTest3() {

        val url: String = "https://mister11.github.io/posts/testing_spring_webflux_application/"
        val id: Int = 10000
        val result: String = "localhost:8080/go/${id.toString(36)}"


        webClient.get()
                .uri { uriBuilder: UriBuilder ->
                    uriBuilder
                            .path("/saveUrl/")
                            .queryParam("url", url)
                            .build()
                }


        webClient.get()
                .uri { uriBuilder: UriBuilder ->
                    uriBuilder
                            .path("/go/{key}")// правильный ключ 7ps
                            .build("hghj7ps")//ложный ключ
                }
                .exchange()
                .expectStatus().isBadRequest


    }


}

@Configuration
class EmbeddedPostgresConfiguration {
    @Bean
    fun embeddedPostgresCustomizer(): Consumer<EmbeddedPostgres.Builder> {
        return Consumer<EmbeddedPostgres.Builder> { builder -> builder.setPort(7432) }
    }
}