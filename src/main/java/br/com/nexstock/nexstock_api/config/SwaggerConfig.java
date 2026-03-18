package br.com.nexstock.nexstock_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI nexStockOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NexStock API")
                        .description("""
                            API REST do Sistema de Estoque Offline-First com sincronização incremental.
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NexStock")
                                .email("contato@nexstock.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Desenvolvimento local")
                ));
    }
}