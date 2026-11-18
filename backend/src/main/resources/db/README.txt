Scripts Oracle (execute com Run Script/F5 no SQL Developer, conectado ao schema
que será utilizado pela API):
1. 01_netinho_ddl.sql cria as tabelas e sequencias.
2. 02_netinho_dml.sql insere os dados de demonstracao.
3. 03_netinho_dql.sql contem consultas de leitura.

04_netinho_reparar_carga_parcial.sql nao faz parte do fluxo normal. Use-o
somente quando a carga tiver sido interrompida no estado parcial exato descrito
no script. Ele valida as contagens antes de alterar o schema e inserir dados.
Se o banco ja estiver completo ou tiver outro estado, nao execute esse script
nem repita o DML.
