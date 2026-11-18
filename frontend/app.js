// Controla os estados/telas do frontend, chama a API e gerencia entrada e leitura por voz.
const app = document.querySelector("#app");
const overlay = document.querySelector("#overlay");
const toastElement = document.querySelector("#toast");

/* ---------------------------------------------------------------- Ícones */

const iconPaths = {
  back: '<path d="M19 12H5M12 19l-7-7 7-7"/>',
  speaker: '<path d="M11 5 6 9H3v6h3l5 4V5ZM15.5 8.5a5 5 0 0 1 0 7m3-10a9 9 0 0 1 0 13"/>',
  sliders: '<path d="M4 6h9M19 6h1M4 12h1M11 12h9M4 18h9M19 18h1"/><circle cx="16" cy="6" r="2.5"/><circle cx="8" cy="12" r="2.5"/><circle cx="16" cy="18" r="2.5"/>',
  book: '<path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v16H6.5A2.5 2.5 0 0 0 4 21V5.5ZM4 17a2.5 2.5 0 0 1 2.5-2.5H20M8 7h8"/>',
  message: '<path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5H5l1.8-3A7.5 7.5 0 1 1 20 11.5Z"/><path d="M8 11h.01M12 11h.01M16 11h.01"/>',
  shield: '<path d="M12 22s8-4 8-11V5l-8-3-8 3v6c0 7 8 11 8 11Z"/><path d="M12 8v4m0 4h.01"/>',
  users: '<path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2m6-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm6-7.8a4 4 0 0 1 0 7.6M20 21v-2a4 4 0 0 0-3-3.9"/>',
  mic: '<rect x="9" y="2" width="6" height="13" rx="3"/><path d="M5 10v2a7 7 0 0 0 14 0v-2m-7 9v3m-4 0h8"/>',
  phone: '<path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.4 19.4 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7l.5 3a2 2 0 0 1-.6 1.8L7.1 10.4a16 16 0 0 0 6 6l1.9-1.9a2 2 0 0 1 1.8-.6l3 .5a2 2 0 0 1 2.2 2.5Z"/>',
  check: '<path d="m5 12 4 4L19 6"/>',
  home: '<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-6v-7h-4v7H4a1 1 0 0 1-1-1V10Z"/>',
  close: '<path d="m18 6-12 12M6 6l12 12"/>',
  arrow: '<path d="M5 12h14m-7-7 7 7-7 7"/>',
  chevron: '<path d="m9 6 6 6-6 6"/>',
  question: '<circle cx="12" cy="12" r="10"/><path d="M9.6 9a2.5 2.5 0 1 1 4.6 1.4c-.9 1.1-2.2 1.4-2.2 3.1m0 3h.01"/>',
  bell: '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.94 1.94 0 0 0 3.4 0"/>',
  camera: '<path d="M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3Z"/><circle cx="12" cy="13" r="3"/>',
  alert: '<path d="m21.7 18-8-14a2 2 0 0 0-3.5 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.7-3ZM12 9v4m0 4h.01"/>',
};

function icon(name, strokeWidth = 2.2) {
  return `<svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">${iconPaths[name]}</svg>`;
}

/* ---------------------------------------------------------------- Dados fixos */

const SCREEN_LABELS = {
  make: "Faz pra mim", confirm: "Faz pra mim", sent: "Faz pra mim",
  guideList: "Me ensina", guide: "Me ensina", guideDone: "Me ensina",
  scam: "É golpe?", scamResult: "É golpe?",
  help: "Pedir ajuda", emergency: "Emergência",
  profile: "Meu perfil", settings: "Ajustes",
  reminders: "Lembretes", history: "Histórico",
};

const HISTORY_DOTS = {
  make: "#2d5be6", scam: "#ef6a1a", guide: "#ffc227", help: "#0b8250", emergency: "#d3261a", reminder: "#e8106e",
};

// O backend só aceita desfazer até 5 s depois do envio (TarefaBO.desfazer).
const UNDO_SECONDS = 5;

const CONTACT_PALETTE = [["#dce5ff", "#2d5be6"], ["#ffe0ee", "#e8106e"]];
const SAMPLE_PORTRAITS = { rafael: "assets/rafael.svg", beatriz: "assets/beatriz.svg" };

/* ---------------------------------------------------------------- Estado */

function readStorage(key, fallback) {
  try {
    const value = localStorage.getItem(key);
    return value === null ? fallback : value;
  } catch {
    return fallback;
  }
}

function writeStorage(key, value) {
  try {
    if (value === null) localStorage.removeItem(key);
    else localStorage.setItem(key, value);
  } catch {
    /* armazenamento indisponível: segue só em memória */
  }
}

function readHistory() {
  try {
    const list = JSON.parse(readStorage("netinho-history", "[]"));
    return Array.isArray(list) ? list.filter((item) => item?.type === "emergency") : [];
  } catch {
    return [];
  }
}

// Em desenvolvimento a API roda em localhost:8080; publicada, fica no mesmo domínio em /api.
function defaultApiBase() {
  const { protocol, hostname, origin } = window.location;
  const local = protocol === "file:" || ["localhost", "127.0.0.1", "[::1]", ""].includes(hostname);
  return local ? "http://localhost:8080" : `${origin}/api`;
}

// A sessão é o token devolvido por POST /auth/login; sem token, a pessoa começa no login.
const savedToken = readStorage("netinho-token", "");

const state = {
  screen: savedToken ? "home" : "login",
  token: savedToken,
  // Conta de demonstração mostrada na tela de login (a senha é conferida pela API).
  demoUser: "netinho",
  demoPassword: "123456",
  user: readStorage("netinho-user", "netinho"),
  apiBase: readStorage("netinho-api-url", defaultApiBase()),
  idosoId: Number(readStorage("netinho-idoso-id", "0")) || null,
  idosoNome: "Maria Helena",
  silentMode: false,
  apiConnected: null, // null = ainda não testado
  supportContacts: [],
  reminders: [],
  history: null, // null = carregando
  historyError: false,
  localHistory: readHistory(), // só emergências: a API não guarda esse registro

  taskText: "",
  taskError: "",
  draft: null,
  undoLeft: 0,

  guides: null, // null = carregando; [] = nenhum
  guidesError: false,
  guide: null,
  guideStep: 0,
  guideTaskId: null,
  apiStep: null,

  scamText: "",
  scamImg: "",
  scamFile: null,
  busy: "",
  scam: null,

  hold: "idle", // idle | holding | done

  listening: false,
  heard: "",
  error: "",
};

let lastRenderedScreen = null;
let undoTimer = null;
let holdTimer = null;
let recognition = null;

/* ---------------------------------------------------------------- Utilidades */

const escapeHtml = (value) =>
  String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;",
  })[character]);

function showToast(message) {
  toastElement.textContent = message;
  toastElement.classList.add("visible");
  window.clearTimeout(showToast.timeoutId);
  showToast.timeoutId = window.setTimeout(() => toastElement.classList.remove("visible"), 3200);
}

function firstName(name) {
  return String(name || "").trim().split(/\s+/)[0] || "";
}

function hasNav() {
  return state.screen !== "login" && state.screen !== "home";
}

// Guarda no aparelho só o que a API não registra (pedido de emergência).
function logLocalHistory(type, text) {
  state.localHistory = [{ type, text, at: new Date().toISOString() }, ...state.localHistory].slice(0, 30);
  writeStorage("netinho-history", JSON.stringify(state.localHistory));
}

function dayLabel(date) {
  const today = new Date();
  const start = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  const diff = Math.round((start(date) - start(today)) / 86400000);
  if (diff === 0) return "Hoje";
  if (diff === 1) return "Amanhã";
  if (diff === -1) return "Ontem";
  return date.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
}

