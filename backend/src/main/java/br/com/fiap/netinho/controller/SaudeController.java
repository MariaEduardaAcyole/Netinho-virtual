package br.com.fiap.netinho.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Verificacao de saude usada pelo Docker: responde 200 so quando o banco Oracle responde.
 */
@RestController
public class SaudeController {
    private final JdbcTemplate jdbc;
    public SaudeController(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @GetMapping("/saude") public ResponseEntity<Map<String,Object>> saude(){
        try{
            jdbc.queryForObject("SELECT 1 FROM DUAL",Integer.class);
            return ResponseEntity.ok(Map.of("status","ok","banco","ok"));
        }catch(Exception e){
            return ResponseEntity.status(503).body(Map.of("status","erro","banco","indisponivel"));
        }
    }
}
