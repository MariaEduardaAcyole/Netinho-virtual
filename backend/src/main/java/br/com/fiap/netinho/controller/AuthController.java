package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.bo.AuthBO;
import br.com.fiap.netinho.dto.Dtos.LoginRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Exp?e o login do aplicativo: troca usuario e senha por um token de sessao.
 */
@RestController
public class AuthController {
    private final AuthBO authBO;
    public AuthController(AuthBO authBO){this.authBO=authBO;}

    @PostMapping("/auth/login") public Map<String,Object> login(@RequestBody LoginRequest r){return authBO.entrar(r.usuario(),r.senha());}
}
