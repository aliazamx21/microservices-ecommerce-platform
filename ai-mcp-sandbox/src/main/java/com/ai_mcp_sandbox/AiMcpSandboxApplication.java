package com.ai_mcp_sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ai_mcp_sandbox", "com.ecom.sandbox"})
public class AiMcpSandboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiMcpSandboxApplication.class, args);
	}

}