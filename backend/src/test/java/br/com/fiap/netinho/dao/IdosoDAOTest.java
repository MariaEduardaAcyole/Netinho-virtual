package br.com.fiap.netinho.dao;

import br.com.fiap.netinho.model.Idoso;
import br.com.fiap.netinho.model.TipoAparelho;
import br.com.fiap.netinho.model.TipoJeitoFalar;
import br.com.fiap.netinho.model.TipoVoz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_ORACLE_TESTS", matches = "true")
class IdosoDAOTest {
    @Autowired
    private IdosoDAO dao;

    @Test
    void deveBuscarIdosoDoBancoOracle() {
        assertTrue(dao.buscarPorId(1L).isPresent());
    }

    @Test
    void deveInserirAtualizarListarEExcluirIdosoNoOracle() {
        Idoso idoso = new Idoso();
        idoso.setNome("Teste Oracle");
        idoso.setDataNascimento(LocalDateTime.of(1960, 5, 10, 0, 0));
        idoso.setTelefone("9" + String.format("%010d", System.nanoTime() % 1_000_000_000L));
        idoso.setAparelho(TipoAparelho.ANDROID);
        idoso.setNomeAssistente("Netinho");
        idoso.setVoz(TipoVoz.MASCULINA);
        idoso.setJeitoFalar(TipoJeitoFalar.NORMAL);
        idoso.setSemLeitura(false);
        idoso.setAceitaVoluntario(true);
        idoso.setDataCadastro(LocalDateTime.now());

        Long id = dao.inserir(idoso);
        assertTrue(dao.buscarPorId(id).isPresent());

        idoso.setNome("Teste Oracle Atualizado");
        dao.atualizar(id, idoso);
        assertEquals("Teste Oracle Atualizado", dao.buscarPorId(id).orElseThrow().getNome());

        assertTrue(dao.listar().stream().anyMatch(item -> item.getId().equals(id)));
        assertEquals(1, dao.excluir(id));
        assertFalse(dao.buscarPorId(id).isPresent());
    }
}
