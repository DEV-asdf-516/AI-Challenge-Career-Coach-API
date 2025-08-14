package com.example.resume_coach.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port}")
    private String serverPort;

    @Value("${swagger.server.url}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Resume Coach API")
                        .description("AI 기반 개인 맞춤형 커리어 코치 챗봇 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AI Challenge Team")
                                .url("https://github.com/DEV-asdf-516/AI-Challenge-Career-Coach-API"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl + ":" + serverPort)
                                .description("개인 서버")
                ));
    }
}