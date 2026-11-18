package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.RegraNegocioException;
import br.com.fiap.netinho.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifica a classificacao de risco e o registro de alertas de golpe.
 */
@ExtendWith(MockitoExtension.class)
class GolpeBOTest {
    @Mock AlertaGolpeDAO alertaDAO;@Mock IdosoDAO idosoDAO;@Mock ConsentimentoDAO consentimentoDAO;
    @Mock RedeApoioDAO redeDAO;@Mock FamiliarDAO familiarDAO;
    private GolpeBO bo(){return new GolpeBO(alertaDAO,idosoDAO,consentimentoDAO,redeDAO,familiarDAO);}
    @Test void classificacaoAltoELow(){
        Idoso i=new Idoso();i.setId(1L);i.setSemLeitura(false);
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));when(alertaDAO.inserir(any())).thenReturn(100L);
        java.util.Map<String,Object> alto=bo().verificar(1L,"Mae, troquei de numero. Me manda um Pix de R$ 800 agora, e urgente",false,TipoOrigemAlerta.MANUAL);
        java.util.Map<String,Object> baixo=bo().verificar(1L,"Vamos almocar domingo?",false,TipoOrigemAlerta.MANUAL);
        assertEquals("ALTO",alto.get("nivel"));assertEquals("BAIXO",baixo.get("nivel"));
    }
    @Test void alertaAutomaticoExigeConsentimentoConcedido(){
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(new Idoso()));
        when(consentimentoDAO.buscarPorTipo(1L,TipoConsentimento.LEITURA_NOTIFICACAO)).thenReturn(Optional.empty());
        assertThrows(RegraNegocioException.class,()->bo().verificar(1L,"Mensagem suspeita",false,TipoOrigemAlerta.AUTOMATICO));
        Consentimento revogado=new Consentimento();revogado.setStatus(StatusConsentimento.REVOGADO);
        when(consentimentoDAO.buscarPorTipo(1L,TipoConsentimento.LEITURA_NOTIFICACAO)).thenReturn(Optional.of(revogado));
        assertThrows(RegraNegocioException.class,()->bo().verificar(1L,"Mensagem suspeita",false,TipoOrigemAlerta.AUTOMATICO));
    }
}
