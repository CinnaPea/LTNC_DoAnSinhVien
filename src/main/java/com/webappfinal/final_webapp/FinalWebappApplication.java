package com.webappfinal.final_webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.webappfinal.final_webapp.util.SqlServerNativeAuthLoader;

@SpringBootApplication
public class FinalWebappApplication {
	static {
		SqlServerNativeAuthLoader.loadIfAvailable();
	}

	public static void main(String[] args) {
		SpringApplication.run(FinalWebappApplication.class, args);
	}

}
