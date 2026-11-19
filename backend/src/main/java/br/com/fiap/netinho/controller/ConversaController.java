package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.auth.Sessao;
import br.com.fiap.netinho.bo.ConversaBO;
import br.com.fiap.netinho.dto.Dtos.ConversaRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Conversa com o Netinho (botao de falar): responde a pessoa e sugere uma funcao do app.
 */
@RestController
public class ConversaController {
    private final ConversaBO conversaBO;
    public ConversaController(ConversaBO conversaBO) { this.conversaBO = conversaBO; }

    @PostMapping("/conversa")
    public Map<String, Object> conversar(@RequestBody ConversaRequest r) {
        return conversaBO.conversar(Sessao.resolver(r.idIdoso()), r.texto(), r.historico());
    }
}
