package br.com.fiap.netinho.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Emite e valida tokens de sessao assinados com HMAC-SHA256.
 * Formato: idIdoso.expiraEmSegundos.assinaturaBase64Url. Nao guarda estado no servidor.
 */
@Service
public class TokenService {
    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);
    private final byte[] segredo;
    private final Duration validade;
    private final Clock clock;

    @Autowired
    public TokenService(@Value("${app.auth.secret:}") String segredo,
                        @Value("${app.auth.validade-horas:720}") long validadeHoras) {
        this(segredo, Duration.ofHours(validadeHoras), Clock.systemUTC());
    }

    /** Usado nos testes, para controlar o relogio. */
    public TokenService(String segredo, Duration validade, Clock clock) {
        if (segredo == null || segredo.length() < 32) {
            byte[] aleatorio = new byte[32];
            new SecureRandom().nextBytes(aleatorio);
            this.segredo = aleatorio;
            LOG.warn("AUTH_SECRET ausente ou curto (minimo 32 caracteres): usando um segredo temporario. As sessoes caem quando a API reiniciar.");
        } else {
            this.segredo = segredo.getBytes(StandardCharsets.UTF_8);
        }
        this.validade = validade;
        this.clock = clock;
    }

    public record Sessao(Long idIdoso, Instant expiraEm) {}

    public String emitir(Long idIdoso) {
        long expira = Instant.now(clock).plus(validade).getEpochSecond();
        String corpo = idIdoso + "." + expira;
        return corpo + "." + assinar(corpo);
    }

    public Instant expiracao(String token) {
        return validar(token).map(Sessao::expiraEm).orElse(null);
    }

    public Optional<Sessao> validar(String token) {
        if (token == null) return Optional.empty();
        String[] partes = token.split("\\.");
        if (partes.length != 3) return Optional.empty();
        String corpo = partes[0] + "." + partes[1];
        byte[] esperado = assinar(corpo).getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(esperado, partes[2].getBytes(StandardCharsets.US_ASCII))) return Optional.empty();
        try {
            Long idIdoso = Long.valueOf(partes[0]);
            Instant expira = Instant.ofEpochSecond(Long.parseLong(partes[1]));
            if (!expira.isAfter(Instant.now(clock))) return Optional.empty();
            return Optional.of(new Sessao(idIdoso, expira));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String assinar(String corpo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 indisponivel no Java.", e);
        }
    }
}
