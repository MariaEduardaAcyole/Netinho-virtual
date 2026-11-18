package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.auth.SenhaHasher;
import br.com.fiap.netinho.auth.TokenService;
import br.com.fiap.netinho.dao.AcessoDAO;
import br.com.fiap.netinho.dao.IdosoDAO;
import br.com.fiap.netinho.exception.NaoAutenticadoException;
import br.com.fiap.netinho.model.Idoso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Confere usuario e senha, limita tentativas erradas e emite o token de sessao.
 */
@Service
public class AuthBO {
    static final int MAX_TENTATIVAS = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(5);
    // Hash fixo usado quando o usuario nao existe, para a resposta levar o mesmo tempo.
    private static final String HASH_FALSO = SenhaHasher.gerar("netinho-usuario-inexistente");

    private final AcessoDAO acessoDAO;
    private final IdosoDAO idosoDAO;
    private final TokenService tokens;
    private final Clock clock;
    private final Map<String, Tentativas> tentativas = new ConcurrentHashMap<>();

    private record Tentativas(int erros, Instant bloqueadoAte) {}

    @Autowired
    public AuthBO(AcessoDAO acessoDAO, IdosoDAO idosoDAO, TokenService tokens) {
        this(acessoDAO, idosoDAO, tokens, Clock.systemUTC());
    }

    AuthBO(AcessoDAO acessoDAO, IdosoDAO idosoDAO, TokenService tokens, Clock clock) {
        this.acessoDAO = acessoDAO; this.idosoDAO = idosoDAO; this.tokens = tokens; this.clock = clock;
    }

    public Map<String, Object> entrar(String usuario, String senha) {
        String chave = usuario == null ? "" : usuario.trim().toLowerCase(Locale.ROOT);
        Instant agora = Instant.now(clock);
        Tentativas t = tentativas.get(chave);
        if (t != null && t.bloqueadoAte() != null) {
            if (t.bloqueadoAte().isAfter(agora))
                throw new NaoAutenticadoException("Muitas tentativas erradas. Espere alguns minutos e tente de novo.");
            t = null; // o bloqueio acabou: recomeca a contagem
        }
        if (chave.isEmpty() || senha == null || senha.isEmpty()) {
            throw new NaoAutenticadoException("Informe usuario e senha.");
        }
        AcessoDAO.Acesso acesso = acessoDAO.buscarPorUsuario(chave).orElse(null);
        boolean confere = SenhaHasher.confere(senha, acesso == null ? HASH_FALSO : acesso.senhaHash()) && acesso != null;
        if (!confere) {
            int erros = (t == null ? 0 : t.erros()) + 1;
            tentativas.put(chave, new Tentativas(erros, erros >= MAX_TENTATIVAS ? agora.plus(BLOQUEIO) : null));
            throw new NaoAutenticadoException("Usuario ou senha incorretos.");
        }
        tentativas.remove(chave);
        acessoDAO.registrarEntrada(acesso.id());
        Idoso idoso = idosoDAO.buscarPorId(acesso.idIdoso())
                .orElseThrow(() -> new NaoAutenticadoException("Cadastro do idoso nao encontrado."));
        String token = tokens.emitir(idoso.getId());
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("token", token);
        r.put("idIdoso", idoso.getId());
        r.put("nome", idoso.getNome());
        r.put("expiraEm", String.valueOf(tokens.expiracao(token)));
        return r;
    }
}
