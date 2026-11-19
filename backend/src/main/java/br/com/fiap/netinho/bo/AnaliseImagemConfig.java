package br.com.fiap.netinho.bo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Escolhe quem le as fotos do "E golpe?": Claude se houver ANTHROPIC_API_KEY, senao NVIDIA se houver
 * NVIDIA_API_KEY. Sem nenhuma chave a analise fica desligada e o GolpeBO usa so as regras de texto.
 */
@Configuration
public class AnaliseImagemConfig {
    private static final Logger LOG = LoggerFactory.getLogger(AnaliseImagemConfig.class);

    @Bean
    public AnaliseImagem analiseImagem(@Value("${anthropic.api-key:}") String chaveAnthropic,
                                       @Value("${anthropic.model:claude-opus-5-5}") String modeloAnthropic,
                                       @Value("${nvidia.api-key:}") String chaveNvidia,
                                       @Value("${nvidia.model:nvidia/nemotron-3.5-super-vl-120b-a12b}") String modeloNvidia,
                                       @Value("${nvidia.base-url:https://integrate.api.nvidia.com/v1}") String urlNvidia) {
        if (chaveAnthropic != null && !chaveAnthropic.isBlank()) {
            LOG.info("Analise de imagens no 'E golpe?' pelo Claude ({}).", modeloAnthropic);
            return new ClaudeAnaliseImagem(chaveAnthropic, modeloAnthropic);
        }
        if (chaveNvidia != null && !chaveNvidia.isBlank()) {
            LOG.info("Analise de imagens no 'E golpe?' pela NVIDIA ({}).", modeloNvidia);
            return new NvidiaAnaliseImagem(chaveNvidia, modeloNvidia, urlNvidia);
        }
        return new ClaudeAnaliseImagem("", modeloAnthropic);
    }
}
