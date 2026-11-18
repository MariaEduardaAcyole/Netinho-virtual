-- Completa os passos dos guias ativos (o 02 deixou alguns com 2 ou 3 passos).
-- Pode ser executado mais de uma vez: atualiza o passo que ja existe e cria o que falta.
-- Revise os textos com a versao atual de cada aplicativo antes de apresentar.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

-- Guia 2: Consultar extrato (6 passos)
MERGE INTO PASSO p USING (SELECT 2 id_gui, 1 ordem, 'Aplicativo Meu INSS' elemento, 'Abra o aplicativo Meu INSS no seu celular. O desenho é azul, com a palavra INSS.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 2 id_gui, 2 ordem, 'Entrar com gov.br' elemento, 'Toque em Entrar com gov.br, digite o seu CPF e toque em Continuar.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 2 id_gui, 3 ordem, 'Senha' elemento, 'Digite a sua senha do gov.br com calma, sem mostrar para ninguém, e toque em Entrar.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 2 id_gui, 4 ordem, 'Do que você precisa?' elemento, 'Na tela inicial, toque na caixa de busca escrita Do que você precisa?.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 2 id_gui, 5 ordem, 'Extrato de Pagamento' elemento, 'Escreva extrato e toque em Extrato de Pagamento de Benefício.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 2 id_gui, 6 ordem, 'Baixar PDF' elemento, 'Escolha o mês que você quer ver. Se quiser guardar, toque em Baixar PDF.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
UPDATE GUIA SET dt_gui_atualizacao = SYSDATE WHERE id_gui = 2;

-- Guia 3: Agendar consulta (6 passos)
MERGE INTO PASSO p USING (SELECT 3 id_gui, 1 ordem, 'Aplicativo Meu SUS Digital' elemento, 'Abra o aplicativo Meu SUS Digital, que antes se chamava Conecte SUS.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 3 id_gui, 2 ordem, 'Entrar com gov.br' elemento, 'Toque em Entrar com gov.br e informe o seu CPF e a sua senha.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 3 id_gui, 3 ordem, 'Agendamento' elemento, 'Procure a opção de agendamento. Se não aparecer, a sua cidade marca consultas por outro canal: ligue para o posto de saúde.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 3 id_gui, 4 ordem, 'Especialidade' elemento, 'Escolha o tipo de atendimento de que você precisa, por exemplo Clínico geral.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 3 id_gui, 5 ordem, 'Data e horário' elemento, 'Escolha o dia e o horário que forem melhores para você.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 3 id_gui, 6 ordem, 'Confirmar' elemento, 'Confira o local, o dia e a hora. Se estiver tudo certo, toque em Confirmar e anote o horário.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
UPDATE GUIA SET dt_gui_atualizacao = SYSDATE WHERE id_gui = 3;

-- Guia 4: Criar conta (6 passos)
MERGE INTO PASSO p USING (SELECT 4 id_gui, 1 ordem, 'Aplicativo gov.br' elemento, 'Abra o aplicativo gov.br. Se ele não estiver no celular, peça ajuda para instalar pela loja de aplicativos.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 4 id_gui, 2 ordem, 'Entrar com gov.br' elemento, 'Toque em Entrar com gov.br.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 4 id_gui, 3 ordem, 'CPF' elemento, 'Digite o seu CPF e toque em Continuar.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 4 id_gui, 4 ordem, 'Termo de uso' elemento, 'Leia o termo com calma. Se concordar, marque a caixinha e toque em Continuar.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 4 id_gui, 5 ordem, 'Confirmar identidade' elemento, 'Escolha como confirmar que é você. O mais fácil costuma ser o reconhecimento facial: olhe para a câmera com boa luz.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 4 id_gui, 6 ordem, 'Criar senha' elemento, 'Crie uma senha que só você saiba e confirme. Guarde em um lugar seguro, longe do celular.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
UPDATE GUIA SET dt_gui_atualizacao = SYSDATE WHERE id_gui = 4;

-- Guia 6: Atualizar dados (5 passos)
MERGE INTO PASSO p USING (SELECT 6 id_gui, 1 ordem, 'Aplicativo Meu INSS' elemento, 'Abra o aplicativo Meu INSS no seu celular.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 6 id_gui, 2 ordem, 'Entrar com gov.br' elemento, 'Toque em Entrar com gov.br e informe o seu CPF e a sua senha.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 6 id_gui, 3 ordem, 'Do que você precisa?' elemento, 'Na tela inicial, toque na caixa de busca escrita Do que você precisa?.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 6 id_gui, 4 ordem, 'Atualizar dados' elemento, 'Escreva atualizar e toque no serviço de atualização do seu cadastro.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
MERGE INTO PASSO p USING (SELECT 6 id_gui, 5 ordem, 'Confirmar' elemento, 'Confira telefone, e-mail e endereço. Corrija o que estiver errado e toque em Confirmar.' instrucao FROM DUAL) n
  ON (p.id_gui = n.id_gui AND p.nr_pas_ordem = n.ordem)
  WHEN MATCHED THEN UPDATE SET p.nm_pas_elemento = n.elemento, p.tx_pas_instrucao = n.instrucao
  WHEN NOT MATCHED THEN INSERT (id_pas, id_gui, nr_pas_ordem, nm_pas_elemento, tx_pas_instrucao)
  VALUES (SQ_PAS.NEXTVAL, n.id_gui, n.ordem, n.elemento, n.instrucao);
UPDATE GUIA SET dt_gui_atualizacao = SYSDATE WHERE id_gui = 6;

COMMIT;
