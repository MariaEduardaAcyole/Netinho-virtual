// Servidor estático mínimo para os testes (sem dependências).
const http = require("http");
const fs = require("fs");
const path = require("path");

const raiz = path.join(__dirname, "..");
const tipos = { ".html": "text/html", ".js": "application/javascript", ".css": "text/css", ".svg": "image/svg+xml" };

http.createServer((req, res) => {
  const caminho = decodeURIComponent(new URL(req.url, "http://x").pathname);
  const arquivo = path.join(raiz, caminho === "/" ? "index.html" : caminho);
  if (!arquivo.startsWith(raiz) || !fs.existsSync(arquivo) || fs.statSync(arquivo).isDirectory()) {
    res.writeHead(404);
    return res.end();
  }
  res.writeHead(200, { "Content-Type": tipos[path.extname(arquivo)] || "application/octet-stream" });
  fs.createReadStream(arquivo).pipe(res);
}).listen(4173, "127.0.0.1");
