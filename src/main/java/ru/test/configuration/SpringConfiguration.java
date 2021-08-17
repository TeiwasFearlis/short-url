package ru.test.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
//import ru.test.dataBase.DataBase;
//import ru.test.dataBase.PostgreDataBase;
import ru.test.shorturl.DataBase;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

//
//@Configuration
//
//public class SpringConfiguration {
//
//
//    @Value("${nameHost}")
//    public String hostname;
//
//    @Value("${db.schema}")
//    public String dbSchema;
//
//    @Bean
//    public DataBase postgreDataBase() {
//        return new PostgreDataBase(hostname, dbSchema);
//    }
//
//    @Bean
//    RouterFunction<ServerResponse> routerFunction() {
//
//        return route(GET("/go/{key}"), req -> {
//                    return ServerResponse.temporaryRedirect(URI.create(postgreDataBase()
//                            .returnUrl(req.pathVariable("key"),req)))
//                            .build();
//                }
//        )
//                .andRoute(RequestPredicates.GET("/saveUrl")
//                        , serverRequest -> {
//                            String url = serverRequest.queryParam("url").get();
//                            return ServerResponse
//                                    .ok()
//                                    .body(BodyInserters.fromValue(postgreDataBase().addInDB(url)));
//                        });
//    }
//}

