package com.ys.virtualthread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.ys.virtualthread")
public class VirtualThreadApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualThreadApplication.class, args);
	}

}
