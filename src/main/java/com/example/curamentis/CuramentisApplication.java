package com.example.curamentis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CuramentisApplication {

	public static void main(String[] args) {
		SpringApplication.run(CuramentisApplication.class, args);
	}

}

