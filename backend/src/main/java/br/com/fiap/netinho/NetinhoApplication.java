package br.com.fiap.netinho;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API; inicia o contexto Spring Boot e registra os componentes da aplicacao.
 */
@SpringBootApplication
public class NetinhoApplication {
    public static void main(String[] args) {
        SpringApplication.run(NetinhoApplication.class, args);
    }
}
