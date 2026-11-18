package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.bo.CrudBO;
import br.com.fiap.netinho.dto.Dtos.*;
import br.com.fiap.netinho.model.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * Exp?e rotas REST de cadastro e consulta de idosos, familiares, voluntarios, guias e passos.
 */
@RestController
public class CrudController {
    private final CrudBO bo;
    public CrudController(CrudBO bo){this.bo=bo;}

    @GetMapping("/idosos") public List<Idoso> listarIdosos(){return bo.listarIdosos();}
    @GetMapping("/idosos/{id}") public Idoso buscarIdoso(@PathVariable Long id){return bo.buscarIdoso(id);}
    @PostMapping("/idosos") public ResponseEntity<Idoso> criarIdoso(@RequestBody Idoso x){Long id=bo.criarIdoso(x);x.setId(id);return ResponseEntity.created(URI.create("/idosos/"+id)).body(x);}
    @PutMapping("/idosos/{id}") public Idoso atualizarIdoso(@PathVariable Long id,@RequestBody Idoso x){bo.atualizarIdoso(id,x);x.setId(id);return x;}
    @DeleteMapping("/idosos/{id}") public ResponseEntity<Void> excluirIdoso(@PathVariable Long id){bo.excluirIdoso(id);return ResponseEntity.noContent().build();}
    @PutMapping("/idosos/{id}/modo-sem-leitura") public ResponseEntity<Void> modo(@PathVariable Long id,@RequestBody ModoSemLeituraRequest r){bo.modoSemLeitura(id,r.ativo());return ResponseEntity.noContent().build();}

    @GetMapping("/familiares") public List<Familiar> familiares(){return bo.listarFamiliares();}
    @GetMapping("/familiares/{id}") public Familiar familiar(@PathVariable Long id){return bo.buscarFamiliar(id);}
    @PostMapping("/familiares") public ResponseEntity<Familiar> criarFamiliar(@RequestBody Familiar x){Long id=bo.criarFamiliar(x);x.setId(id);return ResponseEntity.created(URI.create("/familiares/"+id)).body(x);}
    @PutMapping("/familiares/{id}") public Familiar atualizarFamiliar(@PathVariable Long id,@RequestBody Familiar x){bo.atualizarFamiliar(id,x);x.setId(id);return x;}
    @DeleteMapping("/familiares/{id}") public ResponseEntity<Void> excluirFamiliar(@PathVariable Long id){bo.excluirFamiliar(id);return ResponseEntity.noContent().build();}

    @GetMapping("/voluntarios") public List<Voluntario> voluntarios(){return bo.listarVoluntarios();}
    @GetMapping("/voluntarios/{id}") public Voluntario voluntario(@PathVariable Long id){return bo.buscarVoluntario(id);}
    @PostMapping("/voluntarios") public ResponseEntity<Voluntario> criarVoluntario(@RequestBody Voluntario x){Long id=bo.criarVoluntario(x);x.setId(id);return ResponseEntity.created(URI.create("/voluntarios/"+id)).body(x);}
    @PutMapping("/voluntarios/{id}") public Voluntario atualizarVoluntario(@PathVariable Long id,@RequestBody Voluntario x){bo.atualizarVoluntario(id,x);x.setId(id);return x;}
    @DeleteMapping("/voluntarios/{id}") public ResponseEntity<Void> excluirVoluntario(@PathVariable Long id){bo.excluirVoluntario(id);return ResponseEntity.noContent().build();}
    @PutMapping("/voluntarios/{id}/status") public ResponseEntity<Void> statusVoluntario(@PathVariable Long id,@RequestBody StatusRequest r){bo.statusVoluntario(id,StatusVoluntario.valueOf(r.status()));return ResponseEntity.noContent().build();}

    @GetMapping("/guias") public List<Guia> guias(){return bo.listarGuias();}
    @GetMapping("/guias/{id}") public Guia guia(@PathVariable Integer id){return bo.buscarGuia(id);}
    @PostMapping("/guias") public ResponseEntity<Guia> criarGuia(@RequestBody Guia x){Integer id=bo.criarGuia(x);x.setId(id);return ResponseEntity.created(URI.create("/guias/"+id)).body(x);}
    @PutMapping("/guias/{id}") public Guia atualizarGuia(@PathVariable Integer id,@RequestBody Guia x){bo.atualizarGuia(id,x);x.setId(id);return x;}
    @DeleteMapping("/guias/{id}") public ResponseEntity<Void> excluirGuia(@PathVariable Integer id){bo.excluirGuia(id);return ResponseEntity.noContent().build();}
    @GetMapping("/guias/{id}/passos") public List<Passo> passos(@PathVariable Integer id){return bo.listarPassos(id);}
    @PostMapping("/guias/{id}/passos") public ResponseEntity<Passo> criarPasso(@PathVariable Integer id,@RequestBody Passo x){Long pid=bo.criarPasso(id,x);x.setId(pid);return ResponseEntity.created(URI.create("/passos/"+pid)).body(x);}
    @PutMapping("/passos/{id}") public Passo atualizarPasso(@PathVariable Long id,@RequestBody Passo x){bo.atualizarPasso(id,x);x.setId(id);return x;}
    @DeleteMapping("/passos/{id}") public ResponseEntity<Void> excluirPasso(@PathVariable Long id){bo.excluirPasso(id);return ResponseEntity.noContent().build();}
}
