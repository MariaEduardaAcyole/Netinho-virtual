package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.*;
import br.com.fiap.netinho.model.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Aplica regras de negocio de atendimento, incluindo validacao do inicio, conclusao e horas de voluntarios.
 */
@Service
public class AtendimentoBO {
    private final AtendimentoDAO atendimentoDAO;
    private final IdosoDAO idosoDAO;
    private final TarefaDAO tarefaDAO;
    private final PassoDAO passoDAO;
    private final RedeApoioDAO redeDAO;
    private final FamiliarDAO familiarDAO;
    private final VoluntarioDAO voluntarioDAO;

    public AtendimentoBO(AtendimentoDAO atendimentoDAO,IdosoDAO idosoDAO,TarefaDAO tarefaDAO,PassoDAO passoDAO,RedeApoioDAO redeDAO,FamiliarDAO familiarDAO,VoluntarioDAO voluntarioDAO){
        this.atendimentoDAO=atendimentoDAO;this.idosoDAO=idosoDAO;this.tarefaDAO=tarefaDAO;this.passoDAO=passoDAO;this.redeDAO=redeDAO;this.familiarDAO=familiarDAO;this.voluntarioDAO=voluntarioDAO;
    }
    public Map<String,Object> criar(Long idIdoso,Long idTarefa,Long idPasso,Integer tentativas){
        Idoso idoso=idosoDAO.buscarPorId(idIdoso).orElseThrow(()->new RecursoNaoEncontradoException("Idoso nao encontrado."));
        if(idPasso!=null){
            if(idTarefa==null) throw new RegraNegocioException("Passo exige uma tarefa.");
            Tarefa tarefa=tarefaDAO.buscarPorId(idTarefa).orElseThrow(()->new RecursoNaoEncontradoException("Tarefa nao encontrada."));
            Passo passo=passoDAO.buscarPorId(idPasso).orElseThrow(()->new RecursoNaoEncontradoException("Passo nao encontrado."));
            if(tarefa.getIdGuia()==null||!tarefa.getIdGuia().equals(passo.getIdGuia())) throw new RegraNegocioException("O passo nao pertence ao guia da tarefa.");
        }
        Atendimento x=new Atendimento();x.setIdIdoso(idIdoso);x.setIdTarefa(idTarefa);x.setIdPasso(idPasso);x.setStatus(StatusAtendimento.SOLICITADO);x.setTentativas(tentativas==null?0:tentativas);x.setDataSolicitacao(LocalDateTime.now());x.setId(atendimentoDAO.inserir(x));
        Map<String,Object> r=new LinkedHashMap<>();r.put("id",x.getId());r.put("status",x.getStatus().name());
        Optional<RedeApoio> familiar=redeDAO.listarPorIdoso(idIdoso).stream().filter(RedeApoio::isAutorizado).min(Comparator.comparing(RedeApoio::getOrdemContato));
        if(familiar.isPresent())r.put("indicacao",familiarDAO.buscarPorId(familiar.get().getIdFamiliar()).map(Familiar::getNome).orElse("Familiar"));
        else if(idoso.isAceitaVoluntario()){
            voluntarioDAO.aprovados().stream().findFirst().ifPresent(v->r.put("indicacao",v.getNome()));
        }
        return r;
    }
    public void iniciar(Long id,Long idFamiliar,Long idVoluntario){
        Atendimento x=atendimentoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Atendimento nao encontrado."));
        if(idFamiliar!=null&&idVoluntario!=null) throw new RegraNegocioException("Informe somente familiar ou voluntario.");
        if(idFamiliar==null&&idVoluntario==null) throw new RegraNegocioException("Informe um familiar ou voluntario.");
        if(idFamiliar!=null){
            RedeApoio rede=redeDAO.buscar(x.getIdIdoso(),idFamiliar).orElseThrow(()->new RegraNegocioException("Familiar nao pertence a rede autorizada."));
            if(!rede.isAutorizado()) throw new RegraNegocioException("Familiar nao esta autorizado.");
        }else{
            Idoso idoso=idosoDAO.buscarPorId(x.getIdIdoso()).orElseThrow();
            if(!idoso.isAceitaVoluntario()) throw new RegraNegocioException("O idoso nao aceita voluntario.");
            Voluntario vol=voluntarioDAO.buscarPorId(idVoluntario).orElseThrow(()->new RecursoNaoEncontradoException("Voluntario nao encontrado."));
            if(vol.getStatus()!=StatusVoluntario.APROVADO) throw new RegraNegocioException("Voluntario nao esta aprovado.");
        }
        atendimentoDAO.iniciar(id,idFamiliar,idVoluntario,LocalDateTime.now());
    }
    public void concluir(Long id){
        Atendimento x=atendimentoDAO.buscarPorId(id).orElseThrow(()->new RecursoNaoEncontradoException("Atendimento nao encontrado."));
        if(x.getStatus()!=StatusAtendimento.EM_ANDAMENTO||x.getDataInicio()==null) throw new RegraNegocioException("Somente atendimento EM_ANDAMENTO pode ser concluido.");
        LocalDateTime fim=LocalDateTime.now();
        if(fim.isBefore(x.getDataInicio())) throw new RegraNegocioException("Fim nao pode ser anterior ao inicio.");
        atendimentoDAO.concluir(id,fim);
    }
    public Map<String,Object> horas(Long idVol){
        Voluntario v=voluntarioDAO.buscarPorId(idVol).orElseThrow(()->new RecursoNaoEncontradoException("Voluntario nao encontrado."));
        long minutos=0;
        for(Atendimento a:atendimentoDAO.concluidosPorVoluntario(idVol)){
            if(a.getDataInicio()!=null&&a.getDataFim()!=null&&a.getStatus()==StatusAtendimento.CONCLUIDO)
                minutos+=ChronoUnit.MINUTES.between(a.getDataInicio(),a.getDataFim());
        }
        return Map.of("id",idVol,"nome",v.getNome(),"totalMinutos",minutos,"totalHoras",String.format(Locale.ROOT,"%.2f",minutos/60.0));
    }
}
