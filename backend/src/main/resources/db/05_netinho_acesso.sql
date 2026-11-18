-- Cria a tabela de acesso (login do aplicativo) e o usuario de demonstracao.
-- Execute depois do 01 e do 02, no mesmo schema usado pela API.
-- A senha fica guardada como hash PBKDF2-HMAC-SHA256 (nunca em texto puro).
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

CREATE SEQUENCE SQ_ACE START WITH 100 INCREMENT BY 1 NOCACHE;

CREATE TABLE ACESSO (
  id_ace NUMBER(8) PRIMARY KEY,
  id_ido NUMBER(8) NOT NULL,
  nm_ace_usuario VARCHAR2(40) NOT NULL,
  ds_ace_senha_hash VARCHAR2(200) NOT NULL,
  dh_ace_cadastro DATE NOT NULL,
  dh_ace_ultimo_acesso DATE,
  CONSTRAINT uk_ace_usuario UNIQUE (nm_ace_usuario),
  CONSTRAINT uk_ace_idoso UNIQUE (id_ido),
  CONSTRAINT fk_ace_idoso FOREIGN KEY (id_ido) REFERENCES IDOSO (id_ido) ON DELETE CASCADE,
  CONSTRAINT ck_ace_usuario CHECK (nm_ace_usuario = LOWER(nm_ace_usuario))
);

-- Usuario de demonstracao: netinho / 123456 (idoso 1, Maria Helena Silva).
INSERT INTO ACESSO (id_ace, id_ido, nm_ace_usuario, ds_ace_senha_hash, dh_ace_cadastro)
VALUES (1, 1, 'netinho', 'pbkdf2$210000$DAL+Mltr0kmTKyEoDDDX9w==$F/Fa6xOHN+0AKpYkexkDDY3m/QDYsvJ4xb9F53Uf+xY=', SYSDATE);

COMMIT;
