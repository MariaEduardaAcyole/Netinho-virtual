package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.RegraNegocioException;
import br.com.fiap.netinho.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifica o "E golpe?" com imagem: combinacao com as regras de texto, falta de analise e validacoes.
 */
class GolpeImagemBOTest {
    private AlertaGolpeDAO alertaDAO;
    private AnaliseImagem analise;
    private GolpeImagemBO bo;

    @BeforeEach void setup(){
        alertaDAO=mock(AlertaGolpeDAO.class);IdosoDAO idosoDAO=mock(IdosoDAO.class);
        RedeApoioDAO redeDAO=mock(RedeApoioDAO.class);
        Idoso i=new Idoso();i.setId(1L);
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));
        when(alertaDAO.inserir(any())).thenReturn(10L);
        when(redeDAO.listarPorIdoso(1L)).thenReturn(List.of());
        analise=mock(AnaliseImagem.class);
        GolpeBO golpe=new GolpeBO(alertaDAO,idosoDAO,mock(ConsentimentoDAO.class),redeDAO,mock(FamiliarDAO.class));
        bo=new GolpeImagemBO(golpe,analise);
    }

    @Test void usaAAnaliseDaImagemERegistraAlertaDeImagem(){
        when(analise.analisar(eq("QUJD"),eq("image/png"),anyString())).thenReturn(Optional.of(new AnaliseImagem.Resultado(
                TipoRiscoAlerta.ALTO,"Pede Pix com pressa.","Nao envie dinheiro.","Mae, troquei de numero")));
        Map<String,Object> r=bo.verificar(1L,"chegou agora","data:image/png;base64,QUJD","image/png");
        assertEquals("ALTO",r.get("nivel"));assertEquals("Pede Pix com pressa.",r.get("motivo"));
        assertEquals("Nao envie dinheiro.",r.get("oQueFazer"));assertEquals(true,r.get("imagemAnalisada"));
        ArgumentCaptor<AlertaGolpe> salvo=ArgumentCaptor.forClass(AlertaGolpe.class);
        verify(alertaDAO).inserir(salvo.capture());
        assertEquals(TipoConteudoAlerta.IMAGEM,salvo.getValue().getConteudo());
        assertEquals("chegou agora | Mae, troquei de numero",salvo.getValue().getMensagem());
    }

    @Test void regraDeTextoMaisGraveVenceAAnalise(){
        when(analise.analisar(anyString(),anyString(),anyString())).thenReturn(Optional.of(new AnaliseImagem.Resultado(
                TipoRiscoAlerta.BAIXO,"Parece normal.","Tudo certo.","Me manda sua senha e o codigo do SMS agora, e urgente")));
        assertEquals("ALTO",bo.verificar(1L,"","QUJD","image/jpeg").get("nivel"));
    }

    @Test void semAnaliseDisponivelRespondeMedioComCautela(){
        when(analise.analisar(anyString(),anyString(),anyString())).thenReturn(Optional.empty());
        Map<String,Object> r=bo.verificar(1L,null,"QUJD",null);
        assertEquals("MEDIO",r.get("nivel"));assertEquals(false,r.get("imagemAnalisada"));
    }

    @Test void validaTipoETamanhoDaImagem(){
        assertThrows(RegraNegocioException.class,()->bo.verificar(1L,"","QUJD","application/pdf"));
        assertThrows(RegraNegocioException.class,()->bo.verificar(1L,"","","image/png"));
        assertThrows(RegraNegocioException.class,()->bo.verificar(1L,"","A".repeat(GolpeImagemBO.MAX_BASE64+1),"image/png"));
        verifyNoInteractions(analise);
    }
}
