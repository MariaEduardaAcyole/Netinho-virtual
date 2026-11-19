// Testes de ponta a ponta das telas do Netinho com a API simulada.
const { test, expect } = require("@playwright/test");

const API = "http://localhost:8080";
const PNG_1PX = Buffer.from(
  "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
  "base64",
);

// Simula a API do Netinho e registra as chamadas para os testes conferirem.
async function simularApi(page, { fora = false, sessaoExpirada = false } = {}) {
  const chamadas = [];
  let passo = 0;
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (route) => route.abort());
  await page.route(`${API}/**`, async (route) => {
    const req = route.request();
    const url = new URL(req.url());
    const corpo = req.postData() ? JSON.parse(req.postData()) : null;
    chamadas.push({ metodo: req.method(), caminho: url.pathname, corpo, auth: req.headers().authorization || "" });
    const json = (dados, status = 200) => route.fulfill({ status, contentType: "application/json", body: JSON.stringify(dados) });
    const p = url.pathname;

    if (fora) return route.fulfill({ status: 502, body: "" });
    if (p === "/auth/login") {
      return corpo.usuario === "netinho" && corpo.senha === "123456"
        ? json({ token: "token-teste", idIdoso: 1, nome: "Maria Helena Silva", expiraEm: "2026-12-18T00:00:00Z" })
        : json({ mensagem: "Usuario ou senha incorretos." }, 401);
    }
    if (sessaoExpirada) return json({ mensagem: "Entre de novo para continuar." }, 401);
    if (p === "/idosos/1") return json({ id: 1, nome: "Maria Helena Silva", semLeitura: false });
    if (p === "/idosos/1/rede-apoio") return json([{ idFamiliar: 1, parentesco: "filho", ordemContato: 1 }]);
    if (p === "/familiares/1") return json({ id: 1, nome: "Rafael Silva" });
    if (p === "/idosos/1/lembretes") return json([{ id: 5, tipo: "REMEDIO", descricao: "Remédio da pressão", dataAgendamento: "2030-01-01T14:00:00", status: "PENDENTE" }]);
    if (p === "/guias") return json([
      { id: 1, nome: "Pedir aposentadoria", aplicativo: "Meu INSS", status: "ATIVO" },
      { id: 5, nome: "Recuperar senha", aplicativo: "gov.br", status: "INATIVO" },
    ]);
    if (p === "/guias/1/passos") return json([
      { ordem: 1, elemento: "Aplicativo Meu INSS", instrucao: "Abra o aplicativo Meu INSS." },
      { ordem: 2, elemento: "Entrar", instrucao: "Entre com a conta gov.br." },
    ]);
    if (p === "/tarefas" && req.method() === "POST") {
      if (corpo.pedido && !/rafael|filho/i.test(corpo.pedido)) {
        return json({ mensagem: "Nao encontrei 'Joana' na sua rede de apoio. Contatos: Rafael Silva." }, 404);
      }
      passo = 0;
      return json({ id: 77 }, 201);
    }
    if (p === "/tarefas/77/passo-atual") {
      const passos = [["Aplicativo Meu INSS", "Abra o aplicativo Meu INSS."], ["Entrar", "Entre com a conta gov.br."]];
      return json({ nmPasElemento: passos[passo][0], txPasInstrucao: passos[passo][1] });
    }
    if (p === "/tarefas/77/avancar") { passo += 1; return json({ concluido: passo >= 2 }); }
    if (p === "/voz") return route.fulfill({ status: 204, body: "" }); // sem voz natural: o app usa a do aparelho
    if (p === "/conversa") {
      if (/socorro/i.test(corpo.texto)) return json({ resposta: "Vou abrir a emergência agora.", acao: "EMERGENCIA" });
      if (/consulta/i.test(corpo.texto)) return json({ resposta: "Sua consulta com o clínico é dia 25 de março, às 14h.", acao: "LEMBRETES", ia: true });
      if (/beijo/i.test(corpo.texto)) return json({ resposta: "Que carinho! Preparo a mensagem para o Rafael.", acao: "FAZ_PRA_MIM", pedido: "Manda mensagem pro Rafael dizendo um beijo", ia: true });
      return json({ resposta: "Oi, Maria! Estou aqui com você.", acao: "NENHUMA", ia: true });
    }
    if (p === "/golpes/verificar") return json({ nivel: "ALTO", motivo: "Pede Pix com urgência.", oQueFazer: "Não envie dinheiro." });
    if (p === "/golpes/verificar-imagem") return json({ nivel: "MEDIO", motivo: "O print pede para clicar em um link.", oQueFazer: "Confirme com a família.", imagemAnalisada: true });
    if (p === "/emergencias") return json({ contatos: [{ nome: "Rafael Silva", ordem: 1 }], ligar: "192 - SAMU" });
    if (p === "/idosos/1/historico") return json({ tarefas: [], alertas: [], atendimentos: [] });
    return route.fulfill({ status: 204, body: "" });
  });
  return chamadas;
}

