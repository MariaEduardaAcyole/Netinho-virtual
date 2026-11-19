package br.com.fiap.netinho.controller;

import br.com.fiap.netinho.bo.VozBO;
import br.com.fiap.netinho.dto.Dtos.VozRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

/**
 * Voz natural do Netinho: devolve o MP3 da fala. 204 quando a voz natural nao esta disponivel,
 * e o app entao usa a voz do aparelho.
 */
@RestController
public class VozController {
    private final VozBO vozBO;
    public VozController(VozBO vozBO) { this.vozBO = vozBO; }

    @PostMapping("/voz")
    public ResponseEntity<byte[]> falar(@RequestBody VozRequest r) {
        return vozBO.falar(r.texto(), r.voz())
                .map(mp3 -> ResponseEntity.ok()
                        .contentType(MediaType.valueOf("audio/mpeg"))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                        .body(mp3))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
