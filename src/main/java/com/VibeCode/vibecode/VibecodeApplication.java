package com.VibeCode.vibecode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication (exclude ={org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class})
public class VibecodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(VibecodeApplication.class, args);
	}

}
