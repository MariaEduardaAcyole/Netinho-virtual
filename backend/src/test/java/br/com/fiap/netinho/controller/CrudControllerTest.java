package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.bo.CrudBO;
import br.com.fiap.netinho.exception.GlobalExceptionHandler;
import br.com.fiap.netinho.exception.RecursoNaoEncontradoException;
import br.com.fiap.netinho.model.Idoso;
import br.com.fiap.netinho.model.TipoAparelho;
import br.com.fiap.netinho.model.TipoJeitoFalar;
import br.com.fiap.netinho.model.TipoVoz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CrudControllerTest {
    private CrudBO bo;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        bo = mock(CrudBO.class);
        mvc = MockMvcBuilders.standaloneSetup(new CrudController(bo))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveListarIdososComStatus200() throws Exception {
        Idoso idoso = idosoValido();
        when(bo.listarIdosos()).thenReturn(List.of(idoso));

        mvc.perform(get("/idosos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Maria Teste"));
    }

    @Test
    void deveCriarIdosoComStatus201() throws Exception {
        when(bo.criarIdoso(org.mockito.ArgumentMatchers.any(Idoso.class))).thenReturn(42L);

        mvc.perform(post("/idosos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria Teste",
                                  "dataNascimento": "1958-04-12T00:00:00",
                                  "telefone": "11999990042",
                                  "aparelho": "ANDROID",
                                  "nomeAssistente": "Netinho",
                                  "voz": "FEMININA",
                                  "jeitoFalar": "CALMA",
                                  "semLeitura": true,
                                  "aceitaVoluntario": true,
                                  "dataCadastro": "2026-01-10T00:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/idosos/42"))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void deveAtualizarIdosoComStatus200() throws Exception {
        mvc.perform(put("/idosos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria Atualizada",
                                  "dataNascimento": "1958-04-12T00:00:00",
                                  "telefone": "11999990042",
                                  "aparelho": "ANDROID",
                                  "nomeAssistente": "Netinho",
                                  "voz": "FEMININA",
                                  "jeitoFalar": "NORMAL",
                                  "semLeitura": false,
                                  "aceitaVoluntario": true,
                                  "dataCadastro": "2026-01-10T00:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Atualizada"));

        verify(bo).atualizarIdoso(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(Idoso.class));
    }

    @Test
    void deveExcluirIdosoComStatus204() throws Exception {
        mvc.perform(delete("/idosos/1"))
                .andExpect(status().isNoContent());

        verify(bo).excluirIdoso(1L);
    }

    @Test
    void deveRetornar404QuandoIdosoNaoExiste() throws Exception {
        when(bo.buscarIdoso(999L))
                .thenThrow(new RecursoNaoEncontradoException("Idoso nao encontrado."));

        mvc.perform(get("/idosos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Idoso nao encontrado."));
    }

    private Idoso idosoValido() {
        Idoso idoso = new Idoso();
        idoso.setId(1L);
        idoso.setNome("Maria Teste");
        idoso.setDataNascimento(LocalDateTime.of(1958, 4, 12, 0, 0));
        idoso.setTelefone("11999990001");
        idoso.setAparelho(TipoAparelho.ANDROID);
        idoso.setNomeAssistente("Netinho");
        idoso.setVoz(TipoVoz.FEMININA);
        idoso.setJeitoFalar(TipoJeitoFalar.CALMA);
        idoso.setSemLeitura(true);
        idoso.setAceitaVoluntario(true);
        idoso.setDataCadastro(LocalDateTime.of(2026, 1, 10, 0, 0));
        return idoso;
    }
}
