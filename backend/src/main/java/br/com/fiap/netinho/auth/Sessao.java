package br.com.fiap.netinho.auth;

import br.com.fiap.netinho.exception.AcessoNegadoException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Le o idoso da sessao atual (definido pelo AuthFilter) e confere a quem pertencem os dados.
 * Sem idoso na sessao (token de administracao ou login desligado) as conferencias nao se aplicam.
 */
public final class Sessao {
    private Sessao() {}

    /** Idoso dono do token desta requisicao, ou null para administracao / login desligado. */
    public static Long idoso() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos == null) return null;
        Object valor = atributos.getAttribute(AuthFilter.ATRIBUTO_ID_IDOSO, RequestAttributes.SCOPE_REQUEST);
        return valor instanceof Long id ? id : null;
    }

    /**
     * Idoso que a operacao deve usar: o da sessao. Se o corpo informar outro idoso, recusa.
     * Para administracao, vale o idoso informado.
     */
    public static Long resolver(Long informado) {
        Long sessao = idoso();
        if (sessao == null) return informado;
        if (informado != null && !informado.equals(sessao))
            throw new AcessoNegadoException("Esta conta nao pode agir em nome de outra pessoa.");
        return sessao;
    }

    /** Recusa quando o dado pertence a outro idoso. */
    public static void conferirDono(Long dono) {
        Long sessao = idoso();
        if (sessao != null && !sessao.equals(dono))
            throw new AcessoNegadoException("Esta conta nao pode ver ou alterar dados de outra pessoa.");
    }
}
