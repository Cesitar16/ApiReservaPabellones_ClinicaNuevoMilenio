package com.clinicanuevomilenio.ApiReservaPabellon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "pabellonClient")
    public WebClient pabellonWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8003/api/pabellones")
                .build();
    }

    @Bean(name = "usuarioClient")
    public WebClient usuarioWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8000/api/usuarios")
                .build();
    }
}