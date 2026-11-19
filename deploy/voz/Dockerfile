# Servico interno de voz do Netinho: transforma texto em fala natural (NVIDIA Magpie TTS, pt-BR)
# e devolve MP3. So a API do Netinho fala com ele, pela rede interna do docker compose.
FROM python:3.12-slim
RUN pip install --no-cache-dir nvidia-riva-client==2.* lameenc==1.*
WORKDIR /app
COPY servidor.py .
USER nobody
EXPOSE 8000
HEALTHCHECK --interval=60s --timeout=5s --retries=3 CMD python -c "import urllib.request;urllib.request.urlopen('http://127.0.0.1:8000/saude',timeout=3)"
CMD ["python", "-u", "servidor.py"]