function timeLabel(date) {
  return date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

/* ---------------------------------------------------------------- API */

class SessionExpiredError extends Error {}

// A API respondeu, mas recusou o pedido (contato não encontrado, Pix, senha...). Não é falta de conexão.
class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

async function apiRequest(path, { method = "GET", body } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  let response;
  try {
    response = await fetch(`${state.apiBase.replace(/\/+$/, "")}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch (error) {
    state.apiConnected = false;
    throw error;
  }
  // 502/503/504 vêm do proxy quando a API está fora do ar: é falta de conexão, não resposta da API.
  if ([502, 503, 504].includes(response.status)) {
    state.apiConnected = false;
    throw new TypeError("API indisponível");
  }
  state.apiConnected = true;
  const text = response.status === 204 ? "" : await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }
  if (response.status === 401 && path !== "/auth/login") {
    endSession("Sua sessão terminou. Entre de novo, por favor.");
    throw new SessionExpiredError("Sessão expirada");
  }
  if (!response.ok) {
    throw new ApiError(data?.mensagem || `A API respondeu com erro ${response.status}.`, response.status);
  }
  return data;
}

function startSession(login) {
  state.token = login.token;
  state.idosoId = login.idIdoso;
  state.idosoNome = login.nome || state.idosoNome;
  writeStorage("netinho-token", login.token);
  writeStorage("netinho-idoso-id", String(login.idIdoso));
}

function endSession(message) {
  state.token = "";
  state.idosoId = null;
  state.supportContacts = [];
  state.reminders = [];
  state.history = null;
  state.error = "";
  writeStorage("netinho-token", null);
  writeStorage("netinho-idoso-id", null);
  go("login");
  if (message) showToast(message);
}

function friendlyError(error, offlineMessage) {
  if (error instanceof TypeError) return offlineMessage;
  return `A API respondeu: ${error.message}`;
}

// Mostra o painel "Não consegui conectar." com uma mensagem adequada à ação que falhou.
function showApiError(error, offlineMessage = "Não consegui falar com a API. Verifique se o backend está ligado.") {
  if (error instanceof SessionExpiredError) return;
  state.error = friendlyError(error, offlineMessage);
  render();
}

async function loadElder() {
  const elder = await apiRequest(`/idosos/${state.idosoId}`);
  state.idosoNome = elder.nome || state.idosoNome;
  state.silentMode = Boolean(elder.semLeitura);
}

async function loadSupportNetwork() {
  const links = await apiRequest(`/idosos/${state.idosoId}/rede-apoio`);
  const contacts = await Promise.all(links.map(async (link, index) => {
    const familiar = await apiRequest(`/familiares/${link.idFamiliar}`);
    const nome = familiar.nome || "Contato";
    const key = firstName(nome).toLowerCase();
    return {
      nome,
      parentesco: link.parentesco || "rede de apoio",
      ordem: link.ordemContato ?? index + 1,
      photo: SAMPLE_PORTRAITS[key] || "",
      bg: CONTACT_PALETTE[index % 2][0],
      ink: CONTACT_PALETTE[index % 2][1],
    };
  }));
  state.supportContacts = contacts.sort((a, b) => a.ordem - b.ordem);
}

const REMINDER_TYPES = { REMEDIO: "Remédio", CONSULTA: "Consulta", VACINA: "Vacina" };

async function loadReminders() {
  const list = await apiRequest(`/idosos/${state.idosoId}/lembretes`);
  state.reminders = list
    .filter((item) => item.status !== "CANCELADO")
    .map((item) => {
      const date = new Date(item.dataAgendamento);
      return {
        raw: item,
        id: item.id,
        date,
        quando: dayLabel(date),
        hora: timeLabel(date),
        titulo: item.descricao,
        tipo: REMINDER_TYPES[item.tipo] || "Lembrete",
        done: item.status === "CUMPRIDO" || Boolean(item.dataCumprimento),
      };
    })
    .sort((a, b) => a.date - b.date);
}

async function loadGuides() {
  const guides = await apiRequest("/guias");
  const active = (guides || []).filter((guide) => guide.status === "ATIVO");
  const withSteps = await Promise.all(active.map(async (guide) => {
    const steps = await apiRequest(`/guias/${guide.id}/passos`);
    return {
      id: guide.id,
      title: guide.nome,
      app: guide.aplicativo || "",
      steps: (steps || []).sort((a, b) => a.ordem - b.ordem),
    };
  }));
  state.guides = withSteps.filter((guide) => guide.steps.length > 0);
}

const TASK_STATUS_TEXT = {
  FAZ_PRA_MIM: {
    CONCLUIDA: "Pedido feito",
    AGUARDANDO: "Pedido esperando confirmação",
    CANCELADA: "Pedido cancelado",
    RECUSADA: "Pedido recusado por segurança",
    EM_ANDAMENTO: "Pedido em andamento",
  },
  ME_ENSINA: {
    CONCLUIDA: "Guia concluído",
    EM_ANDAMENTO: "Guia começado",
    CANCELADA: "Guia cancelado",
    RECUSADA: "Guia recusado",
    AGUARDANDO: "Guia começado",
  },
};
const HELP_STATUS_TEXT = {
  SOLICITADO: "Pedido de ajuda registrado",
  EM_ANDAMENTO: "Ajuda em andamento",
  CONCLUIDO: "Ajuda concluída",
  CANCELADO: "Pedido de ajuda cancelado",
};

function shorten(text, max = 60) {
  const value = String(text || "").trim();
  return value.length > max ? `${value.slice(0, max - 1)}…` : value;
}

function guideNameFromTask(task) {
  const guide = state.guides?.find((item) => item.id === task.idGuia);
  return guide ? guide.title : String(task.pedido || "").replace(/^Quero aprender:\s*/i, "");
}

// Junta tarefas, alertas, atendimentos e lembretes cumpridos em uma linha do tempo.
async function loadHistory() {
  const [data, reminders] = await Promise.all([
    apiRequest(`/idosos/${state.idosoId}/historico`),
    apiRequest(`/idosos/${state.idosoId}/lembretes`).catch(() => []),
  ]);
  const items = [];
  (data?.tarefas || []).forEach((task) => {
    const guide = task.modo === "ME_ENSINA";
    const label = TASK_STATUS_TEXT[task.modo]?.[task.status] || "Pedido";
    items.push({
      type: guide ? "guide" : "make",
      text: `${label}: ${shorten(guide ? guideNameFromTask(task) : task.pedido)}`,
      at: task.dataConclusao || task.dataSolicitacao,
    });
  });
  (data?.alertas || []).forEach((alert) => {
    items.push({
      type: "scam",
      text: `Mensagem verificada: risco ${alert.risco === "MEDIO" ? "MÉDIO" : alert.risco}`,
      at: alert.dataRegistro,
    });
  });
  (data?.atendimentos || []).forEach((help) => {
    items.push({
      type: "help",
      text: HELP_STATUS_TEXT[help.status] || "Pedido de ajuda",
      at: help.dataFim || help.dataInicio || help.dataSolicitacao,
    });
  });
  (reminders || []).filter((item) => item.dataCumprimento).forEach((item) => {
    items.push({ type: "reminder", text: `Lembrete feito: ${shorten(item.descricao)}`, at: item.dataCumprimento });
  });
  state.history = [...items, ...state.localHistory]
    .filter((item) => item.at)
    .sort((a, b) => new Date(b.at) - new Date(a.at))
    .slice(0, 40);
}

async function loadAll() {
  const results = await Promise.allSettled([loadElder(), loadSupportNetwork(), loadReminders()]);
  return results[0].status === "fulfilled";
}

/* ---------------------------------------------------------------- Voz */

function speak(text, queue = false) {
  if (!("speechSynthesis" in window)) {
    showToast("A leitura em voz alta não está disponível neste navegador.");
    return;
  }
  if (!queue) window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "pt-BR";
  utterance.rate = 0.88;
  window.speechSynthesis.speak(utterance);
}

function speakScreen(queue = false) {
  const text = Array.from(app.querySelectorAll("section h1, section p, section label, section dt, section dd"))
    .map((node) => node.textContent.trim().replace(/\s+/g, " "))
    .filter(Boolean)
    .join(". ");
  if (text) speak(text, queue);
}

function speechErrorMessage(error) {
  const messages = {
    "not-allowed": "Microfone bloqueado. Permita o acesso ao microfone no navegador e tente novamente.",
    "service-not-allowed": "O navegador não permitiu usar o serviço de voz. Você pode digitar o pedido.",
    "audio-capture": "Não encontrei um microfone disponível. Verifique o dispositivo e tente novamente.",
    "no-speech": "Não ouvi nada. Fale mais perto do microfone ou digite o pedido.",
    network: "Falha na conexão do serviço de voz. Verifique a internet ou digite o pedido.",
    "language-not-supported": "O ditado em português não está disponível. Você pode digitar o pedido.",
  };
  return messages[error] || "Não consegui ouvir. Verifique a permissão do microfone ou digite o pedido.";
}

// Decide o que fazer com a frase falada: preenche o campo atual ou escolhe a função.
function routeSpeech(text) {
  const lower = text.toLowerCase();
  if (state.screen === "scam") {
    state.scamText = text;
    return go("scam", { keepScam: true });
  }
  if (state.screen === "make") {
    state.taskText = text;
    return render();
  }
  if (/emerg|socorro/.test(lower)) return go("emergency");
  if (/golpe|suspeit|estranh/.test(lower)) return go("scam");
  if (/ensina|aprend|aposentadoria|como (eu )?fa/.test(lower)) return go("guideList");
  if (/ajuda|chamar algu/.test(lower)) return go("help");
  state.taskText = text;
  go("make");
}

function stopListening() {
  try {
    recognition?.abort();
  } catch {
    /* já parado */
  }
  recognition = null;
  state.listening = false;
  render();
}

function beginListening() {
  if (state.listening) return;
  const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!Recognition) {
    showToast("Ditado por voz indisponível neste navegador. Você pode escrever o pedido.");
    return;
  }

  try {
    recognition = new Recognition();
  } catch {
    showToast("Não foi possível iniciar o ditado. Você pode escrever o pedido.");
    return;
  }
  const current = recognition;
  current.lang = "pt-BR";
  current.interimResults = true;
  current.maxAlternatives = 1;
  let finished = false;

  current.onresult = (event) => {
    if (recognition !== current) return;
    const text = Array.from(event.results).map((result) => result[0].transcript).join(" ").trim();
    state.heard = text;
    const heardElement = overlay.querySelector("[data-heard]");
    if (heardElement) heardElement.textContent = text;
    if (event.results[event.results.length - 1].isFinal) {
      finished = true;
      recognition = null;
      state.listening = false;
      if (text) routeSpeech(text);
      else render();
    }
  };
  current.onerror = (event) => {
    if (recognition !== current) return;
    finished = true;
    recognition = null;
    state.listening = false;
    render();
    if (event.error !== "aborted") showToast(speechErrorMessage(event.error));
  };
  current.onend = () => {
    if (recognition !== current) return;
    recognition = null;
    if (!finished) {
      state.listening = false;
      render();
      showToast("Não ouvi nada. Tente falar novamente ou digite o pedido.");
    }
  };

  state.listening = true;
  state.heard = "";
  render();
  try {
    current.start();
  } catch {
    recognition = null;
    state.listening = false;
    render();
    showToast("Não foi possível iniciar o ditado. Verifique o microfone ou digite o pedido.");
  }
}

/* ---------------------------------------------------------------- Peças comuns */

function topbar() {
  return `
    <div class="topbar">
      <button class="round-btn bar" data-action="home" aria-label="Voltar ao início">${icon("back", 2.4)}</button>
      <span class="topbar-label">${escapeHtml(SCREEN_LABELS[state.screen] || "")}</span>
      <button class="round-btn bar" data-action="speak" aria-label="Ouvir esta tela">${icon("speaker")}</button>
    </div>`;
}

function bottomNav() {
  return `
    <nav class="bottom-nav" aria-label="Navegação">
      <button class="nav-btn" data-action="home">${icon("home")}Início</button>
      <button class="nav-mic" data-action="listen" aria-label="Falar com o Netinho">${icon("mic")}</button>
      <button class="nav-btn" data-action="help">${icon("users")}Ajuda</button>
    </nav>`;
}

function offlineBanner() {
  return `
    <div class="offline-banner" role="alert">${icon("alert")}
      Sem conexão. Algumas funções não vão funcionar agora.</div>`;
}

function portrait(contact, small = false) {
  const style = contact.photo
    ? `background-color:${contact.bg};background-image:url(${contact.photo})`
    : `background-color:${contact.bg};color:${contact.ink}`;
  return `<span class="portrait${small ? " sm" : ""}" style="${style}" aria-hidden="true">${contact.photo ? "" : escapeHtml(contact.nome[0]?.toUpperCase() || "?")}</span>`;
}

/* ---------------------------------------------------------------- Telas */

function renderLogin() {
  return `
    <section class="screen login">
      <div class="login-brand">
        <span class="brand-mark" aria-hidden="true">N</span>
        <span class="brand-name">Netinho Virtual</span>
        <span class="brand-tagline">Seu assistente com paciência de neto.</span>
      </div>
      <form id="login-form" class="login-card">
        <h1>Entrar</h1>
        <p class="sub">Acesse o protótipo do assistente digital.</p>
        <label class="label small" for="login-user">Usuário</label>
        <input class="field" id="login-user" name="username" type="text" autocomplete="username" value="${escapeHtml(state.demoUser)}" required>
        <label class="label small" for="login-password">Senha</label>
        <input class="field" id="login-password" name="password" type="password" autocomplete="current-password" value="${escapeHtml(state.demoPassword)}" required>
        <div class="demo-box" aria-label="Dados de demonstração">
          <strong>Acesso de demonstração</strong>
          <span>Usuário: <b>${escapeHtml(state.demoUser)}</b></span>
          <span>Senha: <b>${escapeHtml(state.demoPassword)}</b></span>
        </div>
        <button class="btn btn-pink" type="submit" ${state.busy === "login" ? "disabled" : ""}>${state.busy === "login" ? "Entrando…" : "Entrar"}</button>
      </form>
      <p class="login-warning">Conta de demonstração. A senha é conferida pelo servidor do Netinho.</p>
    </section>`;
}

function nextReminder() {
  return state.reminders.find((reminder) => !reminder.done);
}

function renderHome() {
  const next = nextReminder();
  return `
    <section class="screen home">
      <div class="home-head">
        <button class="avatar-btn" data-action="profile" aria-label="Meu perfil"></button>
        <span class="home-who"><span>Netinho Virtual</span><strong>${escapeHtml(state.idosoNome)}</strong></span>
        <button class="round-btn" data-action="settings" aria-label="Configurações">${icon("sliders", 2)}</button>
      </div>

      <div class="hero">
        <div>
          <h1>Oi, ${escapeHtml(firstName(state.idosoNome))}!</h1>
          <p>Aperte o botão rosa e fale.</p>
        </div>
        <button class="hero-mic" data-action="listen" aria-label="Falar com o Netinho">${icon("mic", 2)}</button>
      </div>

      <nav class="fn-grid" aria-label="O que você quer fazer?">
        <button class="fn-card fn-guide" data-action="guideList"><span class="fn-icon">${icon("book", 2)}</span><strong>Me ensina</strong></button>
        <button class="fn-card fn-make" data-action="make"><span class="fn-icon">${icon("message", 2)}</span><strong>Faz pra mim</strong></button>
        <button class="fn-card fn-scam" data-action="scam"><span class="fn-icon">${icon("shield", 2)}</span><strong>É golpe?</strong></button>
        <button class="fn-card fn-help" data-action="help"><span class="fn-icon">${icon("users", 2)}</span><strong>Pedir ajuda</strong></button>
      </nav>

      ${next ? `
        <button class="next-reminder" data-action="reminders">
          <span class="bell">${icon("bell")}</span>
          <span class="next-text"><span>Próximo lembrete</span><strong>${escapeHtml(next.titulo)}</strong></span>
          <span class="time-pill">${escapeHtml(next.hora)}</span>
        </button>` : ""}

      <button class="btn btn-red" data-action="emergency">${icon("phone")}Emergência</button>
    </section>`;
}

function phonePreview() {
  const screen = state.screen;
  const draft = state.draft || { contact: "Rafael", content: "" };
  const contact = screen === "make" ? "Rafael" : draft.contact;
  const content = screen === "make" ? (state.taskText || "Sua mensagem vai aparecer aqui.") : draft.content;
  const notes = {
    make: "Prévia ilustrativa; ainda não mexi no seu celular.",
    confirm: 'Vou tocar em "Enviar" só depois da sua confirmação.',
    sent: "Simulação visual: nenhuma mensagem real foi enviada.",
  };
  return `
    <div class="preview">
      <div class="phone" aria-label="Prévia ilustrativa de uma conversa no celular">
        <div class="phone-head">
          <span class="phone-initial" aria-hidden="true">${escapeHtml((contact[0] || "R").toUpperCase())}</span>
          <span class="phone-who"><strong>${escapeHtml(contact)}</strong><span>Mensagens</span></span>
          <span class="demo-tag">Demonstração</span>
        </div>
        <div class="chat">
          <div class="bubble in">Oi, mãe! Tudo bem?</div>
          <div class="bubble out${screen === "sent" ? " sent" : ""}"><span data-preview-message>${escapeHtml(content)}</span>
            ${screen === "sent" ? `<span class="bubble-status">${icon("check", 3)}Enviado</span>` : ""}
          </div>
        </div>
        <div class="compose${screen === "confirm" ? " glow" : ""}">Mensagem
          <span class="compose-send">${icon("arrow", 2.6)}</span>
        </div>
      </div>
      <p class="note">${notes[screen]}</p>
    </div>`;
}

// Botões com os contatos da rede de apoio: tocar em um já começa o pedido com o nome certo.
function contactPicker() {
  const contacts = state.supportContacts || [];
  if (!contacts.length) return "";
  return `<div class="contact-picker" aria-label="Para quem?">
      <span class="label">Para quem?</span>
      <div class="contact-chips">${contacts.map((c) => `<button type="button" class="contact-chip" data-action="pick-contact" data-name="${escapeHtml(firstName(c.nome))}">${escapeHtml(firstName(c.nome))} <small>${escapeHtml(c.parentesco)}</small></button>`).join("")}</div>
    </div>`;
}

function pickContact(name) {
  const field = app.querySelector("#task-text");
  const current = (field ? field.value : state.taskText || "").trim();
  const replaced = current.replace(/((?:para|pra|pro)\s+(?:(?:o|a|meu|minha|seu|sua)\s+)*)[A-Za-zÀ-ÿ]+/i, `$1${name}`);
  state.taskText = current && replaced !== current ? replaced : `Manda mensagem pro ${name} dizendo `;
  state.taskError = "";
  render();
  const next = app.querySelector("#task-text");
  if (next) {
    next.focus();
    next.setSelectionRange(next.value.length, next.value.length);
  }
}

function renderMake() {
  return `
    <section class="screen">
      <h1 class="title">O que você quer que eu faça?</h1>
      ${phonePreview()}
      <form id="task-form" class="card push-bottom">
        <label class="label" for="task-text">Pode escrever ou falar</label>
        <div class="field-wrap">
          <textarea class="field with-mic" id="task-text" name="taskText" placeholder="Ex.: Manda mensagem pro Rafael dizendo que chego às 3">${escapeHtml(state.taskText)}</textarea>
          <button class="field-mic" type="button" data-action="listen" aria-label="Falar pedido">${icon("mic")}</button>
        </div>
        ${state.taskError ? `<p class="task-error" role="alert">${icon("alert")}<span>${escapeHtml(state.taskError)}</span></p>` : ""}
        ${contactPicker()}
        <p class="note" style="padding:0;text-align:left">Eu vou mostrar tudo para você confirmar antes.</p>
        <div class="btn-row auto">
          <button class="btn btn-soft" type="button" data-action="home">Voltar</button>
          <button class="btn btn-blue" type="submit">Continuar ${icon("arrow", 2.6)}</button>
        </div>
      </form>
    </section>`;
}

function renderConfirm() {
  return `
    <section class="screen">
      <h1 class="title">Mando para ${escapeHtml(state.draft.contact)}?</h1>
      ${phonePreview()}
      <div class="card confirm-card push-bottom">
        <p class="ask">Posso enviar?</p>
        <div class="btn-row">
          <button class="btn choice btn-danger-soft" data-action="cancel-task">${icon("close", 2.8)}Não</button>
          <button class="btn choice btn-green" data-action="confirm-task">${icon("check", 2.8)}Sim</button>
        </div>
      </div>
    </section>`;
}

function undoButtonInner() {
  const left = state.undoLeft;
  const offset = (62.8 * (1 - left / UNDO_SECONDS)).toFixed(1);
  return `<svg class="ico undo-ring" viewBox="0 0 24 24" fill="none" stroke-width="3" aria-hidden="true">
      <circle cx="12" cy="12" r="10" stroke="currentColor" opacity=".18"/>
      <circle class="ring" cx="12" cy="12" r="10" stroke="currentColor" stroke-linecap="round" stroke-dasharray="62.8" stroke-dashoffset="${offset}"/>
    </svg><span data-undo-label>${left > 0 ? `Desfazer (${left})` : "Desfazer"}</span>`;
}

function renderSent() {
  return `
    <section class="screen">
      <h1 class="title">Pronto! O ${escapeHtml(state.draft.contact)} já recebeu.</h1>
      ${phonePreview()}
      <div class="card push-bottom">
        <button class="btn btn-outline" data-action="undo" ${state.undoLeft === 0 ? "disabled" : ""}>${undoButtonInner()}</button>
        <div class="btn-row">
          <button class="btn btn-soft" data-action="home">Início</button>
          <button class="btn btn-blue" data-action="make" style="box-shadow:none">${icon("mic")}Falar</button>
        </div>
      </div>
    </section>`;
}

function loadingCard(text) {
  return `<div class="empty-card">${escapeHtml(text)}</div>`;
}

function renderGuideList() {
  let list;
  if (state.guidesError) list = loadingCard("Não consegui carregar os guias. Tente de novo daqui a pouco.");
  else if (state.guides === null) list = loadingCard("Carregando os guias…");
  else if (state.guides.length === 0) list = loadingCard("Ainda não há guias disponíveis.");
  else {
    list = state.guides.map((guide) => `
          <button class="guide-item" data-action="open-guide" data-guide="${escapeHtml(guide.id)}">
            <span class="yellow-icon">${icon("book", 2)}</span>
            <span class="guide-item-text"><strong>${escapeHtml(guide.title)}</strong><span>${guide.steps.length} ${guide.steps.length === 1 ? "passo" : "passos"}${guide.app ? ` · ${escapeHtml(guide.app)}` : ""}</span></span>
            ${icon("chevron", 2.6)}
          </button>`).join("");
  }
  return `
    <section class="screen">
      <h1 class="title">O que você quer aprender?</h1>
      <div class="list">${list}</div>
      <p class="note push-bottom">Eu explico um passo de cada vez, sem pressa.</p>
    </section>`;
}

function guideStepAt(index) {
  return state.guide?.steps[index] || null;
}

function renderGuide() {
  const guide = state.guide;
  const total = Math.max(guide.steps.length, state.guideStep + 1);
  const local = guideStepAt(state.guideStep);
  const instruction = state.apiStep?.txPasInstrucao || local?.instrucao || "";
  const highlight = state.apiStep?.nmPasElemento || local?.elemento || "";
  const segments = Array.from({ length: total }, (_, index) =>
    `<span class="${index < state.guideStep ? "done" : index === state.guideStep ? "now" : ""}"></span>`).join("");
  return `
    <section class="screen">
      <h1 class="title">Aperte onde está brilhando.</h1>
      <div class="card step-card">
        <div class="step-head"><span>${escapeHtml(guide.title)}</span><span class="step-count">Passo ${state.guideStep + 1} de ${total}</span></div>
        <div class="segments" aria-label="Progresso do guia: passo ${state.guideStep + 1} de ${total}">${segments}</div>
        <div class="step-body">
          <span class="step-num" aria-hidden="true">${state.guideStep + 1}</span>
          <p class="step-text">${escapeHtml(instruction)}</p>
        </div>
        <div class="touch-box">Toque no seu celular:
          <span class="touch-target">${escapeHtml(highlight)}</span>
        </div>
      </div>
      <div class="btn-stack push-bottom">
        <button class="btn btn-green btn-lg" data-action="advance-guide">${icon("check", 2.8)}Consegui</button>
        <button class="btn btn-card" data-action="explain-guide">${icon("question")}Outra explicação</button>
      </div>
    </section>`;
}

function renderGuideDone() {
  return `
    <section class="screen">
      <div class="celebrate">
        <span class="big-check">${icon("check", 3)}</span>
        <h1>Você conseguiu! Fez tudo sozinha.</h1>
      </div>
      <div class="card done-card">
        <p class="strong">Você terminou o guia: ${escapeHtml(state.guide?.title || "")}.</p>
        <p class="small">Este protótipo não faz nada de verdade no aplicativo.</p>
        <button class="btn btn-ink" data-action="home">Início</button>
      </div>
    </section>`;
}

function renderScam() {
  return `
    <section class="screen">
      <h1 class="title">Vamos olhar juntos.</h1>
      <form id="scam-form" class="scam-form">
        <div class="card">
          <label class="label" for="scam-text">Cole ou escreva a mensagem</label>
          <textarea class="field tall orange" id="scam-text" name="scamText" placeholder="Ex.: Mãe, troquei de número. Me manda um Pix agora!">${escapeHtml(state.scamText)}</textarea>
          ${state.scamImg ? `
            <div class="attached">
              <span class="thumb" role="img" aria-label="Imagem anexada" style="background-image:url(${escapeHtml(state.scamImg)})"></span>
              <strong>Imagem anexada</strong>
              <button class="round-btn" type="button" data-action="clear-scam-img" aria-label="Remover imagem">${icon("close", 2.6)}</button>
            </div>` : ""}
          <label class="photo-btn">${icon("camera")}Enviar foto ou print
            <input class="visually-hidden" id="scam-img" type="file" accept="image/*">
          </label>
        </div>
        <div class="warning">${icon("shield")}Não abra links nem envie dinheiro enquanto verificamos.</div>
        <div class="btn-row auto push-bottom">
          <button class="btn btn-card btn-tall" type="button" data-action="home">Voltar</button>
          <button class="btn btn-orange" type="submit" ${state.busy === "scam" ? "disabled" : ""}>${state.busy === "scam" ? (state.scamFile ? "Olhando a foto…" : "Verificando…") : "Verificar"}</button>
        </div>
      </form>
    </section>`;
}

function renderScamResult() {
  const result = state.scam;
  const high = result.level === "ALTO";
  const tone = high ? "risk-high" : result.level === "MÉDIO" ? "risk-mid" : "risk-low";
  return `
    <section class="screen">
      <div class="risk-top">
        <span class="risk-icon ${tone}">${icon("shield")}</span>
        <span class="risk-badge ${tone}">Risco ${escapeHtml(result.level)}</span>
        <h1>${high ? "Não aperte! Pode ser golpe." : "Aqui está o que encontrei."}</h1>
      </div>
      <div class="card risk-card">
        <div><strong class="k">Por quê?</strong><p>${escapeHtml(result.reason)}</p></div>
        <div><strong class="k">O que fazer?</strong><p class="strong">${escapeHtml(result.advice)}</p></div>
        <div class="btn-row auto">
          <button class="btn btn-soft" data-action="scam">Ver outra</button>
          <button class="btn btn-ink" data-action="home">Entendi ${icon("check", 2.8)}</button>
        </div>
      </div>
    </section>`;
}

function renderHelp() {
  const contacts = state.supportContacts.length
    ? state.supportContacts.map((contact) => `
        <div class="contact">
          ${portrait(contact)}
          <span class="contact-text"><strong>${escapeHtml(firstName(contact.nome))}</strong><span>${escapeHtml(contact.parentesco)}</span></span>
          <span class="order-pill">${escapeHtml(contact.ordem)}º contato</span>
        </div>`).join("")
    : `<div class="empty-card">Os contatos da rede de apoio aparecerão aqui.</div>`;
  return `
    <section class="screen">
      <h1 class="title">Quem você quer chamar?</h1>
      <div class="list">${contacts}</div>
      <div class="btn-stack push-bottom">
        <p class="note" style="margin-bottom:4px;font-size:17px">Vou avisar a sua família que você precisa de ajuda.</p>
        <button class="btn btn-green btn-lg" data-action="notify-family">${icon("users")}Pedir ajuda</button>
        <button class="btn btn-card" data-action="home">Agora não</button>
      </div>
    </section>`;
}

function renderEmergency() {
  const contacts = state.supportContacts.length
    ? state.supportContacts.map((contact) => `
        <div class="trusted-row">
          ${portrait(contact, true)}
          <strong>${escapeHtml(firstName(contact.nome))}</strong>
          <span>${escapeHtml(contact.parentesco)} · ${escapeHtml(contact.ordem)}º</span>
        </div>`).join("")
    : `<p class="small" style="font-size:16px">Os contatos de emergência serão carregados da rede de apoio.</p>`;
  return `
    <section class="screen">
      <h1 class="title big center">Segure o botão para pedir ajuda.</h1>
      <div class="hold-area">
        <button id="hold" class="hold${state.hold === "done" ? " done" : ""}" ${state.hold === "done" ? "disabled" : ""}>
          <span class="fill"></span>
          ${icon("phone", 2)}
          <span class="hold-label" data-hold-label>${state.hold === "done" ? "Pedido registrado" : "Segure por 2 segundos"}</span>
        </button>
      </div>
      <div class="card trusted">
        <strong>Contatos de confiança</strong>
        ${contacts}
        <button class="btn btn-ink" data-action="samu">Ligar 192 · SAMU</button>
        <p class="small">O pedido fica registrado para a sua família. A ligação para o 192 você faz pelo seu telefone.</p>
      </div>
    </section>`;
}

function renderReminders() {
  const list = state.reminders.length
    ? state.reminders.map((reminder) => {
      const label = reminder.done ? "Feito" : reminder.tipo === "Consulta" ? "Já fui" : "Já tomei";
      return `
        <div class="card reminder${reminder.done ? " is-done" : ""}">
          <div class="reminder-top">
            <span class="when"><span>${escapeHtml(reminder.quando)}</span><strong>${escapeHtml(reminder.hora)}</strong></span>
            <span class="reminder-text"><strong>${escapeHtml(reminder.titulo)}</strong><span>${escapeHtml(reminder.tipo)}</span></span>
          </div>
          <button class="btn ${reminder.done ? "btn-done" : "btn-green"}" style="box-shadow:none" data-action="toggle-reminder" data-reminder="${escapeHtml(reminder.id)}" aria-pressed="${reminder.done}">${icon("check", 2.8)}${label}</button>
        </div>`;
    }).join("")
    : `<div class="empty-card">Nenhum lembrete por enquanto.</div>`;
  return `
    <section class="screen">
      <h1 class="title">Seus lembretes</h1>
      <div class="list">${list}</div>
      <p class="note push-bottom">Os lembretes são cadastrados por um familiar.</p>
    </section>`;
}

function historyWhen(iso) {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return "";
  return `${dayLabel(date)}, ${timeLabel(date)}`;
}

function renderHistory() {
  let rows;
  if (state.historyError) rows = `<div class="history-row"><span class="history-text"><strong>Não consegui carregar o histórico.</strong><span>Tente de novo daqui a pouco.</span></span></div>`;
  else if (state.history === null) rows = `<div class="history-row"><span class="history-text"><strong>Carregando…</strong></span></div>`;
  else rows = state.history.length
    ? state.history.map((item) => `
        <div class="history-row">
          <span class="dot" style="background:${HISTORY_DOTS[item.type] || "#15152a"}"></span>
          <span class="history-text"><strong>${escapeHtml(item.text)}</strong><span>${escapeHtml(historyWhen(item.at))}</span></span>
        </div>`).join("")
    : `<div class="history-row"><span class="history-text"><strong>Ainda não fizemos nada juntos.</strong><span>O que você fizer com o Netinho aparece aqui.</span></span></div>`;
  return `
    <section class="screen">
      <h1 class="title">O que já fizemos</h1>
      <div class="card history">${rows}</div>
    </section>`;
}

function renderProfile() {
  return `
    <section class="screen">
      <div class="profile-top">
        <span class="profile-photo" aria-hidden="true"></span>
        <h1>${escapeHtml(state.idosoNome)}</h1>
        <span>Usuário: <strong>${escapeHtml(state.user)}</strong></span>
      </div>
      <dl class="card details">
        <div><dt>Cadastro</dt><dd>Idoso nº ${escapeHtml(state.idosoId)}</dd></div>
        <div><dt>API</dt><dd>${escapeHtml(state.apiBase)}</dd></div>
        <div><dt>Conexão</dt><dd class="pill${state.apiConnected ? "" : " off"}">${state.apiConnected ? "Conectada" : "Desconectada"}</dd></div>
        <div><dt>Modo sem leitura</dt><dd>${state.silentMode ? "Ativado" : "Desativado"}</dd></div>
      </dl>
      <div class="btn-stack push-bottom">
        <button class="btn btn-blue lg" data-action="settings">Configurações</button>
        <button class="btn btn-card" data-action="history">Histórico</button>
        <button class="btn btn-danger-soft" data-action="logout">Sair da conta</button>
      </div>
    </section>`;
}

function renderSettings() {
  return `
    <section class="screen settings">
      <h1 class="title">Configurações</h1>
      <button class="switch-row" role="switch" aria-checked="${state.silentMode}" data-action="toggle-silent">
        <span class="switch-text"><strong>Modo sem leitura</strong><span>Tudo por voz, fotos e cores</span></span>
        <span class="track" aria-hidden="true"><span class="knob"></span></span>
      </button>
      <p class="note left">Essa escolha fica salva no seu cadastro.</p>
      <form id="connection-form" class="card">
        <label class="label small" for="api-url" style="margin-bottom:-4px">Endereço da API</label>
        <input class="field sm" id="api-url" name="apiUrl" type="text" value="${escapeHtml(state.apiBase)}" required>
        <button class="btn btn-blue-soft" type="submit" style="margin-top:4px">Conectar</button>
      </form>
      <button class="btn btn-green push-bottom" style="min-height:72px;font-size:22px" data-action="home">Pronto</button>
    </section>`;
}

const SCREENS = {
  login: renderLogin,
  home: renderHome,
  make: renderMake,
  confirm: renderConfirm,
  sent: renderSent,
  guideList: renderGuideList,
  guide: renderGuide,
  guideDone: renderGuideDone,
  scam: renderScam,
  scamResult: renderScamResult,
  help: renderHelp,
  emergency: renderEmergency,
  reminders: renderReminders,
  history: renderHistory,
  profile: renderProfile,
  settings: renderSettings,
};

/* ---------------------------------------------------------------- Renderização */

function screenTone() {
  const screen = state.screen;
  if (screen === "login") return "night";
  if (screen === "emergency" || (screen === "scamResult" && state.scam?.level === "ALTO")) return "red";
  if (screen === "guideDone") return "done";
  if (["make", "confirm", "sent"].includes(screen)) return "task";
  if (["guide", "guideList"].includes(screen)) return "guide";
  if (["scam", "scamResult"].includes(screen)) return "scam";
  if (screen === "help") return "help";
  return "page";
}

function renderOverlay() {
  if (state.listening) {
    return `
      <div class="backdrop" role="dialog" aria-modal="true" aria-label="Ouvindo">
        <div class="sheet listening">
          <span class="pulse" aria-hidden="true"><span class="ring"></span><span class="ring late"></span><span class="core">${icon("mic", 2)}</span></span>
          <strong class="sheet-title">Estou ouvindo. Pode falar.</strong>
          <p data-heard>${escapeHtml(state.heard || "Diga, por exemplo: “isso é golpe?” ou “manda mensagem pro Rafael”.")}</p>
          <button class="btn btn-soft btn-tall" data-action="stop-listen" style="font-size:21px">Parar</button>
        </div>
      </div>`;
  }
  if (state.error) {
    return `
      <div class="backdrop" role="alertdialog" aria-modal="true" aria-label="Erro de conexão">
        <div class="sheet">
          <span class="alert-icon">${icon("alert")}</span>
          <strong class="sheet-title">Não consegui conectar.</strong>
          <p>${escapeHtml(state.error)}</p>
          <button class="btn btn-ink" data-action="close-error">Tentar de novo</button>
          <button class="btn btn-soft" data-action="error-home">Voltar ao início</button>
        </div>
      </div>`;
  }
  return "";
}

function render() {
  const screen = state.screen;
  const changed = screen !== lastRenderedScreen;
  const tone = screenTone();
  const dark = ["night", "red", "done"].includes(tone);

  app.dataset.tone = tone;
  app.classList.toggle("is-dark", dark);
  app.classList.toggle("has-nav", hasNav());

  app.innerHTML = [
    state.apiConnected === false && screen !== "login" ? offlineBanner() : "",
    hasNav() ? topbar() : "",
    (SCREENS[screen] || renderHome)(),
    hasNav() ? bottomNav() : "",
  ].join("");

  const section = app.querySelector("section");
  if (changed) section?.classList.add("enter");

  const previousOverlay = overlay.dataset.kind || "";
  overlay.innerHTML = renderOverlay();
  overlay.dataset.kind = state.listening ? "listening" : state.error ? "error" : "";
  if (overlay.dataset.kind && overlay.dataset.kind !== previousOverlay) {
    overlay.querySelector(".sheet .btn")?.focus({ preventScroll: true });
  }

  toastElement.classList.toggle("above-nav", hasNav());
  document.querySelector('meta[name="theme-color"]')?.setAttribute("content", dark ? (tone === "red" ? "#d3261a" : tone === "done" ? "#0b8250" : "#15152a") : "#15152a");

  if (changed) {
    lastRenderedScreen = screen;
    window.scrollTo(0, 0);
    if (screen !== "sent") window.clearInterval(undoTimer);
    if (state.silentMode && screen !== "login") window.setTimeout(() => speakScreen(true), 150);
  }
}

function go(screen, { keepScam = false } = {}) {
  if (screen === "scam" && !keepScam) {
    state.scamText = "";
    clearScamImage();
  }
  window.clearTimeout(holdTimer);
  state.hold = "idle";
  if (screen === "make" && state.screen !== "make") state.taskError = "";
  if (screen === "make" && !(state.supportContacts || []).length) refreshSupportNetwork(screen);
  state.screen = screen;
  render();
  if (screen === "help" || screen === "emergency") refreshSupportNetwork(screen);
  if (screen === "reminders" || screen === "home") refreshReminders(screen);
  if (screen === "guideList") refreshGuides();
  if (screen === "history") refreshHistory();
}

async function refreshSupportNetwork(screen) {
  try {
    await loadSupportNetwork();
    if (state.screen === screen) render();
  } catch {
    if (state.screen === screen) render();
  }
}

async function refreshGuides() {
  state.guidesError = false;
  if (state.guides === null) render();
  try {
    await loadGuides();
  } catch {
    if (state.guides === null) state.guidesError = true;
  }
  if (state.screen === "guideList") render();
}

async function refreshHistory() {
  state.historyError = false;
  try {
    await loadHistory();
  } catch {
    if (state.history === null) state.historyError = true;
  }
  if (state.screen === "history") render();
}

async function refreshReminders(screen) {
  if (state.screen === "home" && state.apiConnected === null) return; // a carga inicial já busca
  try {
    await loadReminders();
    if (state.screen === screen) render();
  } catch {
    /* mantém a lista atual */
  }
}

/* ---------------------------------------------------------------- Ações */

function clearScamImage() {
  if (state.scamImg) URL.revokeObjectURL(state.scamImg);
  state.scamImg = "";
  state.scamFile = null;
}

function parseDraft(text) {
  const contactMatch = text.match(/(?:para|pra|pro)\s+(?:(?:o|a|meu|minha|seu|sua)\s+)*([A-Za-zÀ-ÿ]+)/i);
  const contentMatch = text.match(/(?:dizendo|falando)\s+(?:que\s+)?(.+)$/i);
  return {
    contact: contactMatch ? contactMatch[1] : "seu contato",
    content: contentMatch ? contentMatch[1].trim() : text.trim(),
  };
}

function taskErrorMessage(error) {
  const msg = String(error.message || "");
  if (error.status === 404) return "Não achei essa pessoa nos seus contatos. Toque em um nome abaixo para escolher.";
  if (error.status === 409) return "Tem mais de uma pessoa com esse nome. Toque no nome certo abaixo.";
  if (/pagamento|banc|pix/i.test(msg)) return "Por segurança, eu não faço Pix, pagamentos nem nada de banco.";
  if (/senha|codigo|token|sms/i.test(msg)) return "Por segurança, eu não digito senhas nem códigos.";
  return msg;
}

async function submitTask(text) {
  if (!text) {
    showToast("Escreva ou fale o seu pedido.");
    return;
  }
  try {
    const task = await apiRequest("/tarefas", {
      method: "POST",
      body: { idIdoso: state.idosoId, pedido: text },
    });
    state.draft = { id: task.id, ...parseDraft(text) };
    state.taskText = "";
    state.taskError = "";
    go("confirm");
  } catch (error) {
    if (error instanceof ApiError) {
      state.taskText = text;
      state.taskError = taskErrorMessage(error);
      render();
      return;
    }
    showApiError(error, "Não deu para preparar a mensagem agora. Seu pedido continua escrito aqui.");
  }
}

async function confirmTask() {
  try {
    await apiRequest(`/tarefas/${state.draft.id}/confirmar`, { method: "PUT", body: { resposta: "SIM" } });
  } catch (error) {
    showApiError(error, "Não deu para enviar agora. Nada foi enviado.");
    return;
  }
  state.undoLeft = UNDO_SECONDS;
  go("sent");
  window.clearInterval(undoTimer);
  undoTimer = window.setInterval(() => {
    state.undoLeft = Math.max(0, state.undoLeft - 1);
    const button = app.querySelector('[data-action="undo"]');
    if (button) {
      button.innerHTML = undoButtonInner();
      button.disabled = state.undoLeft === 0;
    }
    if (state.undoLeft === 0) window.clearInterval(undoTimer);
  }, 1000);
}

async function cancelTask() {
  if (state.draft?.id) {
    apiRequest(`/tarefas/${state.draft.id}/confirmar`, { method: "PUT", body: { resposta: "NAO" } }).catch(() => {});
  }
  go("home");
  showToast("Tudo bem. A tarefa foi cancelada.");
}

async function undoTask() {
  if (state.undoLeft === 0) {
    showToast("O tempo para desfazer terminou.");
    return;
  }
  window.clearInterval(undoTimer);
  try {
    await apiRequest(`/tarefas/${state.draft.id}/desfazer`, { method: "PUT" });
    state.undoLeft = 0;
    go("home");
    showToast("Tarefa desfeita.");
  } catch (error) {
    showApiError(error, "Não deu para desfazer agora. Avise alguém da família se precisar.");
  }
}

async function openGuide(id) {
  const guide = state.guides?.find((item) => String(item.id) === String(id));
  if (!guide) return;
  try {
    const task = await apiRequest("/tarefas", {
      method: "POST",
      body: { idIdoso: state.idosoId, idGuia: guide.id },
    });
    state.guide = guide;
    state.guideStep = 0;
    state.guideTaskId = task.id;
    state.apiStep = await apiRequest(`/tarefas/${task.id}/passo-atual`);
    go("guide");
  } catch (error) {
    showApiError(error, "Não deu para abrir o guia agora. Tente de novo daqui a pouco.");
  }
}

async function advanceGuide() {
  try {
    const result = await apiRequest(`/tarefas/${state.guideTaskId}/avancar`, {
      method: "PUT",
      body: { concluido: true },
    });
    if (result?.concluido) {
      state.apiStep = null;
      go("guideDone");
    } else {
      state.apiStep = await apiRequest(`/tarefas/${state.guideTaskId}/passo-atual`);
      state.guideStep += 1;
      render();
    }
  } catch (error) {
    showApiError(error, "Não deu para ir ao próximo passo agora. Você continua neste passo.");
  }
}

function explainGuide() {
  const element = state.apiStep?.nmPasElemento || guideStepAt(state.guideStep)?.elemento || "o botão indicado";
  const text = `Procure "${element}" na tela do celular. É só tocar nele com calma. Se não achar, peça ajuda.`;
  speak(text);
  showToast(text);
}

// Reduz a foto (lado maior até 1600 px, JPEG) para enviar rápido mesmo em internet fraca.
async function imageToJpegBase64(file, maxSide = 1600, quality = 0.85) {
  const bitmap = await createImageBitmap(file);
  const scale = Math.min(1, maxSide / Math.max(bitmap.width, bitmap.height));
  const canvas = document.createElement("canvas");
  canvas.width = Math.round(bitmap.width * scale);
  canvas.height = Math.round(bitmap.height * scale);
  const context = canvas.getContext("2d");
  context.fillStyle = "#ffffff";
  context.fillRect(0, 0, canvas.width, canvas.height);
  context.drawImage(bitmap, 0, 0, canvas.width, canvas.height);
  bitmap.close?.();
  return canvas.toDataURL("image/jpeg", quality).split(",")[1];
}

function setBusy(kind) {
  state.busy = kind;
  render();
}

async function submitScam(text) {
  if (!text && !state.scamFile) {
    showToast("Cole ou escreva a mensagem, ou envie uma foto.");
    return;
  }
  setBusy("scam");
  try {
    let result;
    if (state.scamFile) {
      let imagemBase64;
      try {
        imagemBase64 = await imageToJpegBase64(state.scamFile);
      } catch {
        setBusy("");
        showToast("Não consegui abrir essa imagem. Tente outra foto ou print.");
        return;
      }
      result = await apiRequest("/golpes/verificar-imagem", {
        method: "POST",
        body: { idIdoso: state.idosoId, texto: text, imagemBase64, tipoImagem: "image/jpeg" },
      });
    } else {
      result = await apiRequest("/golpes/verificar", {
        method: "POST",
        body: { idIdoso: state.idosoId, texto: text, imagem: false },
      });
    }
    state.busy = "";
    state.scam = {
      level: result.nivel === "MEDIO" ? "MÉDIO" : result.nivel,
      reason: result.motivo,
      advice: result.oQueFazer,
    };
    go("scamResult");
  } catch (error) {
    state.busy = "";
    showApiError(error, "Não deu para verificar agora. Enquanto isso, não abra links nem envie dinheiro.");
  }
}

async function notifyFamily() {
  try {
    const help = await apiRequest("/atendimentos", {
      method: "POST",
      body: { idIdoso: state.idosoId, tentativas: 0 },
    });
    go("home");
    showToast(help?.indicacao
      ? `Pedido registrado. ${help.indicacao} pode atender.`
      : "Pedido registrado. Não há atendente disponível agora.");
  } catch (error) {
    showApiError(error, "Não deu para registrar o pedido agora. Se for urgente, ligue para alguém da família.");
  }
}

async function toggleReminder(id) {
  const reminder = state.reminders.find((item) => String(item.id) === String(id));
  if (!reminder) return;
  const done = !reminder.done;
  try {
    await apiRequest(`/lembretes/${reminder.id}`, {
      method: "PUT",
      body: { ...reminder.raw, status: done ? "CUMPRIDO" : "PENDENTE", dataCumprimento: null },
    });
  } catch (error) {
    showApiError(error, "Não deu para marcar o lembrete agora.");
    return;
  }
  reminder.done = done;
  reminder.raw = { ...reminder.raw, status: done ? "CUMPRIDO" : "PENDENTE" };
  render();
}

async function toggleSilent() {
  const ativo = !state.silentMode;
  try {
    await apiRequest(`/idosos/${state.idosoId}/modo-sem-leitura`, { method: "PUT", body: { ativo } });
  } catch (error) {
    showApiError(error, "Não deu para salvar o modo sem leitura agora.");
    return;
  }
  state.silentMode = ativo;
  render();
  showToast(ativo ? "Modo sem leitura ligado." : "Modo sem leitura desligado.");
}

async function connect(apiUrl) {
  if (!/^https?:\/\/\S+$/i.test(apiUrl)) {
    showToast("Informe um endereço que comece com http:// ou https://.");
    return;
  }
  state.apiBase = apiUrl;
  writeStorage("netinho-api-url", apiUrl);
  const ok = await loadAll();
  if (!state.token) return;
  if (ok) showToast(`Conectado ao cadastro de ${state.idosoNome}.`);
  else state.error = "Não consegui carregar o cadastro. Confira o endereço da API.";
  render();
}

/* ---------------------------------------------------------------- Emergência (segurar 2 s) */

function holdLabel(text) {
  const label = app.querySelector("#hold [data-hold-label]");
  if (label) label.textContent = text;
}

document.addEventListener("pointerdown", (event) => {
  const button = event.target.closest?.("#hold");
  if (!button || state.hold !== "idle") return;
  try {
    button.setPointerCapture(event.pointerId);
  } catch {
    /* sem captura */
  }
  state.hold = "holding";
  button.classList.add("holding");
  holdLabel("Continue segurando…");
  window.clearTimeout(holdTimer);
  holdTimer = window.setTimeout(async () => {
    state.hold = "done";
    const current = app.querySelector("#hold");
    if (current) {
      current.classList.remove("holding");
      current.classList.add("done");
      current.disabled = true;
    }
    holdLabel("Pedido registrado");
    if (navigator.vibrate) navigator.vibrate(300);
    try {
      const result = await apiRequest("/emergencias", { method: "POST", body: { idIdoso: state.idosoId } });
      logLocalHistory("emergency", "Pedido de emergência registrado");
      const names = (result?.contatos || []).map((contact) => firstName(contact.nome)).join(", ");
      showToast(names
        ? `Pedido de emergência registrado. Contatos: ${names}. Ligar ${result.ligar}.`
        : `Pedido de emergência registrado. Ligar ${result?.ligar || "192"}.`);
    } catch (error) {
      if (state.screen === "emergency") state.hold = "idle";
      showApiError(error, "Não deu para registrar a emergência. Ligue 192 ou para alguém da família.");
    }
  }, 2000);
});

function endHold() {
  if (state.hold !== "holding") return;
  window.clearTimeout(holdTimer);
  state.hold = "idle";
  app.querySelector("#hold")?.classList.remove("holding");
  holdLabel("Segure por 2 segundos");
}

document.addEventListener("pointerup", endHold);
document.addEventListener("pointercancel", endHold);

/* ---------------------------------------------------------------- Eventos */

// Modo sem leitura: fala o nome de cada botão tocado.
document.addEventListener("click", (event) => {
  if (!state.silentMode) return;
  const button = event.target.closest?.("button, .photo-btn");
  if (!button || button.dataset.action === "speak") return;
  const text = button.getAttribute("aria-label") || button.textContent.trim().replace(/\s+/g, " ");
  if (text) speak(text);
}, true);

app.addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);

  if (event.target.id === "login-form") {
    const username = String(form.get("username")).trim().toLowerCase();
    const password = String(form.get("password"));
    if (!username || !password) {
      showToast("Informe usuário e senha.");
      return;
    }
    setBusy("login");
    try {
      const login = await apiRequest("/auth/login", { method: "POST", body: { usuario: username, senha: password } });
      startSession(login);
      state.user = username;
      writeStorage("netinho-user", username);
    } catch (error) {
      setBusy("");
      if (error instanceof TypeError) showApiError(error, "Não consegui falar com o Netinho agora. Tente de novo daqui a pouco.");
      else showToast(error.message);
      return;
    }
    setBusy("");
    go("home");
    showToast(`Bem-vinda, ${firstName(state.idosoNome)}.`);
    await loadAll();
    render();
  }
  if (event.target.id === "task-form") await submitTask(String(form.get("taskText")).trim());
  if (event.target.id === "scam-form") {
    state.scamText = String(form.get("scamText")).trim();
    await submitScam(state.scamText);
  }
  if (event.target.id === "connection-form") {
    await connect(String(form.get("apiUrl")).trim().replace(/\/+$/, ""));
  }
});

app.addEventListener("input", (event) => {
  if (event.target.id === "task-text") {
    state.taskText = event.target.value;
    const preview = app.querySelector("[data-preview-message]");
    if (preview) preview.textContent = state.taskText || "Sua mensagem vai aparecer aqui.";
  }
  if (event.target.id === "scam-text") state.scamText = event.target.value;
});

app.addEventListener("change", (event) => {
  if (event.target.id !== "scam-img") return;
  const file = event.target.files?.[0];
  if (file) {
    if (!file.type.startsWith("image/")) {
      showToast("Escolha uma foto ou print.");
      event.target.value = "";
      return;
    }
    clearScamImage();
    state.scamFile = file;
    state.scamImg = URL.createObjectURL(file);
    render();
  }
});

async function handleAction(button) {
  const action = button.dataset.action;
  switch (action) {
    case "home": return go("home");
    case "make": return go("make");
    case "guideList": return go("guideList");
    case "scam": return go("scam");
    case "help": return go("help");
    case "emergency": return go("emergency");
    case "reminders": return go("reminders");
    case "history": return go("history");
    case "profile": return go("profile");
    case "settings": return go("settings");
    case "logout":
      return endSession("Você saiu da conta.");
    case "listen": return beginListening();
    case "stop-listen": return stopListening();
    case "speak": return speakScreen();
    case "pick-contact": return pickContact(button.dataset.name);
    case "confirm-task": return confirmTask();
    case "cancel-task": return cancelTask();
    case "undo": return undoTask();
    case "open-guide": return openGuide(button.dataset.guide);
    case "advance-guide": return advanceGuide();
    case "explain-guide": return explainGuide();
    case "clear-scam-img":
      clearScamImage();
      return render();
    case "notify-family": return notifyFamily();
    case "toggle-reminder": return toggleReminder(button.dataset.reminder);
    case "toggle-silent": return toggleSilent();
    case "samu": return showToast("Esta demonstração não inicia chamadas. Em uma emergência real, ligue 192.");
    case "close-error":
      state.error = "";
      render();
      if (state.apiConnected === false && await loadAll()) render();
      return undefined;
    case "error-home":
      state.error = "";
      return go("home");
    default: return undefined;
  }
}

async function onClick(event) {
  const button = event.target.closest("[data-action]");
  if (!button || button.disabled) return;
  if (button.dataset.busy) return;
  button.dataset.busy = "1";
  try {
    await handleAction(button);
  } finally {
    delete button.dataset.busy;
  }
}

app.addEventListener("click", onClick);
overlay.addEventListener("click", onClick);

document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape") return;
  if (state.listening) stopListening();
  else if (state.error) {
    state.error = "";
    render();
  }
});

/* ---------------------------------------------------------------- Início */

render();
if (state.screen !== "login") {
  loadAll().then(() => render());
}
