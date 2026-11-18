package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.auth.SenhaHasher;
import br.com.fiap.netinho.auth.TokenService;
import br.com.fiap.netinho.dao.AcessoDAO;
import br.com.fiap.netinho.dao.IdosoDAO;
import br.com.fiap.netinho.exception.NaoAutenticadoException;
import br.com.fiap.netinho.model.Idoso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifica o login: senha certa, senha errada, usuario inexistente e bloqueio por tentativas.
 */
class AuthBOTest {
    private static final String HASH = SenhaHasher.gerar("123456");
    private AcessoDAO acessoDAO;
    private IdosoDAO idosoDAO;
    private TokenService tokens;

    @BeforeEach void setup(){
        acessoDAO=mock(AcessoDAO.class);idosoDAO=mock(IdosoDAO.class);
        tokens=new TokenService("segredo-de-teste-com-mais-de-32-caracteres",Duration.ofHours(1),Clock.systemUTC());
        when(acessoDAO.buscarPorUsuario(anyString())).thenReturn(Optional.empty());
        when(acessoDAO.buscarPorUsuario("netinho")).thenReturn(Optional.of(new AcessoDAO.Acesso(1L,1L,"netinho",HASH)));
        Idoso i=new Idoso();i.setId(1L);i.setNome("Maria Helena Silva");
        when(idosoDAO.buscarPorId(1L)).thenReturn(Optional.of(i));
    }

    @Test void entraComSenhaCertaEIgnoraMaiusculasNoUsuario(){
        Map<String,Object> r=new AuthBO(acessoDAO,idosoDAO,tokens).entrar(" Netinho ","123456");
        assertEquals(1L,r.get("idIdoso"));assertEquals("Maria Helena Silva",r.get("nome"));
        assertEquals(1L,tokens.validar((String)r.get("token")).orElseThrow().idIdoso());
        verify(acessoDAO).registrarEntrada(1L);
    }

    @Test void recusaSenhaErradaEUsuarioInexistenteComAMesmaMensagem(){
        AuthBO bo=new AuthBO(acessoDAO,idosoDAO,tokens);
        NaoAutenticadoException a=assertThrows(NaoAutenticadoException.class,()->bo.entrar("netinho","errada"));
        NaoAutenticadoException b=assertThrows(NaoAutenticadoException.class,()->bo.entrar("fulano","123456"));
        assertEquals(a.getMessage(),b.getMessage());
        verify(acessoDAO,never()).registrarEntrada(any());
    }

    @Test void bloqueiaDepoisDeCincoErrosELiberaDepoisDoTempo(){
        Instant[] agora={Instant.parse("2026-11-18T12:00:00Z")};
        Clock relogio=new Clock(){
            public java.time.ZoneId getZone(){return ZoneOffset.UTC;}
            public Clock withZone(java.time.ZoneId z){return this;}
            public Instant instant(){return agora[0];}
        };
        AuthBO bo=new AuthBO(acessoDAO,idosoDAO,tokens,relogio);
        for(int i=0;i<AuthBO.MAX_TENTATIVAS;i++)assertThrows(NaoAutenticadoException.class,()->bo.entrar("netinho","errada"));
        NaoAutenticadoException bloqueado=assertThrows(NaoAutenticadoException.class,()->bo.entrar("netinho","123456"));
        assertTrue(bloqueado.getMessage().contains("Muitas tentativas"),"bloqueado mesmo com a senha certa");
        agora[0]=agora[0].plus(AuthBO.BLOQUEIO).plusSeconds(1);
        assertEquals(1L,bo.entrar("netinho","123456").get("idIdoso"),"liberado depois do tempo");
    }
}
