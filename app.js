// Controla os estados/telas do frontend, chama a API e gerencia entrada e leitura por voz.
const app = document.querySelector("#app");
const toastElement = document.querySelector("#toast");

const iconPaths = {
  back: '<path d="M19 12H5M12 19l-7-7 7-7"/>',
  speaker: '<path d="M11 5 6 9H3v6h3l5 4V5ZM15.5 8.5a5 5 0 0 1 0 7m3-10a9 9 0 0 1 0 13"/>',
  smile: '<circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2M9 9h.01M15 9h.01"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="m19.4 15 .1.1 1.4 1.1-1.4 2.4-1.7-.7a8 8 0 0 1-1.7 1l-.3 1.8h-2.8l-.3-1.8a8 8 0 0 1-1.7-1l-1.7.7-1.4-2.4 1.4-1.1a7 7 0 0 1 0-2l-1.4-1.1 1.4-2.4 1.7.7a8 8 0 0 1 1.7-1l.3-1.8h2.8l.3 1.8a8 8 0 0 1 1.7 1l1.7-.7 1.4 2.4-1.4 1.1a7 7 0 0 1 0 2Z" transform="translate(-1 -1)"/>',
  book: '<path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v16H6.5A2.5 2.5 0 0 0 4 21V5.5ZM4 17a2.5 2.5 0 0 1 2.5-2.5H20M8 7h8"/>',
  message: '<path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5H5l1.8-3A7.5 7.5 0 1 1 20 11.5Z"/><path d="M8 11h.01M12 11h.01M16 11h.01"/>',
  shield: '<path d="M12 22s8-4 8-11V5l-8-3-8 3v6c0 7 8 11 8 11Z"/><path d="M12 8v4m0 4h.01"/>',
  users: '<path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2m6-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm6-7.8a4 4 0 0 1 0 7.6M20 21v-2a4 4 0 0 0-3-3.9"/>',
  mic: '<rect x="9" y="2" width="6" height="13" rx="3"/><path d="M5 10v2a7 7 0 0 0 14 0v-2m-7 9v3m-4 0h8"/>',
  phone: '<path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.4 19.4 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7l.5 3a2 2 0 0 1-.6 1.8L7.1 10.4a16 16 0 0 0 6 6l1.9-1.9a2 2 0 0 1 1.8-.6l3 .5a2 2 0 0 1 2.2 2.5Z"/>',
  person: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  check: '<path d="m5 12 4 4L19 6"/>',
  undo: '<path d="M3 7v6h6M4 13a8 8 0 1 0 2-5.3L3 10"/>',
  celebrate: '<path d="m12 3 2.2 5.8L20 11l-5.8 2.2L12 19l-2.2-5.8L4 11l5.8-2.2L12 3Z"/><path d="m19 14 1.2 2.8L23 18l-2.8 1.2L19 22l-1.2-2.8L15 18l2.8-1.2L19 14ZM5 2l1 2 2 1-2 1-1 2-1-2-2-1 2-1 1-2Z"/>',
  stop: '<path d="M7 3h10l4 4v10l-4 4H7l-4-4V7l4-4Z"/><path d="M12 8v5m0 3h.01"/>',
  home: '<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-6v-7h-4v7H4a1 1 0 0 1-1-1V10Z"/>',
  close: '<path d="m18 6-12 12M6 6l12 12"/>',
  arrow: '<path d="M5 12h14m-7-7 7 7-7 7"/>',
  question: '<circle cx="12" cy="12" r="10"/><path d="M9.6 9a2.5 2.5 0 1 1 4.6 1.4c-.9 1.1-2.2 1.4-2.2 3.1m0 3h.01"/>',
  cursor: '<path d="m5 3 14 10-7 1 3 6-3 1-3-6-4 5V3Z"/>',
};

function icon(name, className = "svg-icon") {
  return `<svg class="${className}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">${iconPaths[name]}</svg>`;
}

const state = {
  screen: "home",
  taskText: "",
  taskDraft: null,
  guideStep: 0,
  guideTaskId: null,
  guideStepData: null,
  scamText: "",
  scamResult: null,
  silentMode: false,
  apiBase: localStorage.getItem("netinho-api-url") || "http://localhost:8080",
  idosoId: Number(localStorage.getItem("netinho-idoso-id") || "1"),
  idosoNome: "Maria Helena",
  apiConnected: false,
  supportContacts: [],
};

