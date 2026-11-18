-- Consultas de leitura para explorar e conferir os dados do schema.
-- Pergunta: Quais atendimentos por video foram concluidos por voluntarios e quanto duraram?
-- Resultado esperado: uma linha por atendimento concluido por voluntario, com minutos positivos entre inicio e fim.
SELECT v.nm_vol AS voluntario,
       a.id_ate AS id_atendimento,
       i.nm_ido AS idoso,
       a.dh_ate_inicio AS inicio,
       a.dh_ate_fim AS fim,
       ROUND((a.dh_ate_fim - a.dh_ate_inicio) * 24 * 60) AS duracao_minutos
FROM VOLUNTARIO v
INNER JOIN ATENDIMENTO a ON a.id_vol = v.id_vol
INNER JOIN IDOSO i ON i.id_ido = a.id_ido
WHERE a.st_ate = 'CONCLUIDO'
ORDER BY a.dh_ate_fim DESC;

-- Pergunta: Quais guias sao mais pedidos, quantas tarefas foram concluidas e qual o tempo medio?
-- Resultado esperado: uma linha por guia utilizado, ordenada pela quantidade total de tarefas.
SELECT g.nm_gui AS guia,
       COUNT(t.id_tar) AS total_pedidos,
       SUM(CASE WHEN t.st_tar = 'CONCLUIDA' THEN 1 ELSE 0 END) AS concluidas,
       ROUND(AVG(CASE
         WHEN t.dh_tar_conclusao IS NOT NULL
         THEN (t.dh_tar_conclusao - t.dh_tar_solicitacao) * 24 * 60
         ELSE NULL
       END), 2) AS tempo_medio_minutos
FROM GUIA g
INNER JOIN TAREFA t ON t.id_gui = g.id_gui
GROUP BY g.nm_gui
ORDER BY COUNT(t.id_tar) DESC, g.nm_gui;

-- Pergunta: Entre os idosos com Android, quem revogou alguma permissao e quem nao revogou?
-- Resultado esperado: todos os idosos Android, com a ultima revogacao quando existir.
SELECT i.nm_ido AS idoso,
       i.nr_ido_telefone AS telefone,
       c.tp_con AS permissao_revogada,
       c.dh_con_revogacao AS quando_revogou
FROM IDOSO i
LEFT OUTER JOIN CONSENTIMENTO c
  ON c.id_ido = i.id_ido
 AND c.st_con = 'REVOGADO'
WHERE i.tp_ido_aparelho = 'ANDROID'
ORDER BY i.nm_ido, c.dh_con_revogacao;
