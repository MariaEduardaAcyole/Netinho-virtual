package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.*;
import br.com.fiap.netinho.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Valida relacionamentos da rede de apoio e coordena historico e fluxo de emergencia.
 */
@Service
public class RedeApoioBO {
    private final RedeApoioDAO redeDAO;private final IdosoDAO idosoDAO;private final FamiliarDAO familiarDAO;
    private final TarefaDAO tarefaDAO;private final AlertaGolpeDAO alertaDAO;private final AtendimentoDAO atendimentoDAO;
    private final ConsentimentoDAO consentimentoDAO;

    public RedeApoioBO(RedeApoioDAO redeDAO,IdosoDAO idosoDAO,FamiliarDAO familiarDAO,TarefaDAO tarefaDAO,AlertaGolpeDAO alertaDAO,AtendimentoDAO atendimentoDAO,ConsentimentoDAO consentimentoDAO){
        this.redeDAO=redeDAO;this.idosoDAO=idosoDAO;this.familiarDAO=familiarDAO;this.tarefaDAO=tarefaDAO;this.alertaDAO=alertaDAO;this.atendimentoDAO=atendimentoDAO;this.consentimentoDAO=consentimentoDAO;}

    public List<RedeApoio> listar(Long id){idosoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));return redeDAO.listarPorIdoso(id);}
    public void criar(Long id,RedeApoio x){idosoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));familiarDAO.buscarPorId(x.getIdFamiliar()).orElseThrow(()->new RecursoNaoEncontradoException("Familiar nao encontrado."));
        x.setIdIdoso(id);if(x.getDataCadastro()==null)x.setDataCadastro(LocalDateTime.now());redeDAO.inserir(x);}
    public void atualizar(Long id,Long idFamiliar,RedeApoio x){redeDAO.buscar(id,idFamiliar).orElseThrow(()->new RecursoNaoEncontradoException("Contato da rede nao encontrado."));
        x.setIdIdoso(id);x.setIdFamiliar(idFamiliar);if(x.getDataCadastro()==null)x.setDataCadastro(LocalDateTime.now());redeDAO.atualizar(id,idFamiliar,x);}
    public void excluir(Long id,Long idFamiliar){
        redeDAO.buscar(id,idFamiliar).orElseThrow(()->new RecursoNaoEncontradoException("Contato da rede nao encontrado."));
        redeDAO.excluir(id,idFamiliar);
    }

    public Map<String,Object> historico(Long idIdoso,Long idFamiliar){
        if(idFamiliar!=null){RedeApoio rede=redeDAO.buscar(idIdoso,idFamiliar).orElseThrow(()->new RecursoNaoEncontradoException("Familiar nao pertence ao idoso."));
            if(!rede.isAutorizado())throw new RegraNegocioException("O familiar nao esta autorizado a consultar o historico.");
        }
        Map<String,Object> h=new LinkedHashMap<>();
        h.put("tarefas",tarefaDAO.listarPorIdoso(idIdoso));
        h.put("alertas",alertaDAO.listarPorIdoso(idIdoso));
        h.put("atendimentos",atendimentoDAO.listarPorIdoso(idIdoso));
        return h;
    }
    public Map<String,Object> emergencia(Long idIdoso){
        Idoso idoso=idosoDAO.buscarPorId(idIdoso).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        List<Map<String,Object>> contatos=new ArrayList<>();
        for(RedeApoio r:redeDAO.listarPorIdoso(idIdoso))familiarDAO.buscarPorId(r.getIdFamiliar()).ifPresent(f->contatos.add(Map.of("nome",f.getNome(),"ordem",r.getOrdemContato()==null?0:r.getOrdemContato())));
        Consentimento c=consentimentoDAO.buscarPorTipo(idIdoso,TipoConsentimento.LOCALIZACAO).orElse(null);
        boolean local=c!=null&&c.getStatus()==StatusConsentimento.CONCEDIDO;
        return Map.of("contatos",contatos,"localizacaoConcedida",local,"mensagem","Pedido de emergencia simulada.","ligar","192 - SAMU");
    }
}
