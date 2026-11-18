package br.com.fiap.netinho.controller;

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
    public TarefaGolpeController(TarefaBO tarefaBO,TarefaDAO tarefaDAO,GolpeBO golpeBO){this.tarefaBO=tarefaBO;this.tarefaDAO=tarefaDAO;this.golpeBO=golpeBO;}

    @PostMapping("/tarefas") public ResponseEntity<Tarefa> criar(@RequestBody TarefaRequest r){Tarefa x=r.idGuia()==null?tarefaBO.criar(r.idIdoso(),r.pedido()):tarefaBO.iniciarGuia(r.idIdoso(),r.idGuia());return ResponseEntity.created(URI.create("/tarefas/"+x.getId())).body(x);}
    @GetMapping("/tarefas/{id}") public Tarefa tarefa(@PathVariable Long id){return tarefaDAO.buscarPorId(id).orElseThrow(()->new br.com.fiap.netinho.exception.RecursoNaoEncontradoException("Tarefa nao encontrada."));}
    @GetMapping("/idosos/{id}/tarefas") public List<Tarefa> tarefas(@PathVariable Long id){return tarefaDAO.listarPorIdoso(id);}
    @PutMapping("/tarefas/{id}/confirmar") public Map<String,Object> confirmar(@PathVariable Long id,@RequestBody ConfirmacaoRequest r){return tarefaBO.confirmar(id,r.resposta());}
    @PutMapping("/tarefas/{id}/desfazer") public ResponseEntity<Void> desfazer(@PathVariable Long id){tarefaBO.desfazer(id);return ResponseEntity.noContent().build();}
    @GetMapping("/tarefas/{id}/passo-atual") public Map<String,Object> passoAtual(@PathVariable Long id){return tarefaBO.passoAtual(id);}
    @PutMapping("/tarefas/{id}/avancar") public Map<String,Object> avancar(@PathVariable Long id,@RequestBody AvancarRequest r){return tarefaBO.avancar(id,r.concluido());}
    @DeleteMapping("/tarefas/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id){if(tarefaDAO.excluir(id)==0)throw new br.com.fiap.netinho.exception.RecursoNaoEncontradoException("Tarefa nao encontrada.");return ResponseEntity.noContent().build();}

    @PostMapping("/golpes/verificar") public Map<String,Object> verificar(@RequestBody GolpeRequest r){return golpeBO.verificar(r.idIdoso(),r.texto(),r.imagem(),TipoOrigemAlerta.MANUAL);}
    @PostMapping("/golpes/notificacao-recebida") public Map<String,Object> notificacao(@RequestBody GolpeRequest r){return golpeBO.verificar(r.idIdoso(),r.texto(),r.imagem(),TipoOrigemAlerta.AUTOMATICO);}
    @GetMapping("/idosos/{id}/golpes") public List<AlertaGolpe> golpes(@PathVariable Long id){return golpeBO.listar(id);}
}
