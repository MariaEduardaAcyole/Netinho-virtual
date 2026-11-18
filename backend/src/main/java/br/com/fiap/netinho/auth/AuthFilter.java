package br.com.fiap.netinho.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controla o acesso a API.
 * <ul>
 *   <li>Livre: POST /auth/login, GET /saude e o preflight OPTIONS do CORS.</li>
 *   <li>Token de administracao (ADMIN_TOKEN): todas as rotas (cadastros e CRUDs).</li>
 *   <li>Token do idoso (login do app): so as rotas que o app usa, e /idosos/{id} so do proprio idoso.
 *       A conferencia do dono de tarefas, lembretes e familiares fica nos controllers (classe Sessao).</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);
    public static final String ATRIBUTO_ID_IDOSO = "netinho.idIdoso";
    private static final Pattern ROTA_IDOSO = Pattern.compile("^/idosos/(\\d+)(/.*)?$");

    private record Rota(String metodo, Pattern caminho) {
        boolean aceita(String m, String c) { return metodo.equals(m) && caminho.matcher(c).matches(); }
    }

    // Rotas que o app do idoso usa (fora de /idosos/{id}).
    private static final List<Rota> ROTAS_DO_APP = List.of(
            new Rota("GET", Pattern.compile("^/familiares/\\d+$")),
            new Rota("GET", Pattern.compile("^/guias(/\\d+(/passos)?)?$")),
            new Rota("POST", Pattern.compile("^/tarefas$")),
            new Rota("GET", Pattern.compile("^/tarefas/\\d+(/passo-atual)?$")),
            new Rota("PUT", Pattern.compile("^/tarefas/\\d+/(confirmar|desfazer|avancar)$")),
            new Rota("POST", Pattern.compile("^/golpes/(verificar|verificar-imagem|notificacao-recebida)$")),
            new Rota("POST", Pattern.compile("^/atendimentos$")),
            new Rota("POST", Pattern.compile("^/emergencias$")),
            new Rota("GET", Pattern.compile("^/lembretes/\\d+$")),
            new Rota("PUT", Pattern.compile("^/lembretes/\\d+$")));

    private final TokenService tokens;
    private final boolean ativo;
    private final byte[] tokenAdmin;

    public AuthFilter(TokenService tokens,
                      @Value("${app.auth.enabled:true}") boolean ativo,
                      @Value("${app.auth.admin-token:}") String tokenAdmin) {
        this.tokens = tokens;
        this.ativo = ativo;
        if (tokenAdmin != null && !tokenAdmin.isBlank() && tokenAdmin.length() < 32) {
            LOG.warn("ADMIN_TOKEN com menos de 32 caracteres foi ignorado: as rotas de administracao ficam fechadas.");
            this.tokenAdmin = null;
        } else {
            this.tokenAdmin = tokenAdmin == null || tokenAdmin.isBlank() ? null : tokenAdmin.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String caminho = request.getRequestURI();
        return !ativo
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || caminho.equals("/auth/login")
                || caminho.equals("/saude");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        String token = cabecalho != null && cabecalho.startsWith("Bearer ") ? cabecalho.substring(7).trim() : null;

        if (token != null && tokenAdmin != null
                && MessageDigest.isEqual(tokenAdmin, token.getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(request, response); // administracao: acesso completo, sem idoso na sessao
            return;
        }

        Optional<TokenService.Sessao> sessao = tokens.validar(token);
        if (sessao.isEmpty()) {
            erro(response, request, 401, "Nao autenticado", "Entre de novo para continuar.");
            return;
        }
        Long idIdoso = sessao.get().idIdoso();
        String metodo = request.getMethod().toUpperCase();
        String caminho = request.getRequestURI();

        Matcher rota = ROTA_IDOSO.matcher(caminho);
        boolean permitido;
        if (rota.matches()) {
            boolean proprio = rota.group(1).equals(String.valueOf(idIdoso));
            if (!proprio) {
                erro(response, request, 403, "Proibido", "Esta conta nao pode ver os dados de outra pessoa.");
                return;
            }
            // Nos proprios dados: leitura livre; alteracao so do modo sem leitura (o resto e feito pela familia).
            permitido = metodo.equals("GET") || (metodo.equals("PUT") && "/modo-sem-leitura".equals(rota.group(2)));
        } else {
            permitido = ROTAS_DO_APP.stream().anyMatch(r -> r.aceita(metodo, caminho));
        }
        if (!permitido) {
            erro(response, request, 403, "Proibido", "Esta rota e so para administracao.");
            return;
        }
        request.setAttribute(ATRIBUTO_ID_IDOSO, idIdoso);
        chain.doFilter(request, response);
    }

    private void erro(HttpServletResponse response, HttpServletRequest request, int status, String erro, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        if (status == 401) response.setHeader("WWW-Authenticate", "Bearer");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":" + status
                + ",\"erro\":\"" + erro + "\",\"mensagem\":\"" + mensagem + "\",\"caminho\":\""
                + request.getRequestURI().replace("\"", "").replace("\\", "") + "\"}");
    }
}
