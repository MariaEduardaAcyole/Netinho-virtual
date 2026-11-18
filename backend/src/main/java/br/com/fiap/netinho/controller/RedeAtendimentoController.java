package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.auth.Sessao;
import br.com.fiap.netinho.bo.*;
import br.com.fiap.netinho.dao.AtendimentoDAO;
import br.com.fiap.netinho.dto.Dtos.*;
import br.com.fiap.netinho.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Exp?e rotas REST da rede de apoio, atendimentos, historico, horas de voluntarios e emergencia.
 */
@RestController
public class RedeAtendimentoController {
    private final RedeApoioBO redeBO;
    private final AtendimentoBO atendimentoBO;
    private final AtendimentoDAO atendimentoDAO;
    public RedeAtendimentoController(RedeApoioBO redeBO,AtendimentoBO atendimentoBO,AtendimentoDAO atendimentoDAO){this.redeBO=redeBO;this.atendimentoBO=atendimentoBO;this.atendimentoDAO=atendimentoDAO;}

    @GetMapping("/idosos/{id}/rede-apoio") public List<RedeApoio> rede(@PathVariable Long id){return redeBO.listar(id);}
    @PostMapping("/idosos/{id}/rede-apoio") public ResponseEntity<Void> criarRede(@PathVariable Long id,@RequestBody RedeApoioRequest r){RedeApoio x=new RedeApoio();x.setIdFamiliar(r.idFamiliar());x.setParentesco(r.parentesco());x.setOrdemContato(r.ordemContato());x.setAutorizado(r.autorizado());redeBO.criar(id,x);return ResponseEntity.created(URI.create("/idosos/"+id+"/rede-apoio/"+r.idFamiliar())).build();}
    @PutMapping("/idosos/{id}/rede-apoio/{idFamiliar}") public ResponseEntity<Void> atualizarRede(@PathVariable Long id,@PathVariable Long idFamiliar,@RequestBody RedeApoioRequest r){RedeApoio x=new RedeApoio();x.setParentesco(r.parentesco());x.setOrdemContato(r.ordemContato());x.setAutorizado(r.autorizado());redeBO.atualizar(id,idFamiliar,x);return ResponseEntity.noContent().build();}
    @DeleteMapping("/idosos/{id}/rede-apoio/{idFamiliar}") public ResponseEntity<Void> excluirRede(@PathVariable Long id,@PathVariable Long idFamiliar){redeBO.excluir(id,idFamiliar);return ResponseEntity.noContent().build();}

    @PostMapping("/atendimentos") public ResponseEntity<Map<String,Object>> criarAtendimento(@RequestBody AtendimentoRequest r){Map<String,Object>x=atendimentoBO.criar(Sessao.resolver(r.idIdoso()),r.idTarefa(),r.idPasso(),r.tentativas());return ResponseEntity.created(URI.create("/atendimentos/"+x.get("id"))).body(x);}
    @GetMapping("/atendimentos/{id}") public Atendimento atendimento(@PathVariable Long id){return atendimentoDAO.buscarPorId(id).orElseThrow(()->new br.com.fiap.netinho.exception.RecursoNaoEncontradoException("Atendimento nao encontrado."));}
    @PutMapping("/atendimentos/{id}/iniciar") public ResponseEntity<Void> iniciar(@PathVariable Long id,@RequestBody IniciarAtendimentoRequest r){atendimentoBO.iniciar(id,r.idFamiliar(),r.idVoluntario());return ResponseEntity.noContent().build();}
    @PutMapping("/atendimentos/{id}/concluir") public ResponseEntity<Void> concluir(@PathVariable Long id){atendimentoBO.concluir(id);return ResponseEntity.noContent().build();}
    @GetMapping("/voluntarios/{id}/relatorio-horas") public Map<String,Object> horas(@PathVariable Long id){return atendimentoBO.horas(id);}

    @GetMapping("/idosos/{id}/historico") public Map<String,Object> historico(@PathVariable Long id,@RequestParam(required=false) Long idFamiliar){return redeBO.historico(id,idFamiliar);}
    @PostMapping("/emergencias") public Map<String,Object> emergencia(@RequestBody EmergenciaRequest r){return redeBO.emergencia(Sessao.resolver(r.idIdoso()));}
}