// O navegador de teste não tem microfone: simula um navegador sem ditado por voz.
async function semDitadoPorVoz(page) {
  await page.addInitScript(() => {
    delete window.SpeechRecognition;
    delete window.webkitSpeechRecognition;
  });
}

async function entrar(page) {
  await page.goto("/");
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page.getByRole("heading", { name: "Oi, Maria!" })).toBeVisible();
}

test("login certo entra e manda o token nas chamadas seguintes", async ({ page }) => {
  const chamadas = await simularApi(page);
  await entrar(page);
  await expect(page.getByText("Remédio da pressão")).toBeVisible();
  const depois = chamadas.filter((c) => c.caminho !== "/auth/login");
  expect(depois.length).toBeGreaterThan(0);
  for (const c of depois) expect(c.auth).toBe("Bearer token-teste");
});

test("login com senha errada mostra a mensagem da API", async ({ page }) => {
  await simularApi(page);
  await page.goto("/");
  await page.getByLabel("Senha").fill("000000");
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page.getByRole("status")).toContainText("Usuario ou senha incorretos.");
  await expect(page.getByRole("heading", { name: "Entrar" })).toBeVisible();
});

test("sessão expirada volta para o login", async ({ page }) => {
  await simularApi(page, { sessaoExpirada: true });
  await page.addInitScript(() => {
    localStorage.setItem("netinho-token", "velho");
    localStorage.setItem("netinho-idoso-id", "1");
  });
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Entrar" })).toBeVisible();
  await expect(page.getByRole("status")).toContainText("Sua sessão terminou");
});

test("API fora do ar mostra a faixa de sem conexão", async ({ page }) => {
  await simularApi(page, { fora: true });
  await page.addInitScript(() => {
    localStorage.setItem("netinho-token", "token-teste");
    localStorage.setItem("netinho-idoso-id", "1");
  });
  await page.goto("/");
  await expect(page.getByRole("alert")).toContainText("Sem conexão");
});

test("Faz pra mim: escrever, confirmar e desfazer em 5 segundos", async ({ page }) => {
  const chamadas = await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "Faz pra mim" }).click();
  await page.getByLabel("Pode escrever ou falar").fill("Manda mensagem pro Rafael dizendo que chego às 3");
  await page.getByRole("button", { name: "Continuar" }).click();
  await expect(page.getByRole("heading", { name: "Mando para Rafael?" })).toBeVisible();
  await page.getByRole("button", { name: "Sim" }).click();
  await expect(page.getByRole("button", { name: /Desfazer \(5\)/ })).toBeVisible();
  await page.getByRole("button", { name: /Desfazer/ }).click();
  await expect(page.getByRole("status")).toContainText("Tarefa desfeita");
  expect(chamadas.some((c) => c.caminho === "/tarefas/77/desfazer")).toBe(true);
});

