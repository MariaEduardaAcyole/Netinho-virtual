package br.com.fiap.netinho.auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica hash de senha, emissao/validacao de token e o filtro que protege as rotas.
 */
class AuthTest {
    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres";

    @Test void hashConfereSoComASenhaCerta(){
        String hash=SenhaHasher.gerar("123456");
        assertTrue(hash.startsWith("pbkdf2$"));
        assertTrue(SenhaHasher.confere("123456",hash));
        assertFalse(SenhaHasher.confere("1234567",hash));
        assertFalse(SenhaHasher.confere("123456","texto-qualquer"));
        assertNotEquals(hash,SenhaHasher.gerar("123456"),"cada hash usa um salt novo");
    }

    @Test void tokenValidoExpiradoEAdulterado(){
        Clock agora=Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"),ZoneOffset.UTC);
        TokenService t=new TokenService(SEGREDO,Duration.ofHours(1),agora);
        String token=t.emitir(7L);
        assertEquals(7L,t.validar(token).orElseThrow().idIdoso());
        String[] p=token.split("\\.");
        assertTrue(t.validar("8."+p[1]+"."+p[2]).isEmpty(),"trocar o id invalida a assinatura");
        TokenService depois=new TokenService(SEGREDO,Duration.ofHours(1),Clock.offset(agora,Duration.ofHours(2)));
        assertTrue(depois.validar(token).isEmpty(),"token expirado");
        TokenService outroSegredo=new TokenService("outro-segredo-tambem-com-mais-de-32-chars",Duration.ofHours(1),agora);
        assertTrue(outroSegredo.validar(token).isEmpty());
        assertTrue(t.validar(null).isEmpty());
        assertTrue(t.validar("lixo").isEmpty());
    }

    private static final String ADMIN = "token-de-administracao-com-mais-de-32-chars";

    @Test void filtroExigeTokenEProtegeDadosDeOutroIdoso() throws Exception{
        TokenService t=new TokenService(SEGREDO,Duration.ofHours(1),Clock.systemUTC());
        AuthFilter f=new AuthFilter(t,true,ADMIN);
        String token=t.emitir(1L);

        assertEquals(401,executar(f,"GET","/idosos/1",null).getStatus());
        assertEquals(200,executar(f,"GET","/idosos/1",token).getStatus());
        assertEquals(200,executar(f,"GET","/idosos/1/lembretes",token).getStatus());
        assertEquals(200,executar(f,"PUT","/idosos/1/modo-sem-leitura",token).getStatus());
        assertEquals(403,executar(f,"GET","/idosos/2",token).getStatus());
        assertEquals(403,executar(f,"GET","/idosos/2/historico",token).getStatus());
        assertEquals(200,executar(f,"POST","/auth/login",null).getStatus(),"login e livre");
        assertEquals(200,executar(f,"GET","/saude",null).getStatus(),"saude e livre");
        assertEquals(200,executar(f,"OPTIONS","/idosos/1",null).getStatus(),"preflight do CORS e livre");
        assertEquals(200,executar(new AuthFilter(t,false,""),"GET","/idosos/2",null).getStatus(),"desligado nao bloqueia");
    }

    @Test void contaDoIdosoSoAlcancaAsRotasDoApp() throws Exception{
        TokenService t=new TokenService(SEGREDO,Duration.ofHours(1),Clock.systemUTC());
        AuthFilter f=new AuthFilter(t,true,ADMIN);
        String token=t.emitir(1L);
        String[][] liberadas={{"GET","/guias"},{"GET","/guias/3/passos"},{"GET","/familiares/2"},{"POST","/tarefas"},
                {"GET","/tarefas/9/passo-atual"},{"PUT","/tarefas/9/confirmar"},{"PUT","/tarefas/9/avancar"},
                {"POST","/golpes/verificar"},{"POST","/golpes/verificar-imagem"},{"POST","/atendimentos"},
                {"POST","/emergencias"},{"PUT","/lembretes/5"}};
        for(String[] r:liberadas)assertEquals(200,executar(f,r[0],r[1],token).getStatus(),r[0]+" "+r[1]);
        String[][] administracao={{"GET","/idosos"},{"POST","/idosos"},{"PUT","/idosos/1"},{"DELETE","/idosos/1"},
                {"GET","/lembretes"},{"POST","/lembretes"},{"DELETE","/lembretes/5"},{"DELETE","/tarefas/9"},
                {"POST","/guias"},{"GET","/voluntarios"},{"PUT","/familiares/2"},{"POST","/idosos/1/rede-apoio"}};
        for(String[] r:administracao)assertEquals(403,executar(f,r[0],r[1],token).getStatus(),r[0]+" "+r[1]);
    }

    @Test void tokenDeAdministracaoLiberaTudoESemTokenAdminFicaFechado() throws Exception{
        TokenService t=new TokenService(SEGREDO,Duration.ofHours(1),Clock.systemUTC());
        AuthFilter f=new AuthFilter(t,true,ADMIN);
        assertEquals(200,executar(f,"GET","/idosos",ADMIN).getStatus());
        assertEquals(200,executar(f,"DELETE","/idosos/2",ADMIN).getStatus());
        assertEquals(401,executar(f,"GET","/idosos",ADMIN+"x").getStatus());
        AuthFilter semAdmin=new AuthFilter(t,true,"");
        assertEquals(401,executar(semAdmin,"GET","/idosos","").getStatus());
        AuthFilter adminCurto=new AuthFilter(t,true,"curto");
        assertEquals(401,executar(adminCurto,"GET","/idosos","curto").getStatus(),"token admin curto e ignorado");
    }

    private MockHttpServletResponse executar(AuthFilter f,String metodo,String caminho,String token) throws Exception{
        MockHttpServletRequest req=new MockHttpServletRequest(metodo,caminho);
        if(token!=null)req.addHeader("Authorization","Bearer "+token);
        MockHttpServletResponse res=new MockHttpServletResponse();
        f.doFilter(req,res,new MockFilterChain());
        return res;
    }
}
