package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.RegraNegocioException;
import br.com.fiap.netinho.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifica a conversa do Netinho: emergencia sem IA, conferencia da resposta do modelo e plano B.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConversaBOTest {
    @Mock AssistenteIA ia; @Mock IdosoDAO idosoDAO; @Mock RedeApoioDAO redeDAO; @Mock FamiliarDAO familiarDAO;
    @Mock LembreteDAO lembreteDAO; @Mock GuiaDAO guiaDAO;
    ConversaBO bo;

    @BeforeEach void dados(){
        bo=new ConversaBO(ia,idosoDAO,redeDAO,familiarDAO,lembreteDAO,guiaDAO);
        Idoso i=new Idoso();i.setId(1L);i.setNome("Maria Helena Silva");
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));
        RedeApoio r=new RedeApoio();r.setIdFamiliar(1L);r.setParentesco("filho");
        Familiar f=new Familiar();f.setId(1L);f.setNome("Rafael Silva");
        when(redeDAO.listarPorIdoso(1L)).thenReturn(List.of(r));
        when(familiarDAO.buscarPorId(1L)).thenReturn(Optional.of(f));
        Lembrete l=new Lembrete();l.setDescricao("Consulta com clinico");l.setDataAgendamento(LocalDateTime.of(2027,3,25,14,0));l.setStatus(StatusLembrete.PENDENTE);
        when(lembreteDAO.listarPorIdoso(1L)).thenReturn(List.of(l));
        Guia g=new Guia();g.setId(2);g.setNome("Consultar extrato");g.setStatus(StatusGuia.ATIVO);
        when(guiaDAO.ativos()).thenReturn(List.of(g));
        when(ia.disponivel()).thenReturn(true);
    }

    @Test void emergenciaNaoEsperaAIa(){
        Map<String,Object> r=bo.conversar(1L,"Socorro, estou passando mal",List.of());
        assertEquals("EMERGENCIA",r.get("acao"));
        verify(ia,never()).responder(anyString(),anyList());
    }
    @Test void mandaContextoParaAIaEUsaARespostaDela(){
        when(ia.responder(anyString(),anyList())).thenReturn(Optional.of("```json\n{\"resposta\":\"Sua consulta e dia 25 as 14h.\",\"acao\":\"LEMBRETES\",\"idGuia\":null,\"pedido\":null}\n```"));
        Map<String,Object> r=bo.conversar(1L,"que horas e minha consulta?",List.of(new AssistenteIA.Mensagem("assistant","Oi!")));
        assertEquals("LEMBRETES",r.get("acao"));assertEquals(true,r.get("ia"));
        verify(ia).responder(argThat(s->s.contains("Rafael (filho)")&&s.contains("Consulta com clinico")&&s.contains("id 2: Consultar extrato")),
                argThat(l->l.size()==2&&l.get(1).papel().equals("user")));
    }
    @Test void guiaInventadoPeloModeloEDescartado(){
        when(ia.responder(anyString(),anyList())).thenReturn(Optional.of("{\"resposta\":\"Te ensino.\",\"acao\":\"ME_ENSINA\",\"idGuia\":99}"));
        Map<String,Object> r=bo.conversar(1L,"me ensina",List.of());
        assertEquals("ME_ENSINA",r.get("acao"));assertNull(r.get("idGuia"));
    }
    @Test void modeloNuncaTransformaDinheiroEmTarefa(){
        when(ia.responder(anyString(),anyList())).thenReturn(Optional.of("{\"resposta\":\"Claro!\",\"acao\":\"FAZ_PRA_MIM\",\"pedido\":\"Manda 50 reais pro Rafael\"}"));
        Map<String,Object> r=bo.conversar(1L,"manda um recado pro Rafael",List.of());
        assertEquals("GOLPE",r.get("acao"));assertNull(r.get("pedido"));
    }
    @Test void semIaOuComFalhaUsaPalavrasChave(){
        when(ia.responder(anyString(),anyList())).thenReturn(Optional.empty());
        assertEquals("GOLPE",bo.conversar(1L,"recebi uma mensagem estranha",List.of()).get("acao"));
        when(ia.disponivel()).thenReturn(false);
        Map<String,Object> r=bo.conversar(1L,"manda mensagem pro Rafael dizendo oi",List.of());
        assertEquals("FAZ_PRA_MIM",r.get("acao"));assertEquals(false,r.get("ia"));
        assertEquals("NENHUMA",bo.conversar(1L,"bom dia",List.of()).get("acao"));
    }
    @Test void respostaForaDoFormatoCaiNoPlanoB(){
        when(ia.responder(anyString(),anyList())).thenReturn(Optional.of("Oi, tudo bem?"));
        assertEquals(false,bo.conversar(1L,"bom dia",List.of()).get("ia"));
    }
    @Test void dinheiroERespondidoNaHoraSemIa(){
        Map<String,Object> r=bo.conversar(1L,"faz um pix de 100 reais pro meu filho",List.of());
        assertEquals("AJUDA",r.get("acao"));assertTrue(r.get("resposta").toString().contains("não faço Pix"));
        assertEquals("GOLPE",bo.conversar(1L,"recebi uma mensagem pedindo um pix urgente",List.of()).get("acao"));
        verify(ia,never()).responder(anyString(),anyList());
    }
    @Test void textoVazioERecusado(){
        assertThrows(RegraNegocioException.class,()->bo.conversar(1L,"  ",List.of()));
    }
}
