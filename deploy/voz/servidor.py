"""Servico de voz do Netinho.

POST /sintetizar  {"texto": "...", "voz": "PT-BR.Isabela"}  ->  audio/mpeg
GET  /saude                                               ->  ok

Usa o Magpie TTS Multilingual hospedado pela NVIDIA (gRPC em grpc.nvcf.nvidia.com) com a mesma
NVIDIA_API_KEY da conversa. Fala frase por frase e junta tudo num MP3 leve para o celular.
"""
import json
import os
import re
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

import lameenc
import riva.client

CHAVE = os.environ.get("NVIDIA_API_KEY", "").strip()
FUNCAO = os.environ.get("NVIDIA_TTS_FUNCTION_ID", "877104f7-e885-42b9-8de8-f6e4c6303969")
VOZ_PADRAO = os.environ.get("NVIDIA_TTS_VOZ", "PT-BR.Diego")
VOZES = {"PT-BR.Isabela", "PT-BR.Louise", "PT-BR.Diego"}
TAXA = 22050
MAX_TEXTO = 600

_local = threading.local()


def servico():
    # Um canal gRPC por thread: simples e seguro com o ThreadingHTTPServer.
    if not hasattr(_local, "tts"):
        auth = riva.client.Auth(None, True, "grpc.nvcf.nvidia.com:443",
                                [["function-id", FUNCAO], ["authorization", "Bearer " + CHAVE]])
        _local.tts = riva.client.SpeechSynthesisService(auth)
    return _local.tts


def frases(texto):
    partes = [p.strip() for p in re.findall(r"[^.!?;]+[.!?;]*", texto) if p.strip()]
    blocos = []
    for p in partes or [texto]:
        if blocos and len(blocos[-1]) + len(p) < 180:
            blocos[-1] += " " + p
        else:
            blocos.append(p)
    return blocos


def sintetizar(texto, voz):
    pausa = b"\x00\x00" * int(TAXA * 0.18)  # respiro curto entre frases
    pcm = b""
    for bloco in frases(texto):
        r = servico().synthesize(bloco, voice_name="Magpie-Multilingual." + voz, language_code="pt-BR",
                                 sample_rate_hz=TAXA)
        pcm += r.audio + pausa
    mp3 = lameenc.Encoder()
    mp3.set_bit_rate(48)
    mp3.set_in_sample_rate(TAXA)
    mp3.set_channels(1)
    mp3.set_quality(2)
    return mp3.encode(pcm) + mp3.flush()


class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):  # sem texto da pessoa nos logs
        pass

    def responder(self, codigo, corpo, tipo="application/json"):
        self.send_response(codigo)
        self.send_header("Content-Type", tipo)
        self.send_header("Content-Length", str(len(corpo)))
        self.end_headers()
        self.wfile.write(corpo)

    def do_GET(self):
        if self.path == "/saude":
            return self.responder(200, b"ok", "text/plain")
        self.responder(404, b"{}")

    def do_POST(self):
        if self.path != "/sintetizar":
            return self.responder(404, b"{}")
        if not CHAVE:
            return self.responder(503, b'{"erro":"sem NVIDIA_API_KEY"}')
        try:
            tamanho = min(int(self.headers.get("Content-Length", 0)), 8192)
            dados = json.loads(self.rfile.read(tamanho) or b"{}")
            texto = str(dados.get("texto", "")).strip()[:MAX_TEXTO]
            voz = dados.get("voz") if dados.get("voz") in VOZES else VOZ_PADRAO
            if not texto:
                return self.responder(400, b'{"erro":"texto vazio"}')
            self.responder(200, sintetizar(texto, voz), "audio/mpeg")
        except Exception as e:  # noqa: BLE001 - qualquer falha vira 502 e o app usa a voz do aparelho
            print("falha na voz:", type(e).__name__, str(e)[:200])
            self.responder(502, b'{"erro":"falha na voz"}')


if __name__ == "__main__":
    print("Voz do Netinho pronta (voz padrao %s)." % VOZ_PADRAO if CHAVE else "Sem NVIDIA_API_KEY: voz desligada.")
    ThreadingHTTPServer(("0.0.0.0", 8000), Handler).serve_forever()