const guideSteps = [
  {
    instruction: "Abra o aplicativo gov.br no celular.",
    highlight: "Aplicativo gov.br",
    alternative: "Procure o desenho azul escrito gov.br na tela do celular.",
  },
  {
    instruction: "Entre na sua conta gov.br.",
    highlight: "Entrar com gov.br",
    alternative: "Toque no botão para entrar e informe seus dados com cuidado.",
  },
  {
    instruction: "Escolha o serviço Pedir aposentadoria.",
    highlight: "Pedir aposentadoria",
    alternative: "Na lista de serviços, procure o pedido de aposentadoria.",
  },
  {
    instruction: "Confira seus dados e continue.",
    highlight: "Continuar",
    alternative: "Leia ou ouça os dados. Se estiverem certos, toque em continuar.",
  },
  {
    instruction: "Responda às perguntas do formulário.",
    highlight: "Próxima pergunta",
    alternative: "Responda uma pergunta de cada vez. Peça ajuda se precisar.",
  },
  {
    instruction: "Revise o pedido e toque em Enviar.",
    highlight: "Enviar pedido",
    alternative: "Confira tudo com calma. Só envie quando estiver pronta.",
  },
];

const escapeHtml = (value) =>
  String(value).replace(/[&<>"']/g, (character) => ({
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
  showToast.timeoutId = window.setTimeout(
    () => toastElement.classList.remove("visible"),
    2800,
  );
}

async function apiRequest(path, { method = "GET", body } = {}) {
  const response = await fetch(`${state.apiBase.replace(/\/+$/, "")}${path}`, {
    method,
    headers: body === undefined ? { Accept: "application/json" } : {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  state.apiConnected = true;
  const data = response.status === 204 ? null : await response.json();
  if (!response.ok) {
    throw new Error(data?.mensagem || `A API respondeu com erro ${response.status}.`);
  }
  return data;
}

function showApiError(error) {
  if (error instanceof TypeError) state.apiConnected = false;
  showToast(`Não consegui falar com a API: ${error.message}`);
}

async function loadElder() {
  try {
    const elder = await apiRequest(`/idosos/${state.idosoId}`);
    state.idosoNome = elder.nome || state.idosoNome;
    state.silentMode = Boolean(elder.semLeitura);
  } catch {
    state.apiConnected = false;
  }
}

async function loadSupportNetwork() {
  const links = await apiRequest(`/idosos/${state.idosoId}/rede-apoio`);
  state.supportContacts = await Promise.all(links.map(async (link) => {
    const familiar = await apiRequest(`/familiares/${link.idFamiliar}`);
    return {
      nome: familiar.nome,
      parentesco: link.parentesco,
      ordem: link.ordemContato ?? 0,
    };
  }));
}

function speak(text) {
  if (!("speechSynthesis" in window)) {
    showToast("A leitura em voz alta não está disponível neste navegador.");
    return;
  }
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = "pt-BR";
  utterance.rate = 0.88;
  window.speechSynthesis.speak(utterance);
}

function topbar() {
  return `
    <div class="topbar">
      <button class="icon-button" data-action="home" aria-label="Voltar ao início">${icon("back")}</button>
      <button class="icon-button" data-action="speak" aria-label="Ouvir esta tela">${icon("speaker")}</button>
    </div>`;
}

function faceLine(text) {
  return `
    <div class="face-line">
      <span class="face">${icon("smile")}</span>
      <h1>${escapeHtml(text)}</h1>
    </div>`;
}

function renderHome() {
  return `
    <section class="home">
      <header class="home-header">
        <div class="header-row">
          <span class="brand" aria-label="Netinho Virtual">N</span>
          <button class="header-icon" data-action="settings" aria-label="Configurações">${icon("settings")}</button>
        </div>
        <h1>Oi, ${escapeHtml(state.idosoNome)}!</h1>
        <p>Aperte o botão rosa e fale.</p>
      </header>
      <nav class="function-grid" aria-label="O que você quer fazer?">
        <button class="function-card card-teach" data-action="guide">
          <span class="symbol">${icon("book")}</span><strong>Me ensina</strong>
        </button>
        <button class="function-card card-do" data-action="make">
          <span class="symbol">${icon("message")}</span><strong>Faz pra mim</strong>
        </button>
        <button class="function-card card-scam" data-action="scam">
          <span class="symbol">${icon("shield")}</span><strong>É golpe?</strong>
        </button>
        <button class="function-card card-help" data-action="help">
          <span class="symbol">${icon("users")}</span><strong>Pedir ajuda</strong>
        </button>
      </nav>
      <div class="home-actions">
        <button class="mic-button" data-action="listen" aria-label="Falar com o Netinho">${icon("mic")}</button>
        <button class="emergency-button" data-action="emergency">${icon("phone")} &nbsp; Emergência</button>
      </div>
      <p class="mode-note">${state.apiConnected ? "Conectado à API Netinho." : "API desconectada. Inicie o backend para usar os recursos."}</p>
    </section>`;
}

function phonePreview({ contact, content, status, assistantText, highlighted = false, sent = false }) {
  return `
    <div class="phone-preview">
      <div class="phone-preview-heading">
        <strong>${icon("phone", "inline-icon")} Prévia do celular</strong>
        <span>Demonstração</span>
      </div>
      <div class="mock-phone" aria-label="Prévia ilustrativa de uma conversa no celular">
        <div class="mock-statusbar"><span>9:41</span><span>● ● ▰</span></div>
        <div class="mock-appbar">
          <span class="mock-avatar">${icon("person")}</span>
          <span class="mock-app-title"><strong>Mensagens</strong><small>${escapeHtml(contact)}</small></span>
          ${icon("phone", "mock-phone-action")}
        </div>
        <div class="mock-conversation">
          <span class="mock-date">Hoje</span>
          <div class="mock-bubble received">Oi, mãe! Tudo bem?</div>
          <div class="mock-bubble outgoing ${sent ? "sent" : ""}">
            <span data-preview-message>${escapeHtml(content || "Sua mensagem vai aparecer aqui.")}</span>
            ${sent ? `<small>${icon("check", "inline-icon")} Enviado</small>` : ""}
          </div>
        </div>
        <div class="mock-compose ${highlighted ? "highlighted" : ""}">
          <span>Mensagem</span>
          <span class="mock-send">${icon("arrow")}</span>
          ${highlighted ? `<span class="touch-pointer">${icon("cursor")}</span>` : ""}
        </div>
      </div>
      <div class="automation-status">
        <span class="automation-face">${icon("smile")}</span>
        <span>${escapeHtml(assistantText)}</span>
      </div>
      <p class="demo-note">${escapeHtml(status)}</p>
    </div>`;
}

function renderMake() {
  return `
    <section class="screen blue">
      ${topbar()}
      ${faceLine("O que você quer que eu faça?")}
      <div class="screen-content">
        ${phonePreview({
          contact: "Rafael",
          content: state.taskText,
          status: "Prévia ilustrativa; ainda não mexi no seu celular.",
          assistantText: "Vou preparar tudo e pedir sua confirmação.",
        })}
        <div class="panel">
          <form id="task-form">
            <label for="task-text">Pode escrever ou falar</label>
            <textarea id="task-text" name="taskText" placeholder="Ex.: Manda mensagem pro Rafael dizendo que chego às 3" required>${escapeHtml(state.taskText)}</textarea>
            <p class="hint">Eu vou mostrar tudo para você confirmar antes.</p>
            <div class="actions">
              <button class="action-no" type="button" data-action="home">Voltar</button>
              <button class="action-yes" type="submit">Continuar →</button>
            </div>
          </form>
          <div class="actions single">
            <button class="action-outline" type="button" data-action="listen">${icon("mic", "inline-icon")} Falar pedido</button>
          </div>
        </div>
      </div>
    </section>`;
}

function renderConfirm() {
  const draft = state.taskDraft;
  return `
    <section class="screen blue">
      ${topbar()}
      ${faceLine(`Mando para ${draft.contact}?`)}
      <div class="screen-content">
        ${phonePreview({
          contact: draft.contact,
          content: draft.content,
          status: "O destaque mostra onde o Netinho tocaria depois do seu Sim.",
          assistantText: 'Vou tocar em "Enviar" só depois da sua confirmação.',
          highlighted: true,
        })}
      </div>
      <div class="panel">
        <p>Posso enviar?</p>
        <div class="actions">
          <button class="action-no" data-action="cancel-task">${icon("close", "inline-icon")} &nbsp; Não</button>
          <button class="action-yes" data-action="confirm-task">${icon("check", "inline-icon")} &nbsp; Sim</button>
        </div>
      </div>
    </section>`;
}

function renderSent() {
  const undoLeft = Math.max(0, Math.ceil((state.undoUntil - Date.now()) / 1000));
  return `
    <section class="screen green">
      ${topbar()}
      ${faceLine(`Pronto! O ${state.taskDraft.contact} já recebeu.`)}
      <div class="screen-content">
        ${phonePreview({
          contact: state.taskDraft.contact,
          content: state.taskDraft.content,
          status: "Simulação visual: nenhuma mensagem real foi enviada.",
          assistantText: "No celular demonstrativo, a mensagem aparece como enviada.",
          sent: true,
        })}
      </div>
      <div class="panel">
        <button class="action-outline" data-action="undo" ${undoLeft === 0 ? "disabled" : ""}>${icon("undo", "inline-icon")} &nbsp; Desfazer ${undoLeft > 0 ? `(${undoLeft})` : ""}</button>
        <div class="actions">
          <button class="action-no" data-action="home">${icon("home", "inline-icon")} &nbsp; Início</button>
          <button class="action-blue" data-action="make">${icon("mic", "inline-icon")} &nbsp; Falar</button>
        </div>
      </div>
    </section>`;
}

function renderGuide() {
  const step = state.guideStepData;
  if (!step) {
    return `<section class="screen yellow">${topbar()}${faceLine("Carregando seu guia.")}<div class="screen-content"><div class="panel">Estou buscando o próximo passo na API.</div></div></section>`;
  }
  const instruction = step.txPasInstrucao;
  const highlight = step.nmPasElemento;
  return `
    <section class="screen yellow">
      ${topbar()}
      ${faceLine("Aperte onde está brilhando.")}
      <div class="screen-content">
        <span class="hero-symbol">${icon("book")}</span>
        <div class="panel">
          <p class="step-count">Orientação recebida da API</p>
          <div class="progress" aria-label="Progresso do guia">
            ${guideSteps.map((_, index) => `<span class="${index < state.guideStep ? "done" : ""}"></span>`).join("")}
          </div>
          <p>${escapeHtml(instruction)}</p>
          <p class="hint">Toque no seu celular: <strong>${escapeHtml(highlight)}</strong></p>
          <div class="actions">
            <button class="action-no" data-action="explain-guide">${icon("question", "inline-icon")} &nbsp; Outra explicação</button>
            <button class="action-yes" data-action="advance-guide">${icon("check", "inline-icon")} &nbsp; Consegui</button>
          </div>
        </div>
      </div>
    </section>`;
}

function renderGuideDone() {
  return `
    <section class="screen yellow">
      ${topbar()}
      ${faceLine("Você conseguiu! Fez tudo sozinha.")}
      <div class="screen-content">
        <span class="hero-symbol celebration-icon">${icon("celebrate")}</span>
        <div class="message-card">Você terminou o guia de aposentadoria.</div>
        <p>Este protótipo não envia um pedido real ao gov.br.</p>
      </div>
      <div class="panel">
        <button class="action-yes" data-action="home">⌂ &nbsp; Início</button>
      </div>
    </section>`;
}

function renderScam() {
  return `
    <section class="screen orange">
      ${topbar()}
      ${faceLine("Vamos olhar juntos.")}
      <div class="screen-content">
        <span class="hero-symbol">${icon("shield")}</span>
        <div class="panel">
          <form id="scam-form">
            <label for="scam-text">Cole ou escreva a mensagem</label>
            <textarea id="scam-text" name="scamText" placeholder="Ex.: Mãe, troquei de número. Me manda um Pix agora!" required>${escapeHtml(state.scamText)}</textarea>
            <p class="hint">Não abra links nem envie dinheiro enquanto verificamos.</p>
            <div class="actions">
              <button class="action-no" type="button" data-action="home">Voltar</button>
              <button class="action-yes" type="submit">Verificar</button>
            </div>
          </form>
        </div>
      </div>
    </section>`;
}

function renderScamResult() {
  const result = state.scamResult;
  const badgeClass = result.level === "ALTO" ? "" : result.level === "MÉDIO" ? "medium" : "low";
  return `
    <section class="screen ${result.level === "ALTO" ? "red" : "orange"}">
      ${topbar()}
      ${faceLine(result.level === "ALTO" ? "Não aperte! Pode ser golpe." : "Aqui está o que encontrei.")}
      <div class="screen-content">
        <span class="hero-symbol">${icon(result.level === "ALTO" ? "stop" : "shield")}</span>
        <div class="panel">
          <span class="result-badge ${badgeClass}">Risco ${escapeHtml(result.level)}</span>
          <p><strong>Por quê?</strong> ${escapeHtml(result.reason)}</p>
          <p><strong>O que fazer?</strong> ${escapeHtml(result.advice)}</p>
        </div>
      </div>
      <div class="panel">
        <div class="actions">
          <button class="action-no" data-action="scam">Ver outra</button>
          <button class="action-yes" data-action="home">Entendi ${icon("check", "inline-icon")}</button>
        </div>
      </div>
    </section>`;
}

function renderHelp() {
  const contacts = state.supportContacts.length
    ? state.supportContacts.map((contact, index) => `
        <div class="contact-row"><span class="contact-avatar ${index % 2 ? "beatriz" : "rafael"}">${icon("person")}</span>
          <span><strong>${escapeHtml(contact.nome)}</strong><br>${escapeHtml(contact.parentesco || "rede de apoio")} · ${contact.ordem}º contato</span></div>`).join("")
    : `<p class="hint">Os contatos da rede de apoio aparecerão aqui.</p>`;
  return `
    <section class="screen green">
      ${topbar()}
      ${faceLine("Quem você quer chamar?")}
      <div class="screen-content">
        <span class="hero-symbol">${icon("users")}</span>
        <div class="panel">
          ${contacts}
          <p>Vou registrar seu pedido de ajuda pela API.</p>
          <div class="actions">
            <button class="action-no" data-action="home">Agora não</button>
            <button class="action-yes" data-action="notify-family">Pedir ajuda</button>
          </div>
        </div>
      </div>
    </section>`;
}

function renderEmergency() {
  const contacts = state.supportContacts.length
    ? state.supportContacts.map((contact, index) => `
        <div class="contact-row"><span class="contact-avatar ${index % 2 ? "beatriz" : "rafael"}">${icon("person")}</span>
          <span>${escapeHtml(contact.nome)} · ${escapeHtml(contact.parentesco || "contato")} · ${contact.ordem}º</span></div>`).join("")
    : `<p class="hint">Os contatos de emergência serão carregados da rede de apoio.</p>`;
  return `
    <section class="screen red">
      ${topbar()}
      ${faceLine("Segure o botão para pedir ajuda.")}
      <div class="screen-content">
        <span class="hero-symbol">${icon("phone")}</span>
        <div class="panel">
          <p><strong>Contatos de confiança</strong></p>
          ${contacts}
          <div class="actions single">
            <button id="emergency-hold" class="action-no">Segure por 2 segundos</button>
            <button class="action-dark" data-action="sam-u">Ligar 192 · SAMU</button>
          </div>
          <p class="hint">O pedido é registrado na API. A ligação para 192 não é iniciada automaticamente.</p>
        </div>
      </div>
    </section>`;
}

function renderSettings() {
  return `
    <section class="screen">
      ${topbar()}
      ${faceLine("Configurações")}
      <div class="screen-content">
        <div class="panel">
          <form id="connection-form">
            <label for="api-url">Endereço da API</label>
            <input id="api-url" name="apiUrl" type="text" value="${escapeHtml(state.apiBase)}" required>
            <label for="idoso-id">Código do idoso</label>
            <input id="idoso-id" name="idosoId" type="text" inputmode="numeric" value="${escapeHtml(state.idosoId)}" required>
            <div class="actions single">
              <button class="action-blue" type="submit">Conectar</button>
            </div>
          </form>
          <label class="setting-row" for="silent-mode">
            <span>Modo sem leitura<br><small>Tudo por voz, fotos e cores</small></span>
            <input id="silent-mode" type="checkbox" ${state.silentMode ? "checked" : ""}>
          </label>
          <p class="hint">O modo sem leitura é salvo no cadastro do idoso pela API.</p>
          <div class="actions single">
            <button class="action-yes" data-action="home">Pronto</button>
          </div>
        </div>
      </div>
    </section>`;
}

function render() {
  const screens = {
    home: renderHome,
    make: renderMake,
    confirm: renderConfirm,
    sent: renderSent,
    guide: renderGuide,
    guideDone: renderGuideDone,
    scam: renderScam,
    scamResult: renderScamResult,
    help: renderHelp,
    emergency: renderEmergency,
    settings: renderSettings,
  };
  app.innerHTML = (screens[state.screen] || renderHome)();
  if (state.screen === "make") document.querySelector("#task-text")?.focus({ preventScroll: true });
  if (state.screen === "scam") document.querySelector("#scam-text")?.focus({ preventScroll: true });
  if (state.screen === "sent") updateUndoCountdown();
}

async function createTaskDraft(text) {
  const contactMatch = text.match(/\b(?:para|pra|pro)\s+([A-Za-zÀ-ÿ]+)\b/i);
  const contentMatch = text.match(/\b(?:dizendo|falando)\s+(?:que\s+)?(.+)$/i);
  try {
    const task = await apiRequest("/tarefas", {
      method: "POST",
      body: { idIdoso: state.idosoId, pedido: text },
    });
    state.taskDraft = {
      id: task.id,
      contact: contactMatch ? contactMatch[1] : "seu contato",
      content: contentMatch ? contentMatch[1].trim() : text.trim(),
    };
    state.taskText = "";
    state.screen = "confirm";
    render();
  } catch (error) {
    showApiError(error);
  }
}

function focusSpeechInput() {
  let field = app.querySelector("#task-text, #scam-text");
  if (!field && state.screen !== "scam") {
    state.screen = "make";
    render();
    field = app.querySelector("#task-text");
  }
  field?.focus();
  return field;
}

function speechErrorMessage(error) {
  const messages = {
    "not-allowed": "Microfone bloqueado. Permita o acesso ao microfone no navegador e tente novamente.",
    "service-not-allowed": "O navegador não permitiu usar o serviço de voz. Você pode digitar o pedido.",
    "audio-capture": "Não encontrei um microfone disponível. Verifique o dispositivo e tente novamente.",
    "no-speech": "Não ouvi nada. Fale mais perto do microfone ou digite o pedido.",
    network: "Falha na conexão do serviço de voz. Verifique a internet ou digite o pedido.",
    "language-not-supported": "O ditado em português não está disponível. Você pode digitar o pedido.",
    aborted: "O ditado foi interrompido. Você pode tentar novamente ou digitar o pedido.",
  };
  return messages[error] || "Não consegui ouvir. Verifique a permissão do microfone ou digite o pedido.";
}

function beginListening() {
  const field = focusSpeechInput();
  const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!Recognition) {
    showToast("Ditado por voz indisponível neste navegador. Você pode escrever o pedido.");
    return;
  }

  let recognition;
  try {
    recognition = new Recognition();
  } catch {
    showToast("Não foi possível iniciar o ditado. Você pode escrever o pedido.");
    field?.focus();
    return;
  }

  recognition.lang = "pt-BR";
  recognition.interimResults = false;
  recognition.maxAlternatives = 1;
  let receivedResult = false;
  let receivedError = false;

  recognition.onresult = (event) => {
    receivedResult = true;
    const spokenText = event.results[0][0].transcript;
    const input = focusSpeechInput();
    if (input) {
      input.value = spokenText;
      input.dispatchEvent(new Event("input", { bubbles: true }));
    }
  };
  recognition.onerror = (event) => {
    receivedError = true;
    showToast(speechErrorMessage(event.error));
    focusSpeechInput();
  };
  recognition.onend = () => {
    if (!receivedResult && !receivedError) {
      showToast("Não ouvi nada. Tente falar novamente ou digite o pedido.");
      focusSpeechInput();
    }
  };

  showToast("Estou ouvindo. Pode falar.");
  try {
    recognition.start();
  } catch (error) {
    const message = error instanceof DOMException && error.name === "NotAllowedError"
      ? "Microfone bloqueado. Permita o acesso ao microfone no navegador."
      : error instanceof DOMException && error.name === "InvalidStateError"
        ? "O ditado já está em andamento. Aguarde ou digite o pedido."
        : "Não foi possível iniciar o ditado. Verifique o microfone ou digite o pedido.";
    showToast(message);
    field?.focus();
  }
}

function updateUndoCountdown() {
  if (state.screen !== "sent") return;
  const button = app.querySelector('[data-action="undo"]');
  if (!button) return;
  const secondsLeft = Math.max(0, Math.ceil((state.undoUntil - Date.now()) / 1000));
  button.disabled = secondsLeft === 0;
  button.innerHTML = `${icon("undo", "inline-icon")} &nbsp; Desfazer ${secondsLeft > 0 ? `(${secondsLeft})` : ""}`;
  if (secondsLeft > 0) window.setTimeout(updateUndoCountdown, 1000);
}

app.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (event.target.id === "task-form") {
    const text = new FormData(event.target).get("taskText").trim();
    if (text) await createTaskDraft(text);
  }
  if (event.target.id === "scam-form") {
    state.scamText = new FormData(event.target).get("scamText").trim();
    try {
      const result = await apiRequest("/golpes/verificar", {
        method: "POST",
        body: { idIdoso: state.idosoId, texto: state.scamText, imagem: false },
      });
      state.scamResult = {
        level: result.nivel === "MEDIO" ? "MÉDIO" : result.nivel,
        reason: result.motivo,
        advice: result.oQueFazer,
      };
      state.screen = "scamResult";
      render();
      if (state.silentMode) speak(`Risco ${state.scamResult.level}. ${state.scamResult.reason} ${state.scamResult.advice}`);
    } catch (error) {
      showApiError(error);
    }
  }
  if (event.target.id === "connection-form") {
    const form = new FormData(event.target);
    const apiUrl = String(form.get("apiUrl")).trim().replace(/\/+$/, "");
    const idosoId = Number(form.get("idosoId"));
    if (!apiUrl || !Number.isInteger(idosoId) || idosoId < 1) {
      showToast("Informe um endereço e um código de idoso válidos.");
      return;
    }
    state.apiBase = apiUrl;
    state.idosoId = idosoId;
    localStorage.setItem("netinho-api-url", apiUrl);
    localStorage.setItem("netinho-idoso-id", String(idosoId));
    try {
      await loadElder();
      if (!state.apiConnected) throw new Error("Não foi possível carregar o cadastro.");
      showToast(`Conectado ao cadastro de ${state.idosoNome}.`);
      render();
    } catch (error) {
      showApiError(error);
    }
  }
});

app.addEventListener("change", async (event) => {
  if (event.target.id === "silent-mode") {
    const ativo = event.target.checked;
    try {
      await apiRequest(`/idosos/${state.idosoId}/modo-sem-leitura`, {
        method: "PUT",
        body: { ativo },
      });
      state.silentMode = ativo;
      showToast(state.silentMode ? "Modo sem leitura ligado." : "Modo sem leitura desligado.");
    } catch (error) {
      showApiError(error);
      render();
    }
  }
});

app.addEventListener("input", (event) => {
  if (event.target.id !== "task-text") return;
  state.taskText = event.target.value;
  const preview = app.querySelector("[data-preview-message]");
  if (preview) {
    preview.textContent = state.taskText || "Sua mensagem vai aparecer aqui.";
  }
});

app.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-action]");
  if (!button) return;
  const action = button.dataset.action;

  if (action === "home") state.screen = "home";
  else if (action === "make") state.screen = "make";
  else if (action === "guide") {
    try {
      const task = await apiRequest("/tarefas", {
        method: "POST",
        body: { idIdoso: state.idosoId, pedido: "Quero pedir minha aposentadoria" },
      });
      state.guideTaskId = task.id;
      state.guideStep = 0;
      state.guideStepData = await apiRequest(`/tarefas/${task.id}/passo-atual`);
      state.screen = "guide";
    } catch (error) {
      showApiError(error);
    }
  } else if (action === "scam") state.screen = "scam";
  else if (action === "help") {
    state.screen = "help";
    try {
      await loadSupportNetwork();
    } catch (error) {
      showApiError(error);
    }
  } else if (action === "emergency") {
    state.screen = "emergency";
    try {
      await loadSupportNetwork();
    } catch (error) {
      showApiError(error);
    }
  }
  else if (action === "settings") state.screen = "settings";
  else if (action === "listen") beginListening();
  else if (action === "speak") {
    const text = document.querySelector(".face-line h1, .home-header h1")?.textContent;
    if (text) speak(text);
  } else if (action === "confirm-task") {
    try {
      await apiRequest(`/tarefas/${state.taskDraft.id}/confirmar`, {
        method: "PUT",
        body: { resposta: "SIM" },
      });
      state.undoUntil = Date.now() + 5000;
      state.screen = "sent";
    } catch (error) {
      showApiError(error);
    }
  } else if (action === "cancel-task") {
    try {
      await apiRequest(`/tarefas/${state.taskDraft.id}/confirmar`, {
        method: "PUT",
        body: { resposta: "NAO" },
      });
      state.screen = "home";
      showToast("Tudo bem. A tarefa foi cancelada.");
    } catch (error) {
      showApiError(error);
    }
  } else if (action === "undo") {
    if (Date.now() <= state.undoUntil) {
      try {
        await apiRequest(`/tarefas/${state.taskDraft.id}/desfazer`, { method: "PUT" });
        state.screen = "home";
        showToast("Tarefa desfeita na API.");
      } catch (error) {
        showApiError(error);
      }
    } else {
      showToast("O tempo para desfazer terminou.");
    }
  } else if (action === "advance-guide") {
    try {
      const result = await apiRequest(`/tarefas/${state.guideTaskId}/avancar`, {
        method: "PUT",
        body: { concluido: true },
      });
      if (result.concluido) {
        state.screen = "guideDone";
        state.guideStepData = null;
      } else {
        state.guideStep += 1;
        state.guideStepData = await apiRequest(`/tarefas/${state.guideTaskId}/passo-atual`);
      }
    } catch (error) {
      showApiError(error);
    }
  } else if (action === "explain-guide") {
    const alternative = guideSteps[state.guideStep]?.alternative
      || `Procure ${state.guideStepData?.nmPasElemento || "o elemento indicado"} na tela e toque nele.`;
    speak(alternative);
    showToast(alternative);
  } else if (action === "notify-family") {
    try {
      const help = await apiRequest("/atendimentos", {
        method: "POST",
        body: { idIdoso: state.idosoId, tentativas: 0 },
      });
      state.screen = "home";
      showToast(help.indicacao
        ? `Pedido registrado. ${help.indicacao} pode atender.`
        : "Pedido registrado. Não há atendente disponível agora.");
    } catch (error) {
      showApiError(error);
    }
  } else if (action === "sam-u") {
    showToast("Esta demonstração não inicia chamadas. Em uma emergência real, ligue 192.");
  }
  render();
});

