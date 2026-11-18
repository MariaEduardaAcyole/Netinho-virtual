// Teste de fumaça contra um Netinho de verdade (site + API + banco).
// Só roda quando NETINHO_URL está definido, por exemplo:
//   NETINHO_URL=https://netinho.painelorg.tech npx playwright test tests/real.spec.js --project=claro
// Ele faz login com a conta de demonstração, abre um guia e verifica um texto no "É golpe?"
// (isso grava um alerta no histórico da conta de demonstração).
const { test, expect } = require("@playwright/test");

const URL_REAL = process.env.NETINHO_URL;
test.skip(!URL_REAL, "defina NETINHO_URL para rodar contra um ambiente real");

test("fumaça: login, guia e É golpe? na API real", async ({ page }) => {
  await page.goto(URL_REAL);
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page.getByRole("heading", { name: /^Oi, / })).toBeVisible({ timeout: 15_000 });
  await expect(page.getByRole("alert")).toHaveCount(0); // sem faixa "Sem conexão"

  await page.getByRole("button", { name: "Me ensina" }).click();
  await page.getByRole("button", { name: /Agendar consulta/ }).click();
  await expect(page.getByText(/Passo 1 de \d+/)).toBeVisible();

  await page.getByRole("button", { name: "Início", exact: true }).click();
  await page.getByRole("button", { name: "É golpe?" }).click();
  await page.getByLabel("Cole ou escreva a mensagem").fill("Mãe, troquei de número. Me manda um Pix agora, é urgente!");
  await page.getByRole("button", { name: "Verificar" }).click();
  await expect(page.getByText("Risco ALTO")).toBeVisible({ timeout: 30_000 });
});
