# Netinho Virtual

O Netinho Virtual é um assistente digital que faz por mensagem e por voz o papel que o neto costuma fazer: senta do lado, explica com paciência e acompanha a pessoa idosaaté ela concluir a tarefa no celular. Nas tarefas simples do dia a dia, ele também faz pela pessoa, sob comando de voz. Além disso, protege contra golpes, lembra de remédios e consultas e aciona a família em uma emergência. Funciona no WhatsApp, que o idoso já usa, e em um aplicativo Android configurado por um familiar.

## O que o sistema faz

- **Faz pra mim:** recebe um pedido, prepara uma tarefa e pede confirmação
  antes de mostrar uma prévia da ação.
- **Me ensina:** apresenta instruções passo a passo, como um guia de uso de
  serviços digitais.
- **Isso é golpe?:** analisa texto ou indicação de imagem e apresenta um nível
  de risco demonstrativo.
- **Pedir ajuda:** registra e acompanha solicitações de atendimento, rede de
  apoio, lembretes e preferências de acessibilidade.

Este é um protótipo. Não envia mensagens, não faz chamadas, não aciona
aplicativos do celular e não contata serviços externos. O ditado e a leitura em
voz alta dependem do navegador; o campo de texto permanece disponível como
alternativa.

## Arquitetura

O fluxo principal é:

`Navegador -> frontend -> API REST -> regras de negócio -> DAOs/JDBC -> Oracle`

- **Frontend:** HTML, CSS e JavaScript sem etapa de build. `app.js` controla a
  interface, o estado das telas e as chamadas à API.
- **API:** Java 25 e Spring Boot 3.5. Os controllers recebem as rotas HTTP,
  `bo` aplica regras de negócio, `dao` executa SQL e `model` representa os dados.
- **Banco:** Oracle Database, acessado por JDBC.
- **Testes:** JUnit e Mockito para regras de negócio e utilitários; os testes
  atuais não substituem testes de integração com Oracle.

## Estrutura do projeto

| Caminho | Conteúdo |
|---|---|
| `backend/src/main/java/.../controller` | Endpoints REST |
| `backend/src/main/java/.../bo` | Regras e fluxos de negócio |
| `backend/src/main/java/.../dao` | Consultas e operações JDBC |
| `backend/src/main/java/.../model` | Modelos e enums do domínio |
| `backend/src/main/java/.../dto` | Corpos de requisição e resposta |
| `backend/src/main/resources/application.properties` | Porta, banco e CORS |
| `backend/src/main/resources/db` | Scripts Oracle |
| `backend/src/test/java` | Testes automatizados |
| `backend/docs/endpoints.md` | Lista detalhada das rotas |
| `backend/requests.http` | Exemplos de chamadas HTTP |
| `frontend/index.html` | Página inicial |
| `frontend/app.js` | Renderização, navegação e integração com a API |
| `frontend/styles.css` | Layout e estilos responsivos |

## Requisitos e ferramentas

### Necessários para executar localmente

- **JDK 25** para compilar e executar a API. O workspace do VS Code aponta
  para essa versão; se estiver instalada em outra pasta, ajuste
  `.vscode/settings.json`.
- **Maven 3.9 ou compatível** para compilar, testar e iniciar a API. O
  Dockerfile usa Maven 3.9.12.
- **Oracle Database acessível** para iniciar a aplicação e executar os fluxos
  persistidos.
- **Oracle SQL Developer** (ou ferramenta equivalente) para executar os
  scripts de criação e carga.
- **Python 3** para servir o frontend com o comando deste guia. Pode ser
  substituído por outro servidor HTTP estático.
- **Navegador atualizado**. Chrome ou Edge são recomendados para testar ditado
  e leitura em voz alta.

### Opcionais

- **Docker** para construir e executar a API em container.
- **VS Code ou IntelliJ IDEA** para editar o projeto; não são necessários
  para executar pelo terminal.

Confira as instalações em um terminal:

```powershell
java -version
mvn -version
python --version
```

Para usar Docker, confira também `docker --version` e se o serviço Docker está
iniciado.

## 1. Preparar o banco Oracle

Conecte-se, no SQL Developer, ao schema que será utilizado pela API. Execute
**Run Script (F5)** nesta ordem:

1. `backend/src/main/resources/db/01_netinho_ddl.sql` — cria tabelas,
   sequências, restrições e índices.
2. `backend/src/main/resources/db/02_netinho_dml.sql` — insere dados de
   demonstração.
3. `backend/src/main/resources/db/03_netinho_dql.sql` — consultas opcionais
   para conferir os dados.
4. `backend/src/main/resources/db/05_netinho_acesso.sql` — cria a tabela de
   login (`ACESSO`) e a conta de demonstração **netinho / 123456**. Sem ele
   ninguém consegue entrar no app.
5. `backend/src/main/resources/db/06_netinho_guias_completos.sql` — completa os
   passos dos guias (todos os ativos ficam com 5 ou 6 passos). Pode ser
   executado mais de uma vez.

O DDL e o DML são para a preparação inicial do schema; não os execute novamente
em um banco já carregado.

`04_netinho_reparar_carga_parcial.sql` é uma recuperação excepcional para um
estado parcial específico, descrito no próprio script. Execute-o somente se o
schema estiver exatamente nesse estado. Não o use como instalação normal e não
repita o DML depois da recuperação.

## 2. Configurar e iniciar a API

Abra um terminal na raiz do projeto. Configure as credenciais por variáveis de
ambiente ou crie `backend/.env` com uma chave por linha:

```properties
DB_URL=jdbc:oracle:thin:@//<host>:<porta>/<service_name>
DB_USER=<usuario_do_schema>
DB_PASSWORD=<senha>
# Assina os tokens de login (mínimo 32 caracteres, aleatório). Sem ele, as
# sessões caem toda vez que a API reinicia.
AUTH_SECRET=<texto_aleatorio_longo>
# Opcional: análise de prints no "É golpe?" com o Claude. Sem a chave, o app
# usa só as regras de texto e responde com cautela (risco MÉDIO) para imagens.
ANTHROPIC_API_KEY=<chave_da_anthropic>
# Acesso de administração aos CRUDs (mínimo 32 caracteres, aleatório).
# Sem ele, só o app do idoso consegue usar a API.
ADMIN_TOKEN=<outro_texto_aleatorio_longo>
```

Para gerar os dois valores aleatórios: `openssl rand -hex 32`.

Para testar a API sem login (só localmente), use `AUTH_ENABLED=false`.

Use os valores fornecidos pelo administrador do banco. Algumas instalações
Oracle usam SID em vez de service name; nesse caso, o formato pode ser
`jdbc:oracle:thin:@<host>:<porta>:<SID>`. Não coloque credenciais no código,
não as envie para o repositório e não compartilhe o arquivo `.env`.

### Iniciar usando variáveis de ambiente (PowerShell)

Substitua os valores de exemplo e execute:

```powershell
Set-Location .\backend
$env:DB_URL = "jdbc:oracle:thin:@//<host>:<porta>/<service_name>"
$env:DB_USER = "<usuario_do_schema>"
$env:DB_PASSWORD = "<senha>"
mvn spring-boot:run
```

### Iniciar usando `backend/.env`

Preencha o arquivo `.env` com as três variáveis acima. Depois, na raiz do
projeto, execute:

```powershell
Set-Location .\backend
mvn spring-boot:run
```

Mantenha o terminal aberto. A API fica em `http://localhost:8080`; se a variável
`PORT` estiver definida, ela determina a porta.

Confirme que a API e o banco respondem:

```powershell
Invoke-RestMethod http://localhost:8080/idosos
```

Essa rota deve retornar a lista de idosos do schema. A rota `/` não é uma tela
nem um endpoint da API e, portanto, responde `404`.

## 3. Iniciar o frontend

Com a API em execução, abra **outro terminal na raiz do projeto**:

```powershell
Set-Location .\frontend
python -m http.server 5500
```

Abra `http://localhost:5500` no navegador e entre com **netinho / 123456**
(conta criada pelo script `05`). O endereço da API (`http://localhost:8080`)
pode ser trocado em **Configurações**. Publicado, o site procura a API no
mesmo endereço, em `/api`.

Não abra `frontend/index.html` diretamente como arquivo. O navegador precisa
carregar a página por HTTP para que a integração com a API funcione. O CORS do
backend permite por padrão `http://localhost:5500` e
`http://127.0.0.1:5500`.

Para encerrar cada processo, volte ao terminal correspondente e pressione
`Ctrl+C`.

## Testes e build

Os comandos Maven devem ser executados dentro de `backend`:

```powershell
Set-Location .\backend
mvn test
```

Para limpar os arquivos compilados e executar os testes novamente:

```powershell
mvn clean test
```

Os testes unitários de BO, utilitarios, DAO e Controller podem ser executados
sem contato com o banco:

```powershell
mvn test
```

Os testes de integracao com Oracle ficam desabilitados por padrao para evitar
alteracoes no schema compartilhado. Com `backend/.env` configurado e o schema
acessivel, execute-os com:

```powershell
$env:RUN_ORACLE_TESTS = "true"
mvn test
Remove-Item Env:RUN_ORACLE_TESTS
```

