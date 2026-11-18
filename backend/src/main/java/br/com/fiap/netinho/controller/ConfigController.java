package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.auth.Sessao;
import br.com.fiap.netinho.bo.ConsentimentoBO;
import br.com.fiap.netinho.bo.LembreteBO;
import br.com.fiap.netinho.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * Exp?e rotas REST para consultar e alterar consentimentos e lembretes.
 */
@RestController
public class ConfigController {
    private final ConsentimentoBO consentimentoBO;
    private final LembreteBO lembreteBO;
    public ConfigController(ConsentimentoBO consentimentoBO,LembreteBO lembreteBO){this.consentimentoBO=consentimentoBO;this.lembreteBO=lembreteBO;}

    @GetMapping("/idosos/{id}/consentimentos")
    public List<Consentimento> listarConsentimentos(@PathVariable Long id){return consentimentoBO.listar(id);}
    @PostMapping("/idosos/{id}/consentimentos")
    public ResponseEntity<Consentimento> criarConsentimento(@PathVariable Long id,@RequestBody Consentimento x){
        Consentimento criado=consentimentoBO.criar(id,x);
        return ResponseEntity.created(URI.create("/consentimentos/"+criado.getId())).body(criado);
    }
    @PutMapping("/consentimentos/{id}")
    public Consentimento atualizarConsentimento(@PathVariable Long id,@RequestBody Consentimento x){
        return consentimentoBO.atualizarStatus(id,x.getStatus());
    }

    @GetMapping("/lembretes")
    public List<Lembrete> listarLembretes(){return lembreteBO.listar();}
    @GetMapping("/lembretes/{id}")
    public Lembrete lembrete(@PathVariable Long id){Lembrete l=lembreteBO.buscar(id);Sessao.conferirDono(l.getIdIdoso());return l;}
    @GetMapping("/idosos/{id}/lembretes")
    public List<Lembrete> lembretesIdoso(@PathVariable Long id){return lembreteBO.listarPorIdoso(id);}
    @PostMapping("/lembretes")
    public ResponseEntity<Lembrete> criarLembrete(@RequestBody Lembrete x){
        Long id=lembreteBO.criar(x);x.setId(id);
        return ResponseEntity.created(URI.create("/lembretes/"+id)).body(x);
    }
    @PutMapping("/lembretes/{id}")
    public Lembrete atualizarLembrete(@PathVariable Long id,@RequestBody Lembrete x){Sessao.conferirDono(lembreteBO.buscar(id).getIdIdoso());x.setIdIdoso(Sessao.resolver(x.getIdIdoso()));lembreteBO.atualizar(id,x);x.setId(id);return x;}
    @DeleteMapping("/lembretes/{id}")
    public ResponseEntity<Void> excluirLembrete(@PathVariable Long id){lembreteBO.excluir(id);return ResponseEntity.noContent().build();}
}