app.addEventListener("pointerdown", (event) => {
  if (event.target.id !== "emergency-hold") return;
  const button = event.target;
  button.dataset.holdStarted = String(Date.now());
  button.textContent = "Continue segurando…";
  button.setPointerCapture(event.pointerId);
  button.dataset.holdTimer = String(window.setTimeout(() => {
    apiRequest("/emergencias", {
      method: "POST",
      body: { idIdoso: state.idosoId },
    }).then((result) => {
      const names = result.contatos.map((contact) => contact.nome).join(", ");
      showToast(names
        ? `Pedido de emergência registrado. Contatos: ${names}. ${result.ligar}.`
        : `Pedido de emergência registrado. ${result.ligar}.`);
      button.textContent = "Pedido registrado";
      button.disabled = true;
    }).catch((error) => {
      showApiError(error);
      button.textContent = "Segure por 2 segundos";
    });
  }, 2000));
});

app.addEventListener("pointerup", (event) => {
  if (event.target.id !== "emergency-hold") return;
  const button = event.target;
  if (!button.disabled) {
    window.clearTimeout(Number(button.dataset.holdTimer));
    button.textContent = "Segure por 2 segundos";
  }
});

app.addEventListener("pointercancel", (event) => {
  if (event.target.id !== "emergency-hold") return;
  const button = event.target;
  window.clearTimeout(Number(button.dataset.holdTimer));
  if (!button.disabled) button.textContent = "Segure por 2 segundos";
});

render();
loadElder().then(() => {
  if (state.screen === "home") render();
});
