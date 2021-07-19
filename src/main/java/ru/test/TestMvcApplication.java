package ru.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("ru.test")
public class TestMvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestMvcApplication.class, args);
	}

}
