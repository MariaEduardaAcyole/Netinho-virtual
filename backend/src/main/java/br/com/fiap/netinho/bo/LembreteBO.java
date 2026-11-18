package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.IdosoDAO;
import br.com.fiap.netinho.dao.LembreteDAO;
import br.com.fiap.netinho.exception.RecursoNaoEncontradoException;
import br.com.fiap.netinho.model.Lembrete;
import br.com.fiap.netinho.model.StatusLembrete;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Valida o idoso relacionado e gerencia consultas, criacao, atualizacao e remocao de lembretes.
 */
@Service
public class LembreteBO {
    private final LembreteDAO lembreteDAO;
    private final IdosoDAO idosoDAO;
    public LembreteBO(LembreteDAO lembreteDAO,IdosoDAO idosoDAO){this.lembreteDAO=lembreteDAO;this.idosoDAO=idosoDAO;}
    public List<Lembrete> listar(){return lembreteDAO.listar();}
    public Lembrete buscar(Long id){return lembreteDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Lembrete nao encontrado."));}
    public List<Lembrete> listarPorIdoso(Long id){idosoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));return lembreteDAO.listarPorIdoso(id);}
    public Long criar(Lembrete x){
        idosoDAO.buscarPorId(x.getIdIdoso()).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        if(x.getStatus()==StatusLembrete.CUMPRIDO&&x.getDataCumprimento()==null)x.setDataCumprimento(LocalDateTime.now());
        if(x.getStatus()!=StatusLembrete.CUMPRIDO)x.setDataCumprimento(null);
        return lembreteDAO.inserir(x);
    }
    public void atualizar(Long id,Lembrete x){
        buscar(id);idosoDAO.buscarPorId(x.getIdIdoso()).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        if(x.getStatus()==StatusLembrete.CUMPRIDO&&x.getDataCumprimento()==null)x.setDataCumprimento(LocalDateTime.now());
        if(x.getStatus()!=StatusLembrete.CUMPRIDO)x.setDataCumprimento(null);
        lembreteDAO.atualizar(id,x);
    }
    public void excluir(Long id){buscar(id);lembreteDAO.excluir(id);}
}
