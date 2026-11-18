package br.com.fiap.netinho.auth;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Garante que o hash do usuario de demonstracao no script SQL confere com a senha documentada (123456).
 */
class ScriptAcessoTest {
    @Test void hashDoScriptConfereComASenhaDeDemonstracao() throws Exception{
        try(InputStream in=getClass().getResourceAsStream("/db/05_netinho_acesso.sql")){
            assertNotNull(in,"script 05 nao encontrado no classpath");
            String sql=new String(in.readAllBytes(),StandardCharsets.UTF_8);
            Matcher m=Pattern.compile("'(pbkdf2\\$[^']+)'").matcher(sql);
            assertTrue(m.find(),"hash nao encontrado no script");
            assertTrue(SenhaHasher.confere("123456",m.group(1)));
        }
    }
}