test("Faz pra mim: contato desconhecido mostra aviso e deixa escolher o contato", async ({ page }) => {
  const chamadas = await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "Faz pra mim" }).click();
  await page.getByLabel("Pode escrever ou falar").fill("Manda mensagem pra Joana dizendo oi");
  await page.getByRole("button", { name: "Continuar" }).click();
  await expect(page.getByRole("alert")).toContainText("Não achei essa pessoa");
  await expect(page.getByText("Não consegui conectar.")).toHaveCount(0);
  await page.getByLabel("Pode escrever ou falar").fill("Manda mensagem pra Joana dizendo oi!");
  await expect(page.getByRole("alert")).toHaveCount(0);
  await page.getByRole("button", { name: /Rafael/ }).click();
  await expect(page.getByLabel("Pode escrever ou falar")).toHaveValue("Manda mensagem pra Rafael dizendo oi!");
  await page.getByRole("button", { name: "Continuar" }).click();
  await expect(page.getByRole("heading", { name: "Mando para Rafael?" })).toBeVisible();
  expect(chamadas.filter((c) => c.caminho === "/tarefas").length).toBe(2);
});

test("Faz pra mim: entende o contato sem pra/para e mostra a prévia certa", async ({ page }) => {
  await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "Faz pra mim" }).click();
  await page.getByLabel("Pode escrever ou falar").fill("Avisa o meu filho que cheguei bem");
  await expect(page.locator("[data-preview-contact]")).toHaveText("Rafael");
  await expect(page.locator("[data-preview-message]")).toHaveText("Cheguei bem");
  await expect(page.getByRole("button", { name: /Rafael/ })).toHaveAttribute("aria-pressed", "true");
  await page.getByRole("button", { name: "Continuar" }).click();
  await expect(page.getByRole("heading", { name: "Mando para Rafael?" })).toBeVisible();
  await page.getByRole("button", { name: "Sim" }).click();
  await expect(page.getByRole("heading", { name: "Pronto! Rafael já recebeu." })).toBeVisible();
});

test("Netinho com IA: o microfone abre a conversa sem trocar de tela", async ({ page }) => {
  await semDitadoPorVoz(page);
  const chamadas = await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "Falar com o Netinho" }).click();
  const conversa = page.getByRole("dialog", { name: /Conversa com o Netinho/ });
  await expect(conversa).toBeVisible();
  await expect(page.getByRole("heading", { name: "Oi, Maria!" })).toBeAttached(); // continua no início
  await conversa.getByLabel("Escreva para o Netinho").fill("que dia é minha consulta?");
  await conversa.getByRole("button", { name: "Enviar" }).click();
  await expect(conversa.getByText("Sua consulta com o clínico é dia 25 de março, às 14h.")).toBeVisible();
  // manda a conversa anterior junto, para o Netinho lembrar do assunto
  await conversa.getByLabel("Escreva para o Netinho").fill("e quero mandar um beijo pro meu filho");
  await conversa.getByRole("button", { name: "Enviar" }).click();
  await expect(conversa.getByText(/Preparo a mensagem para o Rafael/)).toBeVisible();
  const ultima = chamadas.filter((c) => c.caminho === "/conversa").pop();
  expect(ultima.corpo.historico.map((h) => h.papel)).toEqual(["user", "assistant"]);
  await expect(conversa.getByRole("button", { name: "Ouvir de novo" })).toHaveCount(1); // só na última resposta
  // a resposta vai para a voz natural do servidor, já limpa para ser falada ("14h" vira "14 horas")
  await expect.poll(() => chamadas.filter((c) => c.caminho === "/voz").length).toBeGreaterThan(0);
  expect(chamadas.find((c) => c.caminho === "/voz").corpo.texto).toBe("Sua consulta com o clínico é dia 25 de março, às 14 horas.");
  await conversa.getByRole("button", { name: "Ouvir de novo" }).click();
  await conversa.getByRole("button", { name: "Abrir Faz pra mim" }).click();
  await expect(conversa).toHaveCount(0);
  await expect(page.getByLabel("Pode escrever ou falar")).toHaveValue("Manda mensagem pro Rafael dizendo um beijo");
});

