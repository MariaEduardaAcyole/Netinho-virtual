> Todas as rotas, menos `/auth/login` e `/saude`, exigem `Authorization: Bearer <token>`.
> O token do login (conta do idoso) alcança só as rotas usadas pelo app e os dados do próprio idoso;
> as demais (CRUDs de cadastro) exigem o `ADMIN_TOKEN` configurado na API.

# Endpoints da API

Base local: `http://localhost:8080`. Os exemplos de corpos abaixo são JSON. A
API não implementa autenticação.

## Idosos

| Método | Rota | Corpo |
|---|---|---|
| GET | `/idosos` | - |
| GET | `/idosos/{id}` | - |
| POST | `/idosos` | objeto `Idoso` |
| PUT | `/idosos/{id}` | objeto `Idoso` |
| DELETE | `/idosos/{id}` | - |
| PUT | `/idosos/{id}/modo-sem-leitura` | `{"ativo":true}` |

## Familiares, voluntários, guias e passos

| Método | Rota | Corpo |
|---|---|---|
| GET, POST | `/familiares` | objeto `Familiar` no POST |
| GET, PUT, DELETE | `/familiares/{id}` | objeto `Familiar` no PUT |
| GET, POST | `/voluntarios` | objeto `Voluntario` no POST |
| GET, PUT, DELETE | `/voluntarios/{id}` | objeto `Voluntario` no PUT |
| PUT | `/voluntarios/{id}/status` | `{"status":"APROVADO"}` |
| GET, POST | `/guias` | objeto `Guia` no POST |
| GET, PUT, DELETE | `/guias/{id}` | objeto `Guia` no PUT |
| GET, POST | `/guias/{id}/passos` | objeto `Passo` no POST |
| PUT, DELETE | `/passos/{id}` | objeto `Passo` no PUT |

## Tarefas e guias

| Método | Rota | Corpo |
|---|---|---|
| POST | `/auth/login` | `{"usuario":"netinho","senha":"123456"}` → `{token, idIdoso, nome, expiraEm}` |
| GET | `/saude` | - (200 com banco no ar, 503 sem banco) |
| POST | `/golpes/verificar-imagem` | `{"idIdoso":1,"texto":"","imagemBase64":"...","tipoImagem":"image/jpeg"}` |
| POST | `/tarefas` | `{"idIdoso":1,"pedido":"..."}` ou `{"idIdoso":1,"idGuia":3}` para abrir um guia escolhido |
| GET | `/tarefas/{id}` | - |
| GET | `/idosos/{id}/tarefas` | - |
| PUT | `/tarefas/{id}/confirmar` | `{"resposta":"SIM"}` |
| PUT | `/tarefas/{id}/desfazer` | - |
| DELETE | `/tarefas/{id}` | - |
| GET | `/tarefas/{id}/passo-atual` | - |
| PUT | `/tarefas/{id}/avancar` | `{"concluido":true}` |

## Golpes

| Método | Rota | Corpo |
|---|---|---|
| POST | `/golpes/verificar` | `{"idIdoso":1,"texto":"...","imagem":false}` |
| POST | `/golpes/notificacao-recebida` | mesmo corpo de `/golpes/verificar` |
| GET | `/idosos/{id}/golpes` | - |

## Rede de apoio, atendimentos e emergência

| Método | Rota | Corpo |
|---|---|---|
| GET, POST | `/idosos/{id}/rede-apoio` | `{"idFamiliar":1,"parentesco":"filho","ordemContato":1,"autorizado":true}` no POST |
| PUT, DELETE | `/idosos/{id}/rede-apoio/{idFamiliar}` | objeto de rede de apoio no PUT |
| POST | `/atendimentos` | `{"idIdoso":1,"idTarefa":2,"idPasso":3,"tentativas":1}` |
| GET | `/atendimentos/{id}` | - |
| PUT | `/atendimentos/{id}/iniciar` | `{"idFamiliar":1,"idVoluntario":null}` |
| PUT | `/atendimentos/{id}/concluir` | - |
| GET | `/voluntarios/{id}/relatorio-horas` | - |
| GET | `/idosos/{id}/historico` | opcional: `?idFamiliar=1` |
| POST | `/emergencias` | `{"idIdoso":1}` |

## Consentimentos e lembretes

| Método | Rota | Corpo |
|---|---|---|
| GET, POST | `/idosos/{id}/consentimentos` | objeto `Consentimento` no POST |
| PUT | `/consentimentos/{id}` | objeto `Consentimento` |
| GET | `/lembretes` | - |
| GET | `/lembretes/{id}` | - |
| GET | `/idosos/{id}/lembretes` | - |
| POST | `/lembretes` | objeto `Lembrete` |
| PUT | `/lembretes/{id}` | objeto `Lembrete` |
| DELETE | `/lembretes/{id}` | - |

Os campos completos dos modelos podem ser consultados em
`backend/src/main/java/br/com/fiap/netinho/model/`. Para executar exemplos,
consulte [`../requests.http`](../requests.http).
