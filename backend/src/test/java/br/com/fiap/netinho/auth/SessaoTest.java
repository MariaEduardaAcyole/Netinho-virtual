package br.com.fiap.netinho.auth;

import br.com.fiap.netinho.exception.AcessoNegadoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica que a conta do idoso so age sobre os proprios dados.
 */
class SessaoTest {
    @AfterEach void limpar(){RequestContextHolder.resetRequestAttributes();}

    private void sessaoDo(Long idIdoso){
        MockHttpServletRequest req=new MockHttpServletRequest();
        if(idIdoso!=null)req.setAttribute(AuthFilter.ATRIBUTO_ID_IDOSO,idIdoso);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
    }

    @Test void idosoUsaSempreOProprioId(){
        sessaoDo(1L);
        assertEquals(1L,Sessao.resolver(1L));
        assertEquals(1L,Sessao.resolver(null),"sem id no corpo, usa o da sessao");
        assertThrows(AcessoNegadoException.class,()->Sessao.resolver(2L));
        Sessao.conferirDono(1L);
        assertThrows(AcessoNegadoException.class,()->Sessao.conferirDono(2L));
    }

    @Test void administracaoOuLoginDesligadoNaoRestringe(){
        sessaoDo(null);
        assertNull(Sessao.idoso());
        assertEquals(2L,Sessao.resolver(2L));
        Sessao.conferirDono(2L);
        RequestContextHolder.resetRequestAttributes();
        assertEquals(3L,Sessao.resolver(3L));
    }
}
