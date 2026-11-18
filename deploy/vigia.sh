#!/bin/sh
# Vigia do Netinho: o cron roda a cada 2 minutos (/etc/cron.d/netinho-vigia).
# O Docker marca a API como "unhealthy" depois de 3 falhas seguidas em GET /saude;
# o restart do Docker nao age nesse caso, entao este script reinicia o container.
LOG=/var/log/netinho-vigia.log
STATUS=$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}sem-healthcheck{{end}}' netinho-api 2>/dev/null)
if [ "$STATUS" = "unhealthy" ]; then
  echo "$(date '+%F %T') API unhealthy: reiniciando netinho-api" >> "$LOG"
  docker restart netinho-api >/dev/null 2>&1
fi
