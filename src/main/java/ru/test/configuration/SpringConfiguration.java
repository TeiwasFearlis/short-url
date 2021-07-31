package ru.test.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import ru.test.dataBase.DataBase;
import ru.test.dataBase.PostgreDataBase;
import ru.test.keyGeneration.KeyCreater;
import java.net.URI;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration

public class SpringConfiguration {


    @Bean
    public KeyCreater keyCreater() {
        return new KeyCreater();
    }


    @Bean
    public DataBase postgreDataBase() {
        return new PostgreDataBase();
    }




    @Bean
    RouterFunction<ServerResponse> routerFunction() {

        return route(GET("/somethingServer/{key}"), req ->
                ServerResponse.temporaryRedirect(URI.create(postgreDataBase().returnUrl(req.pathVariable("key"))))
                        .build())
                .andRoute(RequestPredicates.GET("/saveUrl")
                        , serverRequest -> {
                            String url = serverRequest.queryParam("url").get();
                            return ServerResponse
                                    .ok()
                                    .body(BodyInserters.fromValue(postgreDataBase().addInDB(url)
                                            +serverRequest.headers()));//todo delete headers
                        });

    }



}

