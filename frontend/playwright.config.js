// Testes de tela do Netinho: rodam contra os arquivos estáticos com a API simulada no próprio teste.
// Rodar sem instalar nada (Docker):
//   docker run --rm -v "$PWD":/work -w /work mcr.microsoft.com/playwright:v1.64.1-noble \
//     sh -c "npm install --no-audit --no-fund && npx playwright test"
const { defineConfig, devices } = require("@playwright/test");

module.exports = defineConfig({
  testDir: "tests",
  timeout: 30_000,
  fullyParallel: true,
  reporter: [["list"]],
  use: {
    baseURL: "http://127.0.0.1:4173",
    ...devices["Pixel 7"],
    locale: "pt-BR",
  },
  projects: [
    { name: "claro", use: { colorScheme: "light" } },
    { name: "escuro", use: { colorScheme: "dark" } },
  ],
  webServer: {
    command: "node tests/servidor.js",
    url: "http://127.0.0.1:4173",
    reuseExistingServer: true,
  },
});
