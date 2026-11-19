package br.com.fiap.netinho.bo;

import br.com.fiap.netinho.dao.*;
import br.com.fiap.netinho.exception.RecursoNaoEncontradoException;
import br.com.fiap.netinho.exception.RegraNegocioException;
import br.com.fiap.netinho.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Conversa do botao de falar: responde a pessoa com a IA e, quando faz sentido, sugere uma funcao do app.
 * Emergencia e decidida antes da IA; sem IA (ou se ela falhar) usa palavras-chave.
 */
@Service
public class ConversaBO {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConversaBO.class);
    public enum Acao { NENHUMA, FAZ_PRA_MIM, ME_ENSINA, GOLPE, AJUDA, LEMBRETES, EMERGENCIA }

    static final Pattern EMERGENCIA = Pattern.compile(
            "\\b(socorro|emergencia|urgencia|samu|ambulancia|infarto|desmai\\w*|cai no chao|me machuquei|passando mal|dor no peito|falta de ar|nao consigo respirar)\\b");
    /** A pessoa conta que recebeu um pedido (mensagem, ligacao) em vez de pedir para o Netinho fazer. */
    static final Pattern RECEBEU = Pattern.compile(
            "\\b(recebi|chegou|mandaram|me pediram|pediram|pedindo|pede|ligaram|ligou|disseram|falaram|mensagem|ganhei|premio|golpe)\\b");
    /** Promessas que o Netinho nao pode fazer: ele so prepara, quem envia e a pessoa. */
    static final Pattern PROMESSA = Pattern.compile(
            "\\b(eu )?(ja )?(mandei|enviei|vou (mandar|enviar|ligar|transferir|pagar)|liguei|estou (mandando|enviando|ligando))\\b");
    private static final Pattern CODIGO_ACAO = Pattern.compile("\\b(FAZ_PRA_MIM|ME_ENSINA|GOLPE|AJUDA|LEMBRETES|NENHUMA|EMERGENCIA)\\b");
    private static final Pattern EMOJI = Pattern.compile("[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{FE0F}\\x{200D}]");
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");
    private static final int MAX_TEXTO = 500, MAX_HISTORICO = 8;

    private final AssistenteIA ia;
    private final IdosoDAO idosoDAO;
    private final RedeApoioDAO redeDAO;
    private final FamiliarDAO familiarDAO;
    private final LembreteDAO lembreteDAO;
    private final GuiaDAO guiaDAO;
    private final ObjectMapper json = new ObjectMapper();

    public ConversaBO(AssistenteIA ia, IdosoDAO idosoDAO, RedeApoioDAO redeDAO, FamiliarDAO familiarDAO,
                      LembreteDAO lembreteDAO, GuiaDAO guiaDAO) {
        this.ia = ia; this.idosoDAO = idosoDAO; this.redeDAO = redeDAO; this.familiarDAO = familiarDAO;
        this.lembreteDAO = lembreteDAO; this.guiaDAO = guiaDAO;
    }

    public Map<String, Object> conversar(Long idIdoso, String texto, List<AssistenteIA.Mensagem> historico) {
        String fala = texto == null ? "" : texto.trim();
        if (fala.isEmpty()) throw new RegraNegocioException("Fale ou escreva alguma coisa para o Netinho.");
        if (fala.length() > MAX_TEXTO) fala = fala.substring(0, MAX_TEXTO);
        Idoso idoso = idosoDAO.buscarPorId(idIdoso).orElseThrow(() -> new RecursoNaoEncontradoException("Idoso nao encontrado."));
        String norma = TarefaBO.normalizar(fala);

        if (EMERGENCIA.matcher(norma).find())
            return resposta("Vou abrir a emergência agora. Segure o botão vermelho para chamar sua família. Se for grave, ligue 192.",
                    Acao.EMERGENCIA, null, null, false);

        // Dinheiro tambem nao depende da IA: a resposta e imediata e sempre a mesma.
        if (TarefaBO.PEDIDO_FINANCEIRO.matcher(norma).find()) {
            if (RECEBEU.matcher(norma).find())
                return resposta("Cuidado! Pedido de dinheiro por mensagem ou ligação pode ser golpe. Não pague nada e toque no botão para a gente verificar juntos.",
                        Acao.GOLPE, null, null, false);
            return resposta("Por segurança, eu não faço Pix, pagamentos nem nada de banco. Se precisar, peça ajuda para alguém da sua família.",
                    Acao.AJUDA, null, null, false);
        }

        List<Guia> guias = guiaDAO.ativos();
        if (ia.disponivel()) {
            List<AssistenteIA.Mensagem> conversa = new ArrayList<>();
            if (historico != null) {
                for (AssistenteIA.Mensagem m : historico.subList(Math.max(0, historico.size() - MAX_HISTORICO), historico.size())) {
                    if (m == null || m.texto() == null || m.texto().isBlank()) continue;
                    String papel = "assistant".equals(m.papel()) ? "assistant" : "user";
                    String t = m.texto().length() > MAX_TEXTO ? m.texto().substring(0, MAX_TEXTO) : m.texto();
                    // As falas antigas do Netinho voltam no mesmo formato JSON pedido; em texto puro o modelo
                    // passava a imitar o texto e abandonava o JSON.
                    if ("assistant".equals(papel)) t = json.createObjectNode().put("resposta", t).put("acao", "NENHUMA").toString();
                    conversa.add(new AssistenteIA.Mensagem(papel, t));
                }
            }
            conversa.add(new AssistenteIA.Mensagem("user", fala));
            String falaFinal = fala;
            Optional<Map<String, Object>> r = ia.responder(instrucoes(idoso, guias), conversa).flatMap(c -> interpretar(c, guias, falaFinal));
            if (r.isPresent()) return r.get();
            return porPalavrasChave(fala, norma, true);
        }
        return porPalavrasChave(fala, norma, false);
    }

    /** Contexto da pessoa e regras do Netinho, enviados como instrucoes de sistema. */
    String instrucoes(Idoso idoso, List<Guia> guias) {
        LocalDateTime agora = LocalDateTime.now(FUSO);
        StringBuilder contatos = new StringBuilder();
        for (RedeApoio r : redeDAO.listarPorIdoso(idoso.getId())) {
            familiarDAO.buscarPorId(r.getIdFamiliar()).ifPresent(f ->
                    contatos.append("- ").append(primeiroNome(f.getNome())).append(" (").append(r.getParentesco()).append(")\n"));
        }
        StringBuilder lembretes = new StringBuilder();
        for (Lembrete l : lembreteDAO.listarPorIdoso(idoso.getId())) {
            if (l.getStatus() == StatusLembrete.CANCELADO || l.getDataAgendamento() == null) continue;
            lembretes.append("- ").append(l.getDescricao()).append(": ").append(l.getDataAgendamento().format(DATA_HORA))
                    .append(l.getStatus() == StatusLembrete.CUMPRIDO ? " (ja feito)" : "")
                    .append(l.getDataAgendamento().isBefore(agora) ? " (essa data JA PASSOU)" : " (ainda vai acontecer)").append("\n");
        }
        StringBuilder listaGuias = new StringBuilder();
        for (Guia g : guias) listaGuias.append("- id ").append(g.getId()).append(": ").append(g.getNome()).append("\n");
        return """
                Voce e o %s, um neto virtual paciente e carinhoso que ajuda %s, uma pessoa idosa, a usar o celular.
                Agora e %s (horario de Brasilia).

                Como falar: portugues do Brasil, frases curtas e palavras simples, no maximo 3 frases, sem emojis,
                sem markdown, com pontuacao correta (a resposta vai ser lida em voz alta). Chame a pessoa de %s.
                Seja calmo e acolhedor. Nunca escreva os nomes das acoes (como GOLPE ou FAZ_PRA_MIM) na resposta.

                O que o app sabe fazer (sugira so isto, nunca invente outras coisas):
                - FAZ_PRA_MIM: mandar uma mensagem para um contato da lista abaixo. Preencha "pedido" no formato
                  "Manda mensagem pro <Nome> dizendo <mensagem>".
                - ME_ENSINA: ensinar passo a passo um dos guias abaixo. Preencha "idGuia" com o id do guia certo,
                  ou deixe null para mostrar a lista.
                - GOLPE: verificar se uma mensagem recebida e golpe.
                - AJUDA: pedir ajuda para a familia.
                - LEMBRETES: mostrar os lembretes de remedios e consultas.
                - NENHUMA: so conversar ou responder uma pergunta.

                Regras de seguranca: nunca faca nem ajude a fazer Pix, pagamento, transferencia ou emprestimo; nunca
                peca senha, codigo ou dados de banco. Se a pessoa contar que recebeu pedido de dinheiro, premio,
                "troquei de numero" ou link estranho, avise que pode ser golpe e use GOLPE. Voce nao liga, nao abre
                outros aplicativos e nao manda nada sozinho. Nunca diga "vou mandar", "ja mandei" ou "vou ligar":
                diga que a pessoa pode tocar no botao que vai aparecer e conferir antes de enviar.
                Se um lembrete ja passou, diga isso com clareza e sugira pedir a familia para marcar uma nova data.
                Nao invente datas, nomes ou informacoes que nao estao aqui. Se nao souber, diga com carinho.
                Se a pessoa parecer triste ou sozinha, acolha e sugira falar com a familia (AJUDA).

                Contatos da familia:
                %s
                Lembretes:
                %s
                Guias disponiveis:
                %s
                Responda SOMENTE com um objeto JSON, sem texto antes ou depois:
                {"resposta":"o que voce fala para a pessoa","acao":"NENHUMA|FAZ_PRA_MIM|ME_ENSINA|GOLPE|AJUDA|LEMBRETES","idGuia":null,"pedido":null}
                """.formatted(
                nomeAssistente(idoso), idoso.getNome(), agora.format(DATA_HORA), primeiroNome(idoso.getNome()),
                contatos.isEmpty() ? "(nenhum)\n" : contatos, lembretes.isEmpty() ? "(nenhum)\n" : lembretes,
                listaGuias.isEmpty() ? "(nenhum)\n" : listaGuias);
    }

    /** Le o JSON do modelo e confere tudo antes de devolver ao app. */
    Optional<Map<String, Object>> interpretar(String conteudo, List<Guia> guias, String fala) {
        int ini = conteudo.indexOf('{'), fim = conteudo.lastIndexOf('}');
        if (ini < 0 || fim <= ini) {
            // Veio so o texto, sem JSON: aproveita a fala da IA e escolhe o botao pelas palavras-chave.
            String texto = conteudo.replaceAll("(?s)<think>.*?</think>", "").trim();
            if (texto.isEmpty() || texto.length() > 600) return Optional.empty();
            Map<String, Object> regra = porPalavrasChave(fala, TarefaBO.normalizar(fala), false);
            Acao acao = Acao.valueOf(regra.get("acao").toString());
            return Optional.of(resposta(limparFala(texto, acao), acao, null, (String) regra.get("pedido"), true));
        }
        try {
            JsonNode n = json.readTree(conteudo.substring(ini, fim + 1));
            String texto = n.path("resposta").asText("").trim();
            if (texto.isEmpty()) return Optional.empty();
            Acao acao;
            try { acao = Acao.valueOf(n.path("acao").asText("NENHUMA").trim().toUpperCase()); }
            catch (IllegalArgumentException e) { acao = Acao.NENHUMA; }
            if (acao == Acao.EMERGENCIA) acao = Acao.NENHUMA; // emergencia so pelas regras acima
            Integer idGuia = n.path("idGuia").canConvertToInt() ? n.path("idGuia").asInt() : null;
            if (idGuia != null && guias.stream().noneMatch(g -> g.getId().equals(n.path("idGuia").asInt()))) idGuia = null;
            if (acao != Acao.ME_ENSINA) idGuia = null;
            String pedido = n.path("pedido").isTextual() ? n.path("pedido").asText().trim() : null;
            if (acao != Acao.FAZ_PRA_MIM || pedido == null || pedido.isEmpty()) pedido = acao == Acao.FAZ_PRA_MIM ? fala : null;
            // Dinheiro nunca vira tarefa, mesmo se o modelo errar.
            if (acao == Acao.FAZ_PRA_MIM && TarefaBO.PEDIDO_FINANCEIRO.matcher(TarefaBO.normalizar(pedido)).find()) {
                return Optional.of(resposta("Por segurança, eu não faço Pix, pagamentos nem nada de banco. Se alguém te pediu dinheiro, pode ser golpe.",
                        Acao.GOLPE, null, null, true));
            }
            return Optional.of(resposta(limparFala(texto, acao), acao, idGuia, pedido, true));
        } catch (Exception e) {
            LOG.warn("Nao deu para ler a resposta da IA ({}): {}", e.toString(), conteudo.substring(0, Math.min(300, conteudo.length())));
            return Optional.empty();
        }
    }

    /**
     * Ultima conferencia do que vai ser falado: tira emoji, markdown e codigos internos e troca
     * promessas ("eu mandei") pela frase padrao da acao, porque o app nunca envia nada sozinho.
     */
    static String limparFala(String texto, Acao acao) {
        String t = EMOJI.matcher(texto).replaceAll("").replaceAll("[*_#`]+", "").replaceAll("\\s+", " ").trim();
        boolean codigo = CODIGO_ACAO.matcher(t).find();
        if (codigo || PROMESSA.matcher(TarefaBO.normalizar(t)).find()) {
            String padrao = switch (acao) {
                case FAZ_PRA_MIM -> "Eu preparei a mensagem. Toque no botão abaixo para conferir antes de enviar.";
                case ME_ENSINA -> "Eu te ensino passo a passo. Toque no botão abaixo para começar.";
                case GOLPE -> "Cuidado, isso pode ser golpe. Toque no botão abaixo para a gente verificar juntos.";
                case AJUDA -> "Posso avisar sua família. Toque no botão abaixo.";
                case LEMBRETES -> "Toque no botão abaixo para ver seus lembretes.";
                default -> null;
            };
            if (padrao != null) return padrao;
            if (codigo) t = CODIGO_ACAO.matcher(t).replaceAll("").replaceAll("\\s+", " ").trim();
        }
        if (!t.isEmpty() && !t.matches("(?s).*[.!?]$")) t = t + ".";
        return t.isEmpty() ? "Pode falar de novo?" : t;
    }

    /** Sem IA: mesmas palavras-chave que o app usava antes, agora com uma resposta falada. */
    Map<String, Object> porPalavrasChave(String fala, String norma, boolean iaFalhou) {
        if (Pattern.compile("golpe|suspeit|estranh|premio|troquei de numero|ganhei|sorteio|\\b(recebi|chegou|me mandaram)\\b.*\\b(mensagem|ligacao|link|codigo)\\b").matcher(norma).find())
            return resposta("Vamos ver juntos se é golpe. Toque no botão abaixo.", Acao.GOLPE, null, null, false);
        if (Pattern.compile("ensina|aprend|aposentadoria|como (eu )?fa|extrato").matcher(norma).find())
            return resposta("Eu te ensino passo a passo. Toque no botão abaixo para escolher.", Acao.ME_ENSINA, null, null, false);
        if (Pattern.compile("lembrete|remedio|consulta|vacina").matcher(norma).find())
            return resposta("Vou te mostrar seus lembretes.", Acao.LEMBRETES, null, null, false);
        if (Pattern.compile("ajuda|chamar algu|sozinh").matcher(norma).find())
            return resposta("Posso avisar sua família. Toque no botão abaixo.", Acao.AJUDA, null, null, false);
        if (Pattern.compile("mensagem|manda|avisa|fala (com|pr)").matcher(norma).find())
            return resposta("Eu preparo a mensagem para você conferir antes.", Acao.FAZ_PRA_MIM, null, fala, false);
        return resposta(iaFalhou
                ? "Desculpe, demorei para pensar. Pode falar de novo, ou tocar em uma das opções da tela."
                : "Não entendi bem. Você pode tocar em uma das opções da tela ou falar de novo.", Acao.NENHUMA, null, null, false);
    }

    private static Map<String, Object> resposta(String texto, Acao acao, Integer idGuia, String pedido, boolean ia) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("resposta", texto);
        r.put("acao", acao.name());
        r.put("idGuia", idGuia);
        r.put("pedido", pedido);
        r.put("ia", ia);
        return r;
    }

    private static String nomeAssistente(Idoso i) {
        return i.getNomeAssistente() == null || i.getNomeAssistente().isBlank() ? "Netinho" : i.getNomeAssistente();
    }

    private static String primeiroNome(String nome) {
        return nome == null ? "" : nome.trim().split("\\s+")[0];
    }
}
