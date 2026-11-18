package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.auth.Sessao;
import br.com.fiap.netinho.bo.*;
import br.com.fiap.netinho.dao.TarefaDAO;
import br.com.fiap.netinho.dto.Dtos.*;
import br.com.fiap.netinho.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Exp?e rotas REST para tarefas, progresso dos guias e verificacao de golpes.
 */
@RestController
public class TarefaGolpeController {
    private final TarefaBO tarefaBO;
    private final TarefaDAO tarefaDAO;
    private final GolpeBO golpeBO;
    private final GolpeImagemBO golpeImagemBO;
    public TarefaGolpeController(TarefaBO tarefaBO,TarefaDAO tarefaDAO,GolpeBO golpeBO,GolpeImagemBO golpeImagemBO){this.tarefaBO=tarefaBO;this.tarefaDAO=tarefaDAO;this.golpeBO=golpeBO;this.golpeImagemBO=golpeImagemBO;}

    @PostMapping("/tarefas") public ResponseEntity<Tarefa> criar(@RequestBody TarefaRequest r){Tarefa x=r.idGuia()==null?tarefaBO.criar(Sessao.resolver(r.idIdoso()),r.pedido()):tarefaBO.iniciarGuia(Sessao.resolver(r.idIdoso()),r.idGuia());return ResponseEntity.created(URI.create("/tarefas/"+x.getId())).body(x);}
    @GetMapping("/tarefas/{id}") public Tarefa tarefa(@PathVariable Long id){Tarefa t=tarefaDAO.buscarPorId(id).orElseThrow(()->new br.com.fiap.netinho.exception.RecursoNaoEncontradoException("Tarefa nao encontrada."));Sessao.conferirDono(t.getIdIdoso());return t;}
    @GetMapping("/idosos/{id}/tarefas") public List<Tarefa> tarefas(@PathVariable Long id){return tarefaDAO.listarPorIdoso(id);}
    @PutMapping("/tarefas/{id}/confirmar") public Map<String,Object> confirmar(@PathVariable Long id,@RequestBody ConfirmacaoRequest r){conferirTarefa(id);return tarefaBO.confirmar(id,r.resposta());}
    @PutMapping("/tarefas/{id}/desfazer") public ResponseEntity<Void> desfazer(@PathVariable Long id){conferirTarefa(id);tarefaBO.desfazer(id);return ResponseEntity.noContent().build();}
    @GetMapping("/tarefas/{id}/passo-atual") public Map<String,Object> passoAtual(@PathVariable Long id){conferirTarefa(id);return tarefaBO.passoAtual(id);}
    @PutMapping("/tarefas/{id}/avancar") public Map<String,Object> avancar(@PathVariable Long id,@RequestBody AvancarRequest r){conferirTarefa(id);return tarefaBO.avancar(id,r.concluido());}
    @DeleteMapping("/tarefas/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id){if(tarefaDAO.excluir(id)==0)throw new br.com.fiap.netinho.exception.RecursoNaoEncontradoException("Tarefa nao encontrada.");return ResponseEntity.noContent().build();}

    @PostMapping("/golpes/verificar") public Map<String,Object> verificar(@RequestBody GolpeRequest r){return golpeBO.verificar(Sessao.resolver(r.idIdoso()),r.texto(),r.imagem(),TipoOrigemAlerta.MANUAL);}
    @PostMapping("/golpes/verificar-imagem") public Map<String,Object> verificarImagem(@RequestBody GolpeImagemRequest r){return golpeImagemBO.verificar(Sessao.resolver(r.idIdoso()),r.texto(),r.imagemBase64(),r.tipoImagem());}
    @PostMapping("/golpes/notificacao-recebida") public Map<String,Object> notificacao(@RequestBody GolpeRequest r){return golpeBO.verificar(Sessao.resolver(r.idIdoso()),r.texto(),r.imagem(),TipoOrigemAlerta.AUTOMATICO);}
    /** A tarefa precisa ser do idoso da sessao (se nao existir, o BO responde 404). */
    private void conferirTarefa(Long id){tarefaDAO.buscarPorId(id).ifPresent(t->Sessao.conferirDono(t.getIdIdoso()));}

    @GetMapping("/idosos/{id}/golpes") public List<AlertaGolpe> golpes(@PathVariable Long id){return golpeBO.listar(id);}
}