Os testes Oracle usam transacoes com rollback e fazem insert, update, select,
listagem e delete em dados temporarios. Os dados sao removidos antes do fim
de cada teste.

Para gerar o JAR executável:

```powershell
mvn package
```

### Testes das telas (Playwright)

As telas têm 20 testes automáticos (temas claro e escuro, celular de 360 px),
com a API simulada no próprio teste. Sem instalar nada, usando Docker:

```powershell
Set-Location .\frontend
docker run --rm -v "${PWD}:/work" -w /work mcr.microsoft.com/playwright:v1.64.1-noble sh -c "npm install && npx playwright test"
```

`tests/real.spec.js` é um teste de fumaça contra um ambiente de verdade; só roda
com `NETINHO_URL` definido (ele grava um alerta na conta de demonstração).

O JAR será criado em `backend/target/`. Os testes unitários atuais não precisam
de Oracle; para validar a persistência real, a aplicação precisa conectar a um
schema Oracle.

## Executar a API com Docker (opcional)

O Dockerfile está em `backend/`. Crie primeiro `backend/.env` com as variáveis
do banco e, a partir da raiz, execute:

```powershell
Set-Location .\backend
docker build -t netinho-api .
docker run --rm --name netinho-api --env-file .env -e PORT=8080 -p 8080:8080 netinho-api
```

A máquina que executa o container também precisa alcançar o Oracle configurado
em `DB_URL`. O frontend continua sendo iniciado separadamente conforme a etapa
anterior.

## Rotas principais

- `/idosos`, `/familiares`, `/voluntarios`, `/guias` e `/passos`: cadastros e
  consultas.
- `/tarefas`: criação, confirmação e avanço de tarefas/guias.
- `/auth/login`: troca usuário e senha por um token. Todas as outras rotas
  (menos `/saude`) exigem `Authorization: Bearer <token>`. O token do login só
  alcança as rotas do app e os dados do próprio idoso; os CRUDs exigem o
  `ADMIN_TOKEN`.
- `/golpes`: análise de conteúdo suspeito; `/golpes/verificar-imagem` recebe
  um print em base64.
- `/saude`: responde 200 quando a API e o banco estão no ar (usado pelo Docker).
- `/atendimentos`, `/emergencias`, `/idosos/{id}/rede-apoio` e
  `/idosos/{id}/historico`: ajuda e rede de apoio.
- `/consentimentos` e `/lembretes`: configurações e lembretes.

Consulte [`backend/docs/endpoints.md`](./backend/docs/endpoints.md) para métodos,
rotas e exemplos de corpos. Exemplos executáveis estão em
[`backend/requests.http`](./backend/requests.http).

## Limitações e segurança

- O login usa senha com hash PBKDF2 e token assinado (HMAC), com bloqueio de 5
  minutos depois de 5 senhas erradas. A conta do idoso só age sobre os próprios
  dados: o backend usa o idoso do token (ignora outro `idIdoso` no corpo) e
  confere o dono de tarefas, lembretes e familiares.
- A análise de imagens envia o print para a API da Anthropic (Claude). Avise a
  pessoa antes de usar com mensagens reais.
- Os dados do DML são de demonstração.
- A interface de celular é uma prévia; não envia mensagens nem toca no celular.
- Emergências e chamadas são fluxos demonstrativos; não contatam familiares ou
  serviços externos.
- O progresso do guia é mantido em memória e reinicia quando a API é encerrada.
- Ditado depende do suporte, da permissão de microfone e do serviço de voz do
  navegador. Se não funcionar, use o campo de texto.

## Solução de problemas

| Sintoma | O que verificar |
|---|---|
| `ERR_CONNECTION_REFUSED` em `localhost:8080` | A API está iniciada e o terminal Maven continua aberto? As credenciais e a conexão Oracle estão corretas? |
| `ERR_CONNECTION_REFUSED` em `localhost:5500` | O servidor Python foi iniciado dentro de `frontend` e o terminal continua aberto? |
| `404` em `http://localhost:8080/` | É esperado: a API não define uma rota raiz. Teste `/idosos`. |
| Erro `NoClassDefFoundError` ao usar `java -cp target\classes` | Esse comando não carrega dependências Spring. Inicie com `mvn spring-boot:run` ou use o JAR empacotado. |
| Erro de CORS | Acesse por `localhost:5500` ou `127.0.0.1:5500`; ajuste `FRONTEND_ORIGINS` para outra origem. |
| JDK não encontrado no VS Code | Ajuste o caminho do JDK 25 em `.vscode/settings.json` para a pasta onde ele está instalado. |
| Ditado não inicia ou não transcreve | Use Chrome/Edge, permita o microfone para `localhost` e verifique a conexão; como alternativa, digite o pedido. |