test("Netinho com IA: emergência abre direto e Esc fecha a conversa", async ({ page }) => {
  await semDitadoPorVoz(page);
  await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "Falar com o Netinho" }).click();
  const conversa = page.getByRole("dialog", { name: /Conversa com o Netinho/ });
  await page.keyboard.press("Escape");
  await expect(conversa).toHaveCount(0);
  await page.getByRole("button", { name: "Falar com o Netinho" }).click();
  await conversa.getByLabel("Escreva para o Netinho").fill("socorro");
  await conversa.getByRole("button", { name: "Enviar" }).click();
  await expect(page.locator("#hold")).toBeVisible();
  await expect(conversa).toHaveCount(0);
});

test("Me ensina: só guias ativos, passo a passo até concluir", async ({ page }) => {
  const chamadas = await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "Me ensina" }).click();
  await expect(page.getByRole("button", { name: /Pedir aposentadoria/ })).toBeVisible();
  await expect(page.getByText("Recuperar senha")).toHaveCount(0);
  await page.getByRole("button", { name: /Pedir aposentadoria/ }).click();
  expect(chamadas.find((c) => c.caminho === "/tarefas").corpo).toEqual({ idIdoso: 1, idGuia: 1 });
  await expect(page.getByText("Passo 1 de 2")).toBeVisible();
  await page.getByRole("button", { name: "Consegui" }).click();
  await expect(page.getByText("Passo 2 de 2")).toBeVisible();
  await page.getByRole("button", { name: "Consegui" }).click();
  await expect(page.getByRole("heading", { name: "Você conseguiu! Fez tudo sozinha." })).toBeVisible();
});

test("É golpe? com texto mostra risco ALTO em fundo vermelho", async ({ page }) => {
  await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "É golpe?" }).click();
  await page.getByLabel("Cole ou escreva a mensagem").fill("Mãe, troquei de número, me manda um Pix");
  await page.getByRole("button", { name: "Verificar" }).click();
  await expect(page.getByText("Risco ALTO")).toBeVisible();
  await expect(page.locator("#app")).toHaveAttribute("data-tone", "red");
});

test("É golpe? com foto envia a imagem em JPEG para análise", async ({ page }) => {
  const chamadas = await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "É golpe?" }).click();
  await page.locator("#scam-img").setInputFiles({ name: "print.png", mimeType: "image/png", buffer: PNG_1PX });
  await expect(page.getByText("Imagem anexada")).toBeVisible();
  await page.getByRole("button", { name: "Verificar" }).click();
  await expect(page.getByText("Risco MÉDIO")).toBeVisible();
  const envio = chamadas.find((c) => c.caminho === "/golpes/verificar-imagem");
  expect(envio.corpo.tipoImagem).toBe("image/jpeg");
  expect(Buffer.from(envio.corpo.imagemBase64, "base64").subarray(0, 2).toString("hex")).toBe("ffd8");
});

test("Emergência: segurar 2 segundos registra o pedido", async ({ page }) => {
  const chamadas = await simularApi(page);
  await entrar(page);
  await page.getByRole("button", { name: "Emergência" }).click();
  const botao = page.locator("#hold");
  await expect(botao).toBeVisible();
  await page.waitForTimeout(400); // espera a animação de entrada da tela
  const caixa = await botao.boundingBox();
  await page.mouse.move(caixa.x + caixa.width / 2, caixa.y + caixa.height / 2);
  await page.mouse.down();
  await page.waitForTimeout(2300);
  await page.mouse.up();
  await expect(botao).toContainText("Pedido registrado");
  expect(chamadas.some((c) => c.caminho === "/emergencias")).toBe(true);
});

test("nenhuma tela rola para o lado em 360 px", async ({ page }, info) => {
  await simularApi(page);
  await page.setViewportSize({ width: 360, height: 760 });
  await entrar(page);
  const telas = ["Me ensina", "Faz pra mim", "É golpe?", "Pedir ajuda", "Emergência"];
  for (const tela of telas) {
    await page.getByRole("button", { name: tela, exact: true }).first().click();
    await page.waitForTimeout(350);
    const larguraExtra = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(larguraExtra, `rolagem lateral em ${tela}`).toBeLessThanOrEqual(0);
    await page.screenshot({ path: info.outputPath(`${tela.replace(/\W+/g, "-")}.png`), fullPage: true });
    await page.getByRole("button", { name: "Início", exact: true }).click();
  }
});
