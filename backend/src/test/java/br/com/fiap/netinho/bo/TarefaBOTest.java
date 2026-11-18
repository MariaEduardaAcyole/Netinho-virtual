package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.ConflitoException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifica a interpretacao dos pedidos e as transicoes das tarefas e guias.
 */
@ExtendWith(MockitoExtension.class)
class TarefaBOTest {
    @Mock TarefaDAO tarefaDAO; @Mock IdosoDAO idosoDAO; @Mock RedeApoioDAO redeDAO;
    @Mock FamiliarDAO familiarDAO; @Mock GuiaDAO guiaDAO; @Mock PassoDAO passoDAO;

    private TarefaBO bo(){return new TarefaBO(tarefaDAO,idosoDAO,redeDAO,familiarDAO,guiaDAO,passoDAO);}

    @Test void naoConfirmaForaDeAguardando(){
        Tarefa t=new Tarefa();t.setStatus(StatusTarefa.CONCLUIDA);
        when(tarefaDAO.buscarPorId(1L)).thenReturn(Optional.of(t));
        assertThrows(RegraNegocioException.class,()->bo().confirmar(1L,"SIM"));
    }
    @Test void recusaPagamentoPixOuSenha(){
        Idoso i=new Idoso();i.setId(1L);
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));when(tarefaDAO.inserir(any())).thenReturn(100L);
        assertThrows(RegraNegocioException.class,()->bo().criar(1L,"Quero fazer um Pix"));
        assertThrows(RegraNegocioException.class,()->bo().criar(1L,"Digite minha senha"));
    }
    @Test void detectaContatoAmbiguo(){
        Idoso i=new Idoso();i.setId(1L);
        RedeApoio r1=new RedeApoio();r1.setIdFamiliar(1L);RedeApoio r2=new RedeApoio();r2.setIdFamiliar(2L);
        Familiar f1=new Familiar();f1.setId(1L);f1.setNome("Rafael");
        Familiar f2=new Familiar();f2.setId(2L);f2.setNome("Rafael");
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));
        when(redeDAO.listarPorIdoso(1L)).thenReturn(List.of(r1,r2));
        when(familiarDAO.buscarPorId(1L)).thenReturn(Optional.of(f1));when(familiarDAO.buscarPorId(2L)).thenReturn(Optional.of(f2));
        assertThrows(ConflitoException.class,()->bo().criar(1L,"manda mensagem pra Rafael dizendo oi"));
    }
    @Test void desfazerDepoisDeCincoSegundosFalha(){
        Tarefa t=new Tarefa();t.setStatus(StatusTarefa.CONCLUIDA);t.setDataConclusao(LocalDateTime.now().minusSeconds(6));
        when(tarefaDAO.buscarPorId(1L)).thenReturn(Optional.of(t));
        assertThrows(RegraNegocioException.class,()->bo().desfazer(1L));
    }
    @Test void iniciaGuiaEscolhidoNoPrimeiroPasso(){
        Idoso i=new Idoso();i.setId(1L);
        Guia g=new Guia();g.setId(3);g.setNome("Agendar consulta");g.setStatus(StatusGuia.ATIVO);
        Passo p=new Passo();p.setIdGuia(3);p.setOrdem(1);
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));
        when(guiaDAO.buscarPorId(3)).thenReturn(Optional.of(g));
        when(passoDAO.listarPorGuia(3)).thenReturn(List.of(p));
        when(tarefaDAO.inserir(any())).thenReturn(50L);
        Tarefa t=bo().iniciarGuia(1L,3);
        assertEquals(Integer.valueOf(3),t.getIdGuia());assertEquals(TipoModoTarefa.ME_ENSINA,t.getModo());assertEquals(StatusTarefa.EM_ANDAMENTO,t.getStatus());
    }
    @Test void naoIniciaGuiaInativo(){
        Idoso i=new Idoso();i.setId(1L);
        Guia g=new Guia();g.setId(5);g.setStatus(StatusGuia.INATIVO);
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));
        when(guiaDAO.buscarPorId(5)).thenReturn(Optional.of(g));
        assertThrows(RegraNegocioException.class,()->bo().iniciarGuia(1L,5));
        verify(tarefaDAO,never()).inserir(any());
    }
    @Test void guiaNuncaPulaPassoEExigeConcluido(){
        assertThrows(RegraNegocioException.class,()->bo().avancar(1L,false));
        verify(tarefaDAO,never()).atualizarStatus(anyLong(),any(),any());
    }
}
