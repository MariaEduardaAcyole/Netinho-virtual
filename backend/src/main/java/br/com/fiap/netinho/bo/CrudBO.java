package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.*;
import br.com.fiap.netinho.model.*;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Aplica validacoes e regras de cadastro antes de delegar operacoes de dominio aos DAOs.
 */
@Service
public class CrudBO {
    private final IdosoDAO idosoDAO;private final FamiliarDAO familiarDAO;private final VoluntarioDAO voluntarioDAO;
    private final GuiaDAO guiaDAO;private final PassoDAO passoDAO;
    public CrudBO(IdosoDAO idosoDAO,FamiliarDAO familiarDAO,VoluntarioDAO voluntarioDAO,GuiaDAO guiaDAO,PassoDAO passoDAO){
        this.idosoDAO=idosoDAO;this.familiarDAO=familiarDAO;this.voluntarioDAO=voluntarioDAO;this.guiaDAO=guiaDAO;this.passoDAO=passoDAO;}

    public List<Idoso> listarIdosos(){return idosoDAO.listar();}
    public Idoso buscarIdoso(Long id){return idosoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));}
    public Long criarIdoso(Idoso x){if(x.getDataCadastro()==null)x.setDataCadastro(java.time.LocalDateTime.now());return idosoDAO.inserir(x);}
    public void atualizarIdoso(Long id,Idoso x){buscarIdoso(id);idosoDAO.atualizar(id,x);}
    public void excluirIdoso(Long id){buscarIdoso(id);if(idosoDAO.excluir(id)==0)throw new RecursoNaoEncontradoException("Idoso nao encontrado.");}
    public void modoSemLeitura(Long id,boolean ativo){buscarIdoso(id);idosoDAO.atualizarModoSemLeitura(id,ativo);}

    public List<Familiar> listarFamiliares(){return familiarDAO.listar();}
    public Familiar buscarFamiliar(Long id){return familiarDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Familiar nao encontrado."));}
    public Long criarFamiliar(Familiar x){if(x.getDataCadastro()==null)x.setDataCadastro(java.time.LocalDateTime.now());return familiarDAO.inserir(x);}
    public void atualizarFamiliar(Long id,Familiar x){buscarFamiliar(id);familiarDAO.atualizar(id,x);}
    public void excluirFamiliar(Long id){buscarFamiliar(id);if(familiarDAO.excluir(id)==0)throw new RecursoNaoEncontradoException("Familiar nao encontrado.");}

    public List<Voluntario> listarVoluntarios(){return voluntarioDAO.listar();}
    public Voluntario buscarVoluntario(Long id){return voluntarioDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Voluntario nao encontrado."));}
    public Long criarVoluntario(Voluntario x){if(x.getDataCadastro()==null)x.setDataCadastro(java.time.LocalDateTime.now());return voluntarioDAO.inserir(x);}
    public void atualizarVoluntario(Long id,Voluntario x){buscarVoluntario(id);voluntarioDAO.atualizar(id,x);}
    public void excluirVoluntario(Long id){buscarVoluntario(id);if(voluntarioDAO.excluir(id)==0)throw new RecursoNaoEncontradoException("Voluntario nao encontrado.");}
    public void statusVoluntario(Long id,StatusVoluntario status){buscarVoluntario(id);voluntarioDAO.atualizarStatus(id,status);}

    public List<Guia> listarGuias(){return guiaDAO.listar();}
    public Guia buscarGuia(Integer id){return guiaDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Guia nao encontrado."));}
    public Integer criarGuia(Guia x){if(x.getDataAtualizacao()==null)x.setDataAtualizacao(java.time.LocalDateTime.now());return guiaDAO.inserir(x);}
    public void atualizarGuia(Integer id,Guia x){buscarGuia(id);guiaDAO.atualizar(id,x);}
    public void excluirGuia(Integer id){buscarGuia(id);if(guiaDAO.excluir(id)==0)throw new RecursoNaoEncontradoException("Guia nao encontrado.");}
    public List<Passo> listarPassos(Integer id){buscarGuia(id);return passoDAO.listarPorGuia(id);}
    public Passo buscarPasso(Long id){return passoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Passo nao encontrado."));}
    public Long criarPasso(Integer idGuia,Passo x){buscarGuia(idGuia);x.setIdGuia(idGuia);return passoDAO.inserir(x);}
    public void atualizarPasso(Long id,Passo x){buscarPasso(id);passoDAO.atualizar(id,x);}
    public void excluirPasso(Long id){buscarPasso(id);if(passoDAO.excluir(id)==0)throw new RecursoNaoEncontradoException("Passo nao encontrado.");}
}
