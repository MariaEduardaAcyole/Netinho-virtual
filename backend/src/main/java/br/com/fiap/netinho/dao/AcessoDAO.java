package br.com.fiap.netinho.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Le e atualiza as credenciais de acesso (tabela ACESSO) usadas no login.
 */
@Repository
public class AcessoDAO {
    private final JdbcTemplate jdbc;
    public AcessoDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Linha da tabela ACESSO necessaria para conferir a senha. */
    public record Acesso(Long id, Long idIdoso, String usuario, String senhaHash) {}

    public Optional<Acesso> buscarPorUsuario(String usuario) {
        return jdbc.query("SELECT id_ace,id_ido,nm_ace_usuario,ds_ace_senha_hash FROM ACESSO WHERE nm_ace_usuario=?",
                (rs, i) -> new Acesso(DaoUtils.nullableLong(rs, "id_ace"), DaoUtils.nullableLong(rs, "id_ido"),
                        rs.getString("nm_ace_usuario"), rs.getString("ds_ace_senha_hash")),
                usuario).stream().findFirst();
    }

    public void registrarEntrada(Long id) {
        jdbc.update("UPDATE ACESSO SET dh_ace_ultimo_acesso=? WHERE id_ace=?", Timestamp.valueOf(LocalDateTime.now()), id);
    }
}
