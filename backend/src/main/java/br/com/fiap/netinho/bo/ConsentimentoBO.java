package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.ConsentimentoDAO;
import br.com.fiap.netinho.dao.IdosoDAO;
import br.com.fiap.netinho.exception.RecursoNaoEncontradoException;
import br.com.fiap.netinho.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Valida idosos e aplica as operacoes de consulta, criacao e alteracao de consentimentos.
 */
@Service
public class ConsentimentoBO {
    private final ConsentimentoDAO consentimentoDAO;
    private final IdosoDAO idosoDAO;
    public ConsentimentoBO(ConsentimentoDAO consentimentoDAO,IdosoDAO idosoDAO){this.consentimentoDAO=consentimentoDAO;this.idosoDAO=idosoDAO;}
    public List<Consentimento> listar(Long idIdoso){idosoDAO.buscarPorId(idIdoso).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));return consentimentoDAO.listarPorIdoso(idIdoso);}
    public Consentimento criar(Long idIdoso,Consentimento x){
        idosoDAO.buscarPorId(idIdoso).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        x.setIdIdoso(idIdoso);x.setDataConcessao(LocalDateTime.now());x.setDataRevogacao(null);x.setStatus(StatusConsentimento.CONCEDIDO);
        x.setId(consentimentoDAO.inserir(x));return x;
    }
    public Consentimento atualizarStatus(Long id,StatusConsentimento status){
        Consentimento x=consentimentoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Consentimento nao encontrado."));
        LocalDateTime agora=LocalDateTime.now();
        consentimentoDAO.atualizarStatus(id,status,status==StatusConsentimento.REVOGADO?agora:null);
        x.setStatus(status);x.setDataRevogacao(status==StatusConsentimento.REVOGADO?agora:null);return x;
    }
}
