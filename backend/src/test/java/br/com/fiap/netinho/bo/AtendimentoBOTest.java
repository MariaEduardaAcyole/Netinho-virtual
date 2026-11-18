package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.RegraNegocioException;
import br.com.fiap.netinho.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Verifica as regras de negocio e transicoes validas dos atendimentos.
 */
@ExtendWith(MockitoExtension.class)
class AtendimentoBOTest {
    @Mock AtendimentoDAO atendimentoDAO;@Mock IdosoDAO idosoDAO;@Mock TarefaDAO tarefaDAO;
    @Mock PassoDAO passoDAO;@Mock RedeApoioDAO redeDAO;@Mock FamiliarDAO familiarDAO;@Mock VoluntarioDAO voluntarioDAO;
    private AtendimentoBO bo(){return new AtendimentoBO(atendimentoDAO,idosoDAO,tarefaDAO,passoDAO,redeDAO,familiarDAO,voluntarioDAO);}
    @Test void passoExigeTarefa(){
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(new Idoso()));
        assertThrows(RegraNegocioException.class,()->bo().criar(1L,null,10L,0));
    }
    @Test void passoDeOutroGuiaFalha(){
        Idoso i=new Idoso();i.setId(1L);Tarefa t=new Tarefa();t.setIdGuia(2);Passo p=new Passo();p.setIdGuia(3);
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));when(tarefaDAO.buscarPorId(2L)).thenReturn(Optional.of(t));when(passoDAO.buscarPorId(10L)).thenReturn(Optional.of(p));
        assertThrows(RegraNegocioException.class,()->bo().criar(1L,2L,10L,0));
    }
    @Test void familiarEVoluntarioAoMesmoTempoFalha(){
        Atendimento a=new Atendimento();a.setId(1L);a.setIdIdoso(1L);
        when(atendimentoDAO.buscarPorId(1L)).thenReturn(Optional.of(a));
        assertThrows(RegraNegocioException.class,()->bo().iniciar(1L,2L,3L));
    }
    @Test void voluntarioBloqueadoNaoRecebeIndicacao(){
        Idoso i=new Idoso();i.setAceitaVoluntario(true);i.setId(1L);
        Voluntario v=new Voluntario();v.setId(1L);v.setStatus(StatusVoluntario.BLOQUEADO);
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));when(redeDAO.listarPorIdoso(1L)).thenReturn(List.of());when(voluntarioDAO.aprovados()).thenReturn(List.of());
        java.util.Map<String,Object> r=bo().criar(1L,null,null,0);
        assertFalse(r.containsKey("indicacao"));
    }
    @Test void relatorioSomaConcluidos(){
        Voluntario v=new Voluntario();v.setId(1L);v.setNome("Lucas");
        Atendimento a=new Atendimento();a.setStatus(StatusAtendimento.CONCLUIDO);a.setDataInicio(LocalDateTime.now().minusMinutes(30));a.setDataFim(LocalDateTime.now());
        when(voluntarioDAO.buscarPorId(1L)).thenReturn(Optional.of(v));when(atendimentoDAO.concluidosPorVoluntario(1L)).thenReturn(List.of(a));
        java.util.Map<String,Object> r=bo().horas(1L);assertEquals(30L,r.get("totalMinutos"));
    }
}
