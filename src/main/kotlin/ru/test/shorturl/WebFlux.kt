package ru.test;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("ru.test")
class BlogApplication

fun main(args: Array<String>) {
    runApplication<BlogApplication>(*args)
}