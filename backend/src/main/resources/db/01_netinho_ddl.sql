-- Cria as sequencias, tabelas, restricoes e indices do schema do Netinho Virtual.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

CREATE SEQUENCE SQ_IDO START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_FAM START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_VOL START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_GUI START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_PAS START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_TAR START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_ATE START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_ALE START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_CON START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SQ_LEM START WITH 100 INCREMENT BY 1 NOCACHE;

CREATE TABLE IDOSO (
  id_ido NUMBER(8) PRIMARY KEY,
  nm_ido VARCHAR2(80) NOT NULL,
  dt_ido_nascimento DATE,
  nr_ido_telefone VARCHAR2(11) NOT NULL,
  tp_ido_aparelho VARCHAR2(7) NOT NULL,
  nm_ido_assistente VARCHAR2(30) NOT NULL,
  tp_ido_voz VARCHAR2(9) NOT NULL,
  tp_ido_jeito_falar VARCHAR2(9) NOT NULL,
  in_ido_sem_leitura NUMBER(1) NOT NULL,
  in_ido_aceita_voluntario NUMBER(1) NOT NULL,
  dt_ido_cadastro DATE NOT NULL,
  CONSTRAINT uk_ido_telefone UNIQUE (nr_ido_telefone),
  CONSTRAINT ck_ido_telefone CHECK (REGEXP_LIKE(nr_ido_telefone, '^[0-9]{10,11}$')),
  CONSTRAINT ck_ido_aparelho CHECK (tp_ido_aparelho IN ('ANDROID','IPHONE')),
  CONSTRAINT ck_ido_voz CHECK (tp_ido_voz IN ('FEMININA','MASCULINA')),
  CONSTRAINT ck_ido_jeito CHECK (tp_ido_jeito_falar IN ('CALMA','NORMAL','DEVAGAR')),
  CONSTRAINT ck_ido_sem_leitura CHECK (in_ido_sem_leitura IN (0,1)),
  CONSTRAINT ck_ido_voluntario CHECK (in_ido_aceita_voluntario IN (0,1))
);

CREATE TABLE FAMILIAR (
  id_fam NUMBER(8) PRIMARY KEY,
  nm_fam VARCHAR2(80) NOT NULL,
  nr_fam_telefone VARCHAR2(11) NOT NULL,
  ds_fam_email VARCHAR2(100) NOT NULL,
  dt_fam_cadastro DATE NOT NULL,
  CONSTRAINT uk_fam_telefone UNIQUE (nr_fam_telefone),
  CONSTRAINT uk_fam_email UNIQUE (ds_fam_email),
  CONSTRAINT ck_fam_telefone CHECK (REGEXP_LIKE(nr_fam_telefone, '^[0-9]{10,11}$'))
);

CREATE TABLE VOLUNTARIO (
  id_vol NUMBER(8) PRIMARY KEY,
  nm_vol VARCHAR2(80) NOT NULL,
  nr_vol_telefone VARCHAR2(11) NOT NULL,
  ds_vol_email VARCHAR2(100) NOT NULL,
  st_vol VARCHAR2(9) NOT NULL,
  dt_vol_cadastro DATE NOT NULL,
  CONSTRAINT uk_vol_telefone UNIQUE (nr_vol_telefone),
  CONSTRAINT uk_vol_email UNIQUE (ds_vol_email),
  CONSTRAINT ck_vol_telefone CHECK (REGEXP_LIKE(nr_vol_telefone, '^[0-9]{10,11}$')),
  CONSTRAINT ck_vol_status CHECK (st_vol IN ('PENDENTE','APROVADO','BLOQUEADO'))
);

CREATE TABLE GUIA (
  id_gui NUMBER(4) PRIMARY KEY,
  nm_gui VARCHAR2(100) NOT NULL,
  nm_gui_aplicativo VARCHAR2(30) NOT NULL,
  ds_gui VARCHAR2(300),
  st_gui VARCHAR2(7) NOT NULL,
  dt_gui_atualizacao DATE NOT NULL,
  CONSTRAINT uk_gui_nome UNIQUE (nm_gui),
  CONSTRAINT ck_gui_status CHECK (st_gui IN ('ATIVO','INATIVO'))
);

CREATE TABLE PASSO (
  id_pas NUMBER(6) PRIMARY KEY,
  id_gui NUMBER(4) NOT NULL,
  nr_pas_ordem NUMBER(2) NOT NULL,
  nm_pas_elemento VARCHAR2(80) NOT NULL,
  tx_pas_instrucao VARCHAR2(500) NOT NULL,
  CONSTRAINT uk_pas_guia_ordem UNIQUE (id_gui, nr_pas_ordem)
);

CREATE TABLE REDE_APOIO (
  id_ido NUMBER(8) NOT NULL,
  id_fam NUMBER(8) NOT NULL,
  ds_rea_parentesco VARCHAR2(30),
  nr_rea_ordem_contato NUMBER(2),
  in_rea_autorizado NUMBER(1) NOT NULL,
  dt_rea_cadastro DATE NOT NULL,
  CONSTRAINT pk_rea PRIMARY KEY (id_ido, id_fam),
  CONSTRAINT uk_rea_ido_ordem UNIQUE (id_ido, nr_rea_ordem_contato),
  CONSTRAINT ck_rea_ordem CHECK (nr_rea_ordem_contato >= 0),
  CONSTRAINT ck_rea_autorizado CHECK (in_rea_autorizado IN (0,1))
);

CREATE TABLE TAREFA (
  id_tar NUMBER(10) PRIMARY KEY,
  id_ido NUMBER(8) NOT NULL,
  id_gui NUMBER(4),
  tp_tar_modo VARCHAR2(11) NOT NULL,
  ds_tar_pedido VARCHAR2(300) NOT NULL,
  st_tar VARCHAR2(12) NOT NULL,
  dh_tar_solicitacao DATE NOT NULL,
  dh_tar_conclusao DATE,
  CONSTRAINT ck_tar_modo CHECK (tp_tar_modo IN ('FAZ_PRA_MIM','ME_ENSINA')),
  CONSTRAINT ck_tar_status CHECK (st_tar IN ('AGUARDANDO','EM_ANDAMENTO','CONCLUIDA','CANCELADA','RECUSADA')),
  CONSTRAINT ck_tar_guia_modo CHECK ((tp_tar_modo = 'ME_ENSINA' AND id_gui IS NOT NULL) OR (tp_tar_modo = 'FAZ_PRA_MIM' AND id_gui IS NULL)),
  CONSTRAINT ck_tar_datas CHECK (dh_tar_conclusao IS NULL OR dh_tar_conclusao >= dh_tar_solicitacao)
);

CREATE TABLE ATENDIMENTO (
  id_ate NUMBER(10) PRIMARY KEY,
  id_ido NUMBER(8) NOT NULL,
  id_tar NUMBER(10),
  id_pas NUMBER(6),
  id_fam NUMBER(8),
  id_vol NUMBER(8),
  st_ate VARCHAR2(12) NOT NULL,
  qt_ate_tentativas NUMBER(2) NOT NULL,
  dh_ate_solicitacao DATE NOT NULL,
  dh_ate_inicio DATE,
  dh_ate_fim DATE,
  CONSTRAINT ck_ate_status CHECK (st_ate IN ('SOLICITADO','EM_ANDAMENTO','CONCLUIDO','CANCELADO')),
  CONSTRAINT ck_ate_tentativas CHECK (qt_ate_tentativas >= 0),
  CONSTRAINT ck_ate_passo CHECK ((id_pas IS NULL) OR (id_tar IS NOT NULL)),
  CONSTRAINT ck_ate_atendente CHECK (
    (id_fam IS NULL AND id_vol IS NULL AND st_ate IN ('SOLICITADO','CANCELADO'))
    OR (id_fam IS NOT NULL AND id_vol IS NULL AND st_ate <> 'SOLICITADO')
    OR (id_fam IS NULL AND id_vol IS NOT NULL AND st_ate <> 'SOLICITADO')
  ),
  CONSTRAINT ck_ate_datas CHECK ((dh_ate_inicio IS NULL OR dh_ate_solicitacao <= dh_ate_inicio) AND (dh_ate_fim IS NULL OR dh_ate_inicio IS NULL OR dh_ate_inicio <= dh_ate_fim))
);

CREATE TABLE ALERTA_GOLPE (
  id_ale NUMBER(10) PRIMARY KEY,
  id_ido NUMBER(8) NOT NULL,
  tp_ale_origem VARCHAR2(10) NOT NULL,
  tp_ale_conteudo VARCHAR2(6) NOT NULL,
  tx_ale_mensagem VARCHAR2(500) NOT NULL,
  tp_ale_risco VARCHAR2(5) NOT NULL,
  ds_ale_motivo VARCHAR2(300) NOT NULL,
  in_ale_familiar_avisado NUMBER(1) NOT NULL,
  dh_ale_registro DATE NOT NULL,
  CONSTRAINT ck_ale_origem CHECK (tp_ale_origem IN ('MANUAL','AUTOMATICO')),
  CONSTRAINT ck_ale_conteudo CHECK (tp_ale_conteudo IN ('TEXTO','LINK','IMAGEM')),
  CONSTRAINT ck_ale_risco CHECK (tp_ale_risco IN ('BAIXO','MEDIO','ALTO')),
  CONSTRAINT ck_ale_avisado CHECK (in_ale_familiar_avisado IN (0,1))
);

CREATE TABLE CONSENTIMENTO (
  id_con NUMBER(10) PRIMARY KEY,
  id_ido NUMBER(8) NOT NULL,
  id_fam NUMBER(8),
  tp_con VARCHAR2(19) NOT NULL,
  st_con VARCHAR2(9) NOT NULL,
  dh_con_concessao DATE NOT NULL,
  dh_con_revogacao DATE,
  CONSTRAINT uk_con_ido_tipo UNIQUE (id_ido, tp_con),
  CONSTRAINT ck_con_tipo CHECK (tp_con IN ('MICROFONE','ACESSIBILIDADE','SOBREPOSICAO_TELA','LEITURA_NOTIFICACAO','LOCALIZACAO')),
  CONSTRAINT ck_con_status CHECK (st_con IN ('CONCEDIDO','REVOGADO')),
  CONSTRAINT ck_con_revogacao CHECK ((st_con = 'REVOGADO' AND dh_con_revogacao IS NOT NULL) OR (st_con = 'CONCEDIDO'))
);

CREATE TABLE LEMBRETE (
  id_lem NUMBER(10) PRIMARY KEY,
  id_ido NUMBER(8) NOT NULL,
  id_fam NUMBER(8),
  tp_lem VARCHAR2(8) NOT NULL,
  ds_lem VARCHAR2(200) NOT NULL,
  dh_lem_agendamento DATE NOT NULL,
  st_lem VARCHAR2(9) NOT NULL,
  dh_lem_cumprimento DATE,
  CONSTRAINT ck_lem_tipo CHECK (tp_lem IN ('REMEDIO','CONSULTA','VACINA')),
  CONSTRAINT ck_lem_status CHECK (st_lem IN ('PENDENTE','CUMPRIDO','CANCELADO')),
  CONSTRAINT ck_lem_cumprimento CHECK ((st_lem = 'CUMPRIDO' AND dh_lem_cumprimento IS NOT NULL) OR (st_lem IN ('PENDENTE','CANCELADO')))
);

ALTER TABLE PASSO ADD CONSTRAINT fk_pas_gui FOREIGN KEY (id_gui) REFERENCES GUIA(id_gui);
ALTER TABLE REDE_APOIO ADD CONSTRAINT fk_rea_ido FOREIGN KEY (id_ido) REFERENCES IDOSO(id_ido);
ALTER TABLE REDE_APOIO ADD CONSTRAINT fk_rea_fam FOREIGN KEY (id_fam) REFERENCES FAMILIAR(id_fam);
ALTER TABLE TAREFA ADD CONSTRAINT fk_tar_ido FOREIGN KEY (id_ido) REFERENCES IDOSO(id_ido);
ALTER TABLE TAREFA ADD CONSTRAINT fk_tar_gui FOREIGN KEY (id_gui) REFERENCES GUIA(id_gui);
ALTER TABLE ATENDIMENTO ADD CONSTRAINT fk_ate_ido FOREIGN KEY (id_ido) REFERENCES IDOSO(id_ido);
ALTER TABLE ATENDIMENTO ADD CONSTRAINT fk_ate_tar FOREIGN KEY (id_tar) REFERENCES TAREFA(id_tar);
ALTER TABLE ATENDIMENTO ADD CONSTRAINT fk_ate_pas FOREIGN KEY (id_pas) REFERENCES PASSO(id_pas);
ALTER TABLE ATENDIMENTO ADD CONSTRAINT fk_ate_fam FOREIGN KEY (id_fam) REFERENCES FAMILIAR(id_fam);
ALTER TABLE ATENDIMENTO ADD CONSTRAINT fk_ate_vol FOREIGN KEY (id_vol) REFERENCES VOLUNTARIO(id_vol);
ALTER TABLE ALERTA_GOLPE ADD CONSTRAINT fk_ale_ido FOREIGN KEY (id_ido) REFERENCES IDOSO(id_ido);
ALTER TABLE CONSENTIMENTO ADD CONSTRAINT fk_con_ido FOREIGN KEY (id_ido) REFERENCES IDOSO(id_ido);
ALTER TABLE CONSENTIMENTO ADD CONSTRAINT fk_con_fam FOREIGN KEY (id_fam) REFERENCES FAMILIAR(id_fam);
ALTER TABLE LEMBRETE ADD CONSTRAINT fk_lem_ido FOREIGN KEY (id_ido) REFERENCES IDOSO(id_ido);
ALTER TABLE LEMBRETE ADD CONSTRAINT fk_lem_fam FOREIGN KEY (id_fam) REFERENCES FAMILIAR(id_fam);

CREATE INDEX idx_pas_gui ON PASSO(id_gui);
CREATE INDEX idx_rea_fam ON REDE_APOIO(id_fam);
CREATE INDEX idx_tar_ido ON TAREFA(id_ido);
CREATE INDEX idx_tar_gui ON TAREFA(id_gui);
CREATE INDEX idx_ate_ido ON ATENDIMENTO(id_ido);
CREATE INDEX idx_ate_tar ON ATENDIMENTO(id_tar);
CREATE INDEX idx_ate_pas ON ATENDIMENTO(id_pas);
CREATE INDEX idx_ate_fam ON ATENDIMENTO(id_fam);
CREATE INDEX idx_ate_vol ON ATENDIMENTO(id_vol);
CREATE INDEX idx_ale_ido ON ALERTA_GOLPE(id_ido);
CREATE INDEX idx_con_ido ON CONSENTIMENTO(id_ido);
CREATE INDEX idx_lem_ido ON LEMBRETE(id_ido);

COMMENT ON TABLE IDOSO IS 'Pessoa idosa atendida pelo Netinho Virtual.';
COMMENT ON TABLE FAMILIAR IS 'Familiar que integra a rede de apoio.';
COMMENT ON TABLE VOLUNTARIO IS 'Voluntario aprovado para atendimentos.';
COMMENT ON TABLE GUIA IS 'Guia de um servico essencial.';
COMMENT ON TABLE PASSO IS 'Passo sequencial de um guia.';
COMMENT ON TABLE REDE_APOIO IS 'Associacao entre idosos e familiares.';
COMMENT ON TABLE TAREFA IS 'Pedido Faz pra mim ou Me ensina.';
COMMENT ON TABLE ATENDIMENTO IS 'Pedido de ajuda ou atendimento por video.';
COMMENT ON TABLE ALERTA_GOLPE IS 'Resultado da verificacao de golpe.';
COMMENT ON TABLE CONSENTIMENTO IS 'Permissao concedida ou revogada pelo idoso.';
COMMENT ON TABLE LEMBRETE IS 'Remedio, consulta ou vacina agendada.';
