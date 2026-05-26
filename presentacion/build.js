// Generador de la presentacion Billetera Fintech
const pptxgen = require("pptxgenjs");
const React = require("react");
const ReactDOMServer = require("react-dom/server");
const sharp = require("sharp");
const {
  FaUserShield, FaWallet, FaExchangeAlt, FaUndo, FaCalendarAlt,
  FaMedal, FaBell, FaProjectDiagram, FaChartBar, FaShieldAlt,
  FaCogs, FaCode, FaDatabase, FaServer, FaReact,
  FaSitemap, FaCheckCircle, FaPlayCircle, FaUsers, FaListOl
} = require("react-icons/fa");

// Paleta Ocean Gradient + acentos
const NAVY     = "0B1A33";
const DEEP     = "065A82";
const TEAL     = "1C7293";
const MINT     = "9AC8DB";
const ICE      = "EAF4FA";
const WHITE    = "FFFFFF";
const SLATE    = "1E293B";
const MUTED    = "64748B";
const CODE_BG  = "0F172A";
const CODE_FG  = "E2E8F0";
const KEYWORD  = "60A5FA";
const STRING   = "FBBF24";
const COMMENT  = "94A3B8";
const ACCENT   = "F59E0B";

const HEAD = "Cambria";
const BODY = "Calibri";
const MONO = "Consolas";

// ---------- Helpers de iconos ----------
function svgFor(IconComponent, color, size) {
  return ReactDOMServer.renderToStaticMarkup(
    React.createElement(IconComponent, { color, size: String(size) })
  );
}
async function iconPng(IconComponent, color, size = 256) {
  const svg = svgFor(IconComponent, color, size);
  const buf = await sharp(Buffer.from(svg)).png().toBuffer();
  return "image/png;base64," + buf.toString("base64");
}

// ---------- Helpers de slide ----------
function addFooter(slide, owner, n, total) {
  // numero de slide
  slide.addText(`${n} / ${total}`, {
    x: 9.0, y: 5.25, w: 0.9, h: 0.25,
    fontFace: BODY, fontSize: 9, color: MUTED, align: "right", margin: 0
  });
  // owner
  if (owner) {
    slide.addText(`Expone: ${owner}`, {
      x: 0.5, y: 5.25, w: 5, h: 0.25,
      fontFace: BODY, fontSize: 9, color: MUTED, align: "left", margin: 0
    });
  }
}

function addTitle(slide, text, sub) {
  slide.addText(text, {
    x: 0.5, y: 0.3, w: 9, h: 0.55,
    fontFace: HEAD, fontSize: 28, bold: true, color: NAVY,
    align: "left", margin: 0
  });
  if (sub) {
    slide.addText(sub, {
      x: 0.5, y: 0.85, w: 9, h: 0.3,
      fontFace: BODY, fontSize: 13, color: TEAL, italic: true,
      align: "left", margin: 0
    });
  }
}

// Code block oscuro con sintaxis basica
function addCodeBlock(slide, lines, x, y, w, h) {
  slide.addShape("rect", {
    x, y, w, h,
    fill: { color: CODE_BG },
    line: { color: SLATE, width: 0.5 }
  });
  // pequena banda titulo (dots tipo IDE)
  slide.addShape("ellipse", { x: x + 0.12, y: y + 0.10, w: 0.12, h: 0.12, fill: { color: "EF4444" }, line: { type: "none" } });
  slide.addShape("ellipse", { x: x + 0.28, y: y + 0.10, w: 0.12, h: 0.12, fill: { color: "F59E0B" }, line: { type: "none" } });
  slide.addShape("ellipse", { x: x + 0.44, y: y + 0.10, w: 0.12, h: 0.12, fill: { color: "10B981" }, line: { type: "none" } });

  // Cada line es {parts:[{text, color?}]}
  const runs = [];
  lines.forEach((ln, i) => {
    if (!ln.parts) {
      runs.push({ text: ln.text || "", options: { color: ln.color || CODE_FG, breakLine: i < lines.length - 1 } });
    } else {
      ln.parts.forEach((p, j) => {
        const isLast = j === ln.parts.length - 1;
        runs.push({
          text: p.text,
          options: { color: p.color || CODE_FG, bold: !!p.bold, breakLine: isLast && i < lines.length - 1 }
        });
      });
    }
  });
  slide.addText(runs, {
    x: x + 0.15, y: y + 0.35, w: w - 0.3, h: h - 0.45,
    fontFace: MONO, fontSize: 11, color: CODE_FG,
    valign: "top", margin: 0, lineSpacing: 16
  });
}

// Card blanco con titulo y descripcion
function addCard(slide, x, y, w, h, title, body, accent) {
  slide.addShape("rect", {
    x, y, w, h,
    fill: { color: WHITE },
    line: { color: "E2E8F0", width: 0.75 },
    shadow: { type: "outer", color: "0F172A", blur: 8, offset: 2, angle: 90, opacity: 0.08 }
  });
  slide.addText(title, {
    x: x + 0.2, y: y + 0.15, w: w - 0.4, h: 0.4,
    fontFace: HEAD, fontSize: 14, bold: true, color: accent || NAVY,
    margin: 0
  });
  slide.addText(body, {
    x: x + 0.2, y: y + 0.55, w: w - 0.4, h: h - 0.7,
    fontFace: BODY, fontSize: 11.5, color: SLATE,
    valign: "top", margin: 0
  });
}

// ---------- Helper para crear circulo de icono ----------
async function addIconCircle(slide, IconComponent, color, x, y, diameter) {
  slide.addShape("ellipse", {
    x, y, w: diameter, h: diameter,
    fill: { color },
    line: { type: "none" }
  });
  const icon = await iconPng(IconComponent, "FFFFFF", 256);
  const pad = diameter * 0.22;
  slide.addImage({
    data: icon,
    x: x + pad, y: y + pad, w: diameter - 2 * pad, h: diameter - 2 * pad
  });
}

// ====================================================================
async function build() {
  const pres = new pptxgen();
  pres.layout = "LAYOUT_16x9"; // 10 x 5.625
  pres.author = "Tomas Suaza, Byron Prieto, Andres Herrera";
  pres.title  = "Billetera Fintech - Estructuras de Datos 2026-1";

  const TOTAL = 18;

  // ====================== SLIDE 1 - PORTADA ======================
  let s = pres.addSlide();
  s.background = { color: NAVY };

  // Iconito grande de wallet centrado arriba
  await addIconCircle(s, FaWallet, TEAL, 4.5, 0.5, 1.0);

  s.addText("Billetera Fintech", {
    x: 0.5, y: 1.75, w: 9, h: 0.9,
    fontFace: HEAD, fontSize: 48, bold: true, color: WHITE,
    align: "center", margin: 0
  });
  s.addText("Plataforma de gestion de billeteras digitales y analitica de transacciones", {
    x: 0.5, y: 2.7, w: 9, h: 0.5,
    fontFace: BODY, fontSize: 16, color: MINT, italic: true,
    align: "center", margin: 0
  });

  // Linea sutil (no es accent line bajo titulo, es separador en zona inferior)
  s.addShape("rect", {
    x: 4.0, y: 3.35, w: 2.0, h: 0.03,
    fill: { color: TEAL }, line: { type: "none" }
  });

  s.addText("Proyecto final - Estructuras de Datos 2026-1", {
    x: 0.5, y: 3.55, w: 9, h: 0.35,
    fontFace: BODY, fontSize: 14, color: ICE, align: "center", margin: 0
  });

  s.addText("Integrantes", {
    x: 0.5, y: 4.05, w: 9, h: 0.3,
    fontFace: BODY, fontSize: 12, color: MINT, align: "center", bold: true, margin: 0
  });
  s.addText([
    { text: "Tomas Suaza   |   ", options: { color: WHITE } },
    { text: "Byron Prieto   |   ", options: { color: WHITE } },
    { text: "Andres Herrera", options: { color: WHITE } }
  ], {
    x: 0.5, y: 4.4, w: 9, h: 0.4,
    fontFace: BODY, fontSize: 15, align: "center", margin: 0
  });

  // ====================== SLIDE 2 - AGENDA (Tomas) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Agenda de la exposicion");

  const agenda = [
    { n: "01", t: "Descripcion del problema y objetivo", who: "Tomas" },
    { n: "02", t: "Arquitectura y stack tecnologico",    who: "Tomas" },
    { n: "03", t: "Estructuras clasicas: Hash, Lista, Pila, Cola de prioridad", who: "Byron" },
    { n: "04", t: "Estructuras avanzadas: Arbol, Cola FIFO, Grafo",            who: "Andres" },
    { n: "05", t: "Modulos derivados: analitica y deteccion de fraude",        who: "Andres" },
    { n: "06", t: "Demo en vivo y conclusiones",                                who: "Los tres" }
  ];
  agenda.forEach((it, i) => {
    const y = 1.35 + i * 0.6;
    s.addShape("ellipse", { x: 0.6, y: y, w: 0.45, h: 0.45, fill: { color: TEAL }, line: { type: "none" } });
    s.addText(it.n, {
      x: 0.6, y: y, w: 0.45, h: 0.45,
      fontFace: HEAD, fontSize: 12, bold: true, color: WHITE, align: "center", valign: "middle", margin: 0
    });
    s.addText(it.t, {
      x: 1.25, y: y, w: 6.5, h: 0.45,
      fontFace: BODY, fontSize: 14, color: SLATE, valign: "middle", margin: 0
    });
    s.addText(it.who, {
      x: 7.9, y: y, w: 1.6, h: 0.45,
      fontFace: BODY, fontSize: 11, color: DEEP, bold: true, align: "right", valign: "middle", margin: 0
    });
  });
  addFooter(s, "Equipo", 2, TOTAL);

  // ====================== SLIDE 3 - PROBLEMA (Tomas) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "El problema", "Que necesita una plataforma fintech moderna");

  await addIconCircle(s, FaUserShield, DEEP, 0.5, 1.4, 0.55);
  s.addText("Manejo de volumen", {
    x: 1.2, y: 1.4, w: 4, h: 0.3,
    fontFace: HEAD, fontSize: 13, bold: true, color: NAVY, margin: 0
  });
  s.addText("Miles de transacciones por usuario, consultas instantaneas y reportes en linea.", {
    x: 1.2, y: 1.7, w: 8.3, h: 0.5,
    fontFace: BODY, fontSize: 12, color: SLATE, margin: 0
  });

  await addIconCircle(s, FaExchangeAlt, TEAL, 0.5, 2.4, 0.55);
  s.addText("Operaciones complejas", {
    x: 1.2, y: 2.4, w: 4, h: 0.3,
    fontFace: HEAD, fontSize: 13, bold: true, color: NAVY, margin: 0
  });
  s.addText("Recargas, retiros, transferencias internas y externas, programaciones, reversiones.", {
    x: 1.2, y: 2.7, w: 8.3, h: 0.5,
    fontFace: BODY, fontSize: 12, color: SLATE, margin: 0
  });

  await addIconCircle(s, FaChartBar, ACCENT, 0.5, 3.4, 0.55);
  s.addText("Analitica y deteccion de patrones", {
    x: 1.2, y: 3.4, w: 6, h: 0.3,
    fontFace: HEAD, fontSize: 13, bold: true, color: NAVY, margin: 0
  });
  s.addText("Reportes de uso, top usuarios, top transacciones por valor, deteccion de fraude.", {
    x: 1.2, y: 3.7, w: 8.3, h: 0.5,
    fontFace: BODY, fontSize: 12, color: SLATE, margin: 0
  });

  // Callout objetivo
  s.addShape("rect", {
    x: 0.5, y: 4.45, w: 9, h: 0.65,
    fill: { color: ICE }, line: { color: MINT, width: 1 }
  });
  s.addText([
    { text: "Objetivo: ", options: { bold: true, color: NAVY } },
    { text: "demostrar tecnicamente como la eleccion correcta de cada estructura de datos resuelve un problema del dominio fintech.", options: { color: SLATE } }
  ], {
    x: 0.7, y: 4.5, w: 8.6, h: 0.55, fontFace: BODY, fontSize: 12.5, valign: "middle", margin: 0
  });

  addFooter(s, "Tomas Suaza", 3, TOTAL);

  // ====================== SLIDE 4 - MODULOS (Tomas) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Modulos implementados");

  const modulos = [
    { icon: FaUsers,          c: DEEP,   t: "Usuarios y billeteras",       d: "Multi-billetera por usuario con 5 categorias." },
    { icon: FaExchangeAlt,    c: TEAL,   t: "Operaciones financieras",     d: "Recarga, retiro, transferencia interna y externa." },
    { icon: FaUndo,           c: ACCENT, t: "Reversion de operaciones",    d: "Deshacer ultima con pila por usuario." },
    { icon: FaCalendarAlt,    c: DEEP,   t: "Operaciones programadas",     d: "Cola de prioridad por fecha de ejecucion." },
    { icon: FaMedal,          c: TEAL,   t: "Puntos y niveles",            d: "Ranking por TreeMap con consultas por rango." },
    { icon: FaBell,           c: ACCENT, t: "Notificaciones",              d: "Cola FIFO por usuario, eventos del sistema." },
    { icon: FaProjectDiagram, c: DEEP,   t: "Red de transferencias",       d: "Grafo dirigido y ponderado entre usuarios." },
    { icon: FaChartBar,       c: TEAL,   t: "Analitica de movimientos",    d: "Tops, frecuencias, distribuciones." },
    { icon: FaShieldAlt,      c: ACCENT, t: "Deteccion de fraude",         d: "Reglas con ventana temporal sobre historial." }
  ];
  // grid 3x3
  for (let i = 0; i < modulos.length; i++) {
    const col = i % 3, row = Math.floor(i / 3);
    const x = 0.4 + col * 3.15;
    const y = 1.3 + row * 1.27;
    s.addShape("rect", {
      x, y, w: 2.95, h: 1.12,
      fill: { color: WHITE }, line: { color: "E2E8F0", width: 0.75 },
      shadow: { type: "outer", color: "0F172A", blur: 6, offset: 1, angle: 90, opacity: 0.08 }
    });
    await addIconCircle(s, modulos[i].icon, modulos[i].c, x + 0.15, y + 0.15, 0.5);
    s.addText(modulos[i].t, {
      x: x + 0.75, y: y + 0.15, w: 2.1, h: 0.3,
      fontFace: HEAD, fontSize: 11, bold: true, color: NAVY, margin: 0
    });
    s.addText(modulos[i].d, {
      x: x + 0.15, y: y + 0.65, w: 2.65, h: 0.42,
      fontFace: BODY, fontSize: 9.5, color: SLATE, margin: 0
    });
  }
  addFooter(s, "Tomas Suaza", 4, TOTAL);

  // ====================== SLIDE 5 - ARQUITECTURA (Tomas) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Arquitectura por capas");

  // Capas verticales
  const capas = [
    { t: "Frontend",   d: "React 18 + Vite + Tailwind",  c: DEEP,  y: 1.35 },
    { t: "API REST",   d: "Spring Boot 3 + Java 17",      c: TEAL,  y: 2.20 },
    { t: "Services",   d: "Logica de negocio",            c: ACCENT, y: 3.05 },
    { t: "Repositorios", d: "Estructuras en memoria",     c: DEEP,  y: 3.90 }
  ];
  capas.forEach(cap => {
    s.addShape("rect", {
      x: 0.6, y: cap.y, w: 5.0, h: 0.7,
      fill: { color: cap.c }, line: { type: "none" }
    });
    s.addText(cap.t, {
      x: 0.8, y: cap.y, w: 2.0, h: 0.7,
      fontFace: HEAD, fontSize: 14, bold: true, color: WHITE, valign: "middle", margin: 0
    });
    s.addText(cap.d, {
      x: 3.0, y: cap.y, w: 2.5, h: 0.7,
      fontFace: BODY, fontSize: 11, color: WHITE, valign: "middle", align: "right", margin: 0
    });
  });

  // Flechas verticales entre capas
  [2.05, 2.90, 3.75].forEach(y => {
    s.addText("▼", {
      x: 2.9, y: y, w: 0.4, h: 0.18,
      fontFace: BODY, fontSize: 12, color: MUTED, align: "center", margin: 0
    });
  });

  // Panel lateral con stack
  s.addShape("rect", {
    x: 6.6, y: 1.35, w: 2.9, h: 3.25,
    fill: { color: ICE }, line: { color: MINT, width: 1 }
  });
  s.addText("Stack tecnico", {
    x: 6.8, y: 1.45, w: 2.5, h: 0.35,
    fontFace: HEAD, fontSize: 14, bold: true, color: NAVY, margin: 0
  });
  s.addText([
    { text: "Backend:", options: { bold: true, color: DEEP, breakLine: true } },
    { text: "Java 17 + Spring Boot 3", options: { color: SLATE, breakLine: true } },
    { text: "Puerto 8081", options: { color: SLATE, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Frontend:", options: { bold: true, color: DEEP, breakLine: true } },
    { text: "React 18 + Vite + Tailwind", options: { color: SLATE, breakLine: true } },
    { text: "Puerto 5173", options: { color: SLATE, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Persistencia:", options: { bold: true, color: DEEP, breakLine: true } },
    { text: "En memoria,", options: { color: SLATE, breakLine: true } },
    { text: "estructuras de datos puras", options: { color: SLATE } }
  ], {
    x: 6.8, y: 1.85, w: 2.5, h: 2.7,
    fontFace: BODY, fontSize: 10.5, valign: "top", margin: 0, lineSpacing: 14
  });

  addFooter(s, "Tomas Suaza", 5, TOTAL);

  // ====================== SLIDE 6 - RESUMEN ESTRUCTURAS (Tomas) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Mapa de estructuras de datos", "Una estructura distinta por cada modulo, todas justificadas");

  const tabla = [
    [
      { text: "Modulo",     options: { bold: true, color: WHITE, fill: { color: NAVY }, align: "left" } },
      { text: "Estructura", options: { bold: true, color: WHITE, fill: { color: NAVY }, align: "left" } },
      { text: "Operacion clave", options: { bold: true, color: WHITE, fill: { color: NAVY }, align: "center" } }
    ],
    [ "Repositorios por id",        "HashMap",          { text: "O(1)", options: { align: "center", bold: true, color: DEEP } } ],
    [ "Historial de transacciones", "LinkedList addFirst", { text: "O(1)", options: { align: "center", bold: true, color: DEEP } } ],
    [ "Reversion (deshacer)",       "ArrayDeque (Pila)", { text: "O(1)", options: { align: "center", bold: true, color: DEEP } } ],
    [ "Operaciones programadas",    "PriorityQueue",    { text: "O(log n)", options: { align: "center", bold: true, color: DEEP } } ],
    [ "Ranking de fidelizacion",    "TreeMap",          { text: "O(log n + k)", options: { align: "center", bold: true, color: DEEP } } ],
    [ "Notificaciones",             "LinkedList FIFO (Cola)", { text: "O(1)", options: { align: "center", bold: true, color: DEEP } } ],
    [ "Red de transferencias",      "Grafo (lista adyacencia)", { text: "O(V+E)", options: { align: "center", bold: true, color: DEEP } } ],
    [ "Top transacciones por valor", "TreeSet ordenado",  { text: "O(log n)", options: { align: "center", bold: true, color: DEEP } } ]
  ];
  s.addTable(tabla, {
    x: 0.5, y: 1.35, w: 9, colW: [3.5, 3.5, 2],
    rowH: 0.36,
    fontFace: BODY, fontSize: 11, color: SLATE,
    border: { pt: 0.5, color: "E2E8F0" }, valign: "middle"
  });

  addFooter(s, "Tomas Suaza", 6, TOTAL);

  // ====================== SLIDE 7 - HashMap (Byron) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "HashMap - repositorios principales", "Acceso por id en O(1) promedio");

  addCodeBlock(s, [
    { parts: [
      { text: "// UsuarioRepository.java", color: COMMENT }
    ]},
    { parts: [
      { text: "@Repository", color: KEYWORD }
    ]},
    { parts: [
      { text: "public class ", color: KEYWORD },
      { text: "UsuarioRepository {", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [
      { text: "  private final ", color: KEYWORD },
      { text: "Map<String, Usuario> usuarios", color: CODE_FG }
    ]},
    { parts: [
      { text: "      = ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "HashMap<>();", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [
      { text: "  public ", color: KEYWORD },
      { text: "Optional<Usuario> ", color: CODE_FG },
      { text: "buscarPorId", color: STRING },
      { text: "(String id) {", color: CODE_FG }
    ]},
    { parts: [
      { text: "    return ", color: KEYWORD },
      { text: "Optional.ofNullable(usuarios.get(id));", color: CODE_FG }
    ]},
    { parts: [{ text: "  }", color: CODE_FG }] },
    { parts: [{ text: "}", color: CODE_FG }] }
  ], 0.5, 1.4, 5.5, 3.5);

  addCard(s, 6.2, 1.4, 3.3, 1.05, "Por que aqui",
    "El acceso por id es la operacion mas frecuente del sistema.", TEAL);
  addCard(s, 6.2, 2.55, 3.3, 1.05, "Alternativas descartadas",
    "ArrayList: O(n) por id. TreeMap: O(log n) pero no necesitamos orden.", DEEP);
  addCard(s, 6.2, 3.70, 3.3, 1.2, "Complejidad",
    "get / put / remove: O(1) promedio. listar: O(n).", ACCENT);

  addFooter(s, "Byron Prieto", 7, TOTAL);

  // ====================== SLIDE 8 - LinkedList historial (Byron) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "LinkedList - historial de transacciones", "addFirst para mantener orden cronologico inverso");

  addCodeBlock(s, [
    { parts: [{ text: "// TransaccionRepository.guardar(...)", color: COMMENT }] },
    { parts: [
      { text: "historialPorBilletera", color: CODE_FG }
    ]},
    { parts: [
      { text: "  .", color: CODE_FG },
      { text: "computeIfAbsent", color: STRING },
      { text: "(idBilletera,", color: CODE_FG }
    ]},
    { parts: [
      { text: "      k -> ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "LinkedList<>())", color: CODE_FG }
    ]},
    { parts: [
      { text: "  .", color: CODE_FG },
      { text: "addFirst", color: STRING },
      { text: "(t.getId());", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [{ text: "// ArrayList.add(0, x) seria O(n)", color: COMMENT }] },
    { parts: [{ text: "// LinkedList.addFirst() es O(1)",  color: COMMENT }] }
  ], 0.5, 1.4, 5.5, 3.5);

  addCard(s, 6.2, 1.4, 3.3, 1.15, "Por que LinkedList",
    "Las transacciones se insertan SIEMPRE al inicio del historial. addFirst en LinkedList es O(1).", TEAL);
  addCard(s, 6.2, 2.65, 3.3, 1.15, "Decision de diseño",
    "Guardamos solo los IDs en la lista, no objetos completos. El HashMap principal resuelve la transaccion.", DEEP);
  addCard(s, 6.2, 3.90, 3.3, 1.0, "Complejidad",
    "guardar: O(1). historialBilletera: O(k).", ACCENT);

  addFooter(s, "Byron Prieto", 8, TOTAL);

  // ====================== SLIDE 9 - ArrayDeque Pila (Byron) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "ArrayDeque como Pila - reversion", "LIFO para deshacer la ultima operacion del usuario");

  addCodeBlock(s, [
    { parts: [{ text: "// ReversionRepository.java", color: COMMENT }] },
    { parts: [
      { text: "private final ", color: KEYWORD },
      { text: "Map<String, Deque<Transaccion>>", color: CODE_FG }
    ]},
    { parts: [
      { text: "  pilasPorUsuario = ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "HashMap<>();", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [
      { text: "public void ", color: KEYWORD },
      { text: "apilar", color: STRING },
      { text: "(String idUsuario, Transaccion t) {", color: CODE_FG }
    ]},
    { parts: [
      { text: "  pilasPorUsuario", color: CODE_FG }
    ]},
    { parts: [
      { text: "    .", color: CODE_FG },
      { text: "computeIfAbsent", color: STRING },
      { text: "(idUsuario, k ->", color: CODE_FG }
    ]},
    { parts: [
      { text: "        ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "ArrayDeque<>())", color: CODE_FG }
    ]},
    { parts: [
      { text: "    .", color: CODE_FG },
      { text: "push", color: STRING },
      { text: "(t);", color: CODE_FG }
    ]},
    { parts: [{ text: "}", color: CODE_FG }] }
  ], 0.5, 1.4, 5.5, 3.5);

  addCard(s, 6.2, 1.4, 3.3, 1.15, "Por que ArrayDeque y no Stack",
    "Stack hereda de Vector con overhead de sincronizacion. La doc oficial de Java recomienda ArrayDeque.", TEAL);
  addCard(s, 6.2, 2.65, 3.3, 1.15, "Marcado en vez de pop",
    "Al revertir, marcamos como REVERTIDA y la siguiente operacion la salta. Permite reversiones repetidas.", DEEP);
  addCard(s, 6.2, 3.90, 3.3, 1.0, "Complejidad",
    "push / pop / peek: O(1).", ACCENT);

  addFooter(s, "Byron Prieto", 9, TOTAL);

  // ====================== SLIDE 10 - PriorityQueue (Byron) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "PriorityQueue - operaciones programadas", "Heap binario ordenado por fecha de ejecucion");

  addCodeBlock(s, [
    { parts: [{ text: "// ProgramacionRepository", color: COMMENT }] },
    { parts: [
      { text: "private final ", color: KEYWORD },
      { text: "PriorityQueue<OperacionProgramada>", color: CODE_FG }
    ]},
    { parts: [
      { text: "  cola = ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "PriorityQueue<>();", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [{ text: "// OperacionProgramada implements Comparable", color: COMMENT }] },
    { parts: [
      { text: "public int ", color: KEYWORD },
      { text: "compareTo", color: STRING },
      { text: "(OperacionProgramada otra) {", color: CODE_FG }
    ]},
    { parts: [
      { text: "  return ", color: KEYWORD },
      { text: "this.fechaEjecucion", color: CODE_FG }
    ]},
    { parts: [
      { text: "    .", color: CODE_FG },
      { text: "compareTo", color: STRING },
      { text: "(otra.fechaEjecucion);", color: CODE_FG }
    ]},
    { parts: [{ text: "}", color: CODE_FG }] }
  ], 0.5, 1.4, 5.5, 3.5);

  addCard(s, 6.2, 1.4, 3.3, 1.15, "Por que aqui",
    "Si programo algo para manana y luego para hoy, la segunda debe ejecutarse primero. El heap garantiza ese orden.", TEAL);
  addCard(s, 6.2, 2.65, 3.3, 1.15, "Truco al listar todas",
    "PriorityQueue solo garantiza orden en la cabeza. Para listar todas: copia + Collections.sort.", DEEP);
  addCard(s, 6.2, 3.90, 3.3, 1.0, "Complejidad",
    "offer: O(log n). peek: O(1). poll: O(log n).", ACCENT);

  addFooter(s, "Byron Prieto", 10, TOTAL);

  // ====================== SLIDE 11 - TreeMap (Andres) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "TreeMap - ranking de fidelizacion", "Arbol rojo-negro para consultas por rango");

  addCodeBlock(s, [
    { parts: [{ text: "// FidelizacionRepository", color: COMMENT }] },
    { parts: [
      { text: "private final ", color: KEYWORD },
      { text: "TreeMap<Integer, Set<String>>", color: CODE_FG }
    ]},
    { parts: [
      { text: "  ranking = ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "TreeMap<>();", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [{ text: "// 'Usuarios entre min y max puntos'", color: COMMENT }] },
    { parts: [
      { text: "public ", color: KEYWORD },
      { text: "Set<String> ", color: CODE_FG },
      { text: "usuariosEnRango", color: STRING },
      { text: "(", color: CODE_FG }
    ]},
    { parts: [{ text: "    int min, int max) {", color: CODE_FG }] },
    { parts: [
      { text: "  return ", color: KEYWORD },
      { text: "ranking.", color: CODE_FG },
      { text: "subMap", color: STRING },
      { text: "(min, true, max, true)", color: CODE_FG }
    ]},
    { parts: [
      { text: "    .values().stream()", color: CODE_FG }
    ]},
    { parts: [
      { text: "    .flatMap(Set::stream).", color: CODE_FG },
      { text: "collect", color: STRING },
      { text: "(...);", color: CODE_FG }
    ]},
    { parts: [{ text: "}", color: CODE_FG }] }
  ], 0.5, 1.4, 5.5, 3.6);

  addCard(s, 6.2, 1.4, 3.3, 1.25, "Por que TreeMap",
    "HashMap no preserva orden: filtrar por rango seria O(n). subMap salta al rango exacto.", TEAL);
  addCard(s, 6.2, 2.75, 3.3, 1.15, "Diseño Integer -> Set",
    "Varios usuarios pueden empatar en puntos. La clave es el puntaje, el valor es el conjunto de IDs.", DEEP);
  addCard(s, 6.2, 3.95, 3.3, 0.95, "Complejidad",
    "actualizar: O(log n). rango: O(log n + k).", ACCENT);

  addFooter(s, "Andres Herrera", 11, TOTAL);

  // ====================== SLIDE 12 - Cola FIFO (Andres) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "LinkedList como Cola FIFO - notificaciones", "Buzon por usuario con offer y poll");

  addCodeBlock(s, [
    { parts: [{ text: "// NotificacionRepository", color: COMMENT }] },
    { parts: [
      { text: "private final ", color: KEYWORD },
      { text: "Map<String, LinkedList<Notificacion>>", color: CODE_FG }
    ]},
    { parts: [
      { text: "  buzones = ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "HashMap<>();", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [
      { text: "public ", color: KEYWORD },
      { text: "Notificacion ", color: CODE_FG },
      { text: "encolar", color: STRING },
      { text: "(Notificacion n) {", color: CODE_FG }
    ]},
    { parts: [
      { text: "  buzones.", color: CODE_FG },
      { text: "computeIfAbsent", color: STRING },
      { text: "(n.getIdUsuario(),", color: CODE_FG }
    ]},
    { parts: [
      { text: "      k -> ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "LinkedList<>())", color: CODE_FG }
    ]},
    { parts: [
      { text: "    .", color: CODE_FG },
      { text: "offer", color: STRING },
      { text: "(n);", color: CODE_FG }
    ]},
    { parts: [
      { text: "  return ", color: KEYWORD },
      { text: "n;", color: CODE_FG }
    ]},
    { parts: [{ text: "}", color: CODE_FG }] }
  ], 0.5, 1.4, 5.5, 3.6);

  addCard(s, 6.2, 1.4, 3.3, 1.25, "FIFO vs LIFO",
    "Misma clase Java. Aqui offer al final + poll del inicio. Las mas antiguas se procesan primero.", TEAL);
  addCard(s, 6.2, 2.75, 3.3, 1.25, "Eventos del sistema",
    "Bienvenida, saldo bajo, ascenso de nivel, operacion rechazada, programadas, fraude detectado.", DEEP);
  addCard(s, 6.2, 4.05, 3.3, 0.85, "Complejidad",
    "offer / poll / peek: O(1).", ACCENT);

  addFooter(s, "Andres Herrera", 12, TOTAL);

  // ====================== SLIDE 13 - Grafo (Andres) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Grafo dirigido y ponderado - red de transferencias", "Lista de adyacencia con doble HashMap");

  addCodeBlock(s, [
    { parts: [{ text: "// GrafoTransferenciasRepository", color: COMMENT }] },
    { parts: [
      { text: "private final ", color: KEYWORD },
      { text: "Map<String, Map<String, Arista>>", color: CODE_FG }
    ]},
    { parts: [
      { text: "  adyacencia = ", color: CODE_FG },
      { text: "new ", color: KEYWORD },
      { text: "HashMap<>();", color: CODE_FG }
    ]},
    { text: "" },
    { parts: [{ text: "// Operaciones implementadas:", color: COMMENT }] },
    { parts: [{ text: "//  - BFS por nivel (amigos de amigos)", color: COMMENT }] },
    { parts: [{ text: "//  - Camino mas corto con BFS + padres", color: COMMENT }] },
    { parts: [{ text: "//  - Deteccion de ciclos (DFS 3 colores)", color: COMMENT }] },
    { parts: [{ text: "//  - Rutas frecuentes (orden por peso)", color: COMMENT }] }
  ], 0.5, 1.4, 5.5, 3.0);

  // Diagramita de un grafito con 3 nodos
  s.addText("Ejemplo de ciclo detectado:", {
    x: 0.5, y: 4.50, w: 5.5, h: 0.25,
    fontFace: BODY, fontSize: 11, color: NAVY, bold: true, margin: 0
  });
  // Tres nodos circulares
  const nodos = [
    { n: "Andres",   x: 0.7, y: 4.80 },
    { n: "Gayron",   x: 2.5, y: 4.80 },
    { n: "Susanita", x: 4.3, y: 4.80 }
  ];
  nodos.forEach(nd => {
    s.addShape("ellipse", { x: nd.x, y: nd.y, w: 1.3, h: 0.4, fill: { color: ICE }, line: { color: TEAL, width: 1.5 } });
    s.addText(nd.n, { x: nd.x, y: nd.y, w: 1.3, h: 0.4, fontFace: BODY, fontSize: 10, color: NAVY, align: "center", valign: "middle", margin: 0 });
  });
  // Flechas A->G, G->S, S->A (con texto)
  s.addText("→", { x: 2.0, y: 4.80, w: 0.5, h: 0.4, fontFace: BODY, fontSize: 18, color: TEAL, align: "center", valign: "middle", margin: 0, bold: true });
  s.addText("→", { x: 3.8, y: 4.80, w: 0.5, h: 0.4, fontFace: BODY, fontSize: 18, color: TEAL, align: "center", valign: "middle", margin: 0, bold: true });
  // Flecha de regreso (curva representada como linea)
  s.addText("Susanita → Andres (cierra el ciclo)", {
    x: 0.5, y: 5.20, w: 5.5, h: 0.18,
    fontFace: BODY, fontSize: 9, color: ACCENT, italic: true, margin: 0
  });

  addCard(s, 6.2, 1.4, 3.3, 1.15, "Por que lista de adyacencia",
    "Grafo esparso: matriz seria O(V^2) casi vacia. Lista usa O(V+E).", TEAL);
  addCard(s, 6.2, 2.55, 3.3, 1.15, "Doble HashMap interno",
    "Para ya-existe-arista en O(1) al acumular peso (en List<Arista> seria O(grado)).", DEEP);
  addCard(s, 6.2, 3.70, 3.3, 1.2, "Complejidad",
    "BFS / DFS / ciclos: O(V+E). Rutas frecuentes: O(E log E).", ACCENT);

  addFooter(s, "Andres Herrera", 13, TOTAL);

  // ====================== SLIDE 14 - Analitica + Fraude (Andres) ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Modulos derivados", "Analitica y deteccion de fraude reusan las estructuras anteriores");

  // Dos columnas
  s.addShape("rect", {
    x: 0.5, y: 1.3, w: 4.4, h: 3.8,
    fill: { color: WHITE }, line: { color: "E2E8F0", width: 0.75 },
    shadow: { type: "outer", color: "0F172A", blur: 6, offset: 1, angle: 90, opacity: 0.08 }
  });
  await addIconCircle(s, FaChartBar, TEAL, 0.7, 1.45, 0.55);
  s.addText("Analitica de movimientos", {
    x: 1.4, y: 1.45, w: 3.4, h: 0.4,
    fontFace: HEAD, fontSize: 14, bold: true, color: NAVY, margin: 0, valign: "middle"
  });
  s.addText([
    { text: "Top usuarios activos", options: { bullet: true, breakLine: true } },
    { text: "Top billeteras mas usadas", options: { bullet: true, breakLine: true } },
    { text: "Frecuencia por tipo de transaccion", options: { bullet: true, breakLine: true } },
    { text: "Monto movilizado por rango de fechas", options: { bullet: true, breakLine: true } },
    { text: "Top transacciones por valor (TreeSet ordenado)", options: { bullet: true } }
  ], {
    x: 0.7, y: 2.15, w: 4.0, h: 2.9,
    fontFace: BODY, fontSize: 11.5, color: SLATE, margin: 0, paraSpaceAfter: 6
  });

  s.addShape("rect", {
    x: 5.1, y: 1.3, w: 4.4, h: 3.8,
    fill: { color: WHITE }, line: { color: "E2E8F0", width: 0.75 },
    shadow: { type: "outer", color: "0F172A", blur: 6, offset: 1, angle: 90, opacity: 0.08 }
  });
  await addIconCircle(s, FaShieldAlt, ACCENT, 5.3, 1.45, 0.55);
  s.addText("Deteccion de fraude", {
    x: 6.0, y: 1.45, w: 3.4, h: 0.4,
    fontFace: HEAD, fontSize: 14, bold: true, color: NAVY, margin: 0, valign: "middle"
  });
  s.addText([
    { text: "Rafaga: 4+ transferencias en 5 min", options: { bullet: true, breakLine: true } },
    { text: "Mismo destino repetido en 10 min", options: { bullet: true, breakLine: true } },
    { text: "Monto 5x mayor al promedio del usuario", options: { bullet: true, breakLine: true } },
    { text: "Fragmentacion entre billeteras", options: { bullet: true, breakLine: true } },
    { text: "Cada deteccion encola FRAUDE_DETECTADO en la cola FIFO del usuario", options: { bullet: true, color: ACCENT, bold: true } }
  ], {
    x: 5.3, y: 2.15, w: 4.0, h: 2.9,
    fontFace: BODY, fontSize: 11.5, color: SLATE, margin: 0, paraSpaceAfter: 6
  });

  addFooter(s, "Andres Herrera", 14, TOTAL);

  // ====================== SLIDE 15 - DIAGRAMA DE ESTRUCTURAS ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Mapa: estructura -> repositorio que la usa");

  // Caja central con titulo
  const izq = [
    { e: "HashMap",            r: "Usuario, Billetera, Transaccion, Notificacion" },
    { e: "LinkedList (LIFO)",  r: "Historial de transacciones" },
    { e: "LinkedList (FIFO)",  r: "Buzon de notificaciones" },
    { e: "ArrayDeque (Pila)",  r: "Reversion por usuario" },
    { e: "PriorityQueue",      r: "Operaciones programadas" },
    { e: "TreeMap",            r: "Ranking de fidelizacion" },
    { e: "TreeSet",            r: "Top transacciones por valor" },
    { e: "Grafo (adyacencia)", r: "Red de transferencias" }
  ];
  izq.forEach((it, i) => {
    const y = 1.30 + i * 0.5;
    // izq box estructura
    s.addShape("rect", {
      x: 0.5, y, w: 3.0, h: 0.42,
      fill: { color: NAVY }, line: { type: "none" }
    });
    s.addText(it.e, {
      x: 0.6, y, w: 2.8, h: 0.42,
      fontFace: HEAD, fontSize: 11, bold: true, color: WHITE, valign: "middle", margin: 0
    });
    // arrow simulado
    s.addText("→", {
      x: 3.55, y, w: 0.4, h: 0.42,
      fontFace: BODY, fontSize: 16, color: TEAL, align: "center", valign: "middle", margin: 0, bold: true
    });
    // der box repositorio
    s.addShape("rect", {
      x: 4.0, y, w: 5.5, h: 0.42,
      fill: { color: ICE }, line: { color: MINT, width: 0.75 }
    });
    s.addText(it.r, {
      x: 4.15, y, w: 5.3, h: 0.42,
      fontFace: BODY, fontSize: 11, color: SLATE, valign: "middle", margin: 0
    });
  });

  addFooter(s, "Equipo", 15, TOTAL);

  // ====================== SLIDE 16 - DEMO FLOW ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Demo en vivo", "Recorrido sugerido para mostrar todos los modulos");

  const pasos = [
    { n: "1", t: "Crear usuarios y billeteras",      d: "Andres, Gayron y Susanita (precargados por DataInitializer)." },
    { n: "2", t: "Recargar y transferir",             d: "Mostrar cambios de saldo y notificaciones automaticas." },
    { n: "3", t: "Reversion de operacion",            d: "Boton 'Deshacer ultima' usa la pila por usuario." },
    { n: "4", t: "Ejecutar programadas",              d: "Vencida y futura, el heap procesa la mas antigua." },
    { n: "5", t: "Ver Ranking y filtro por rango",    d: "TreeMap responde subMap(min, max)." },
    { n: "6", t: "Red de transferencias",             d: "Lista de aristas, BFS amigos de amigos, ciclo detectado." },
    { n: "7", t: "Analitica y Auditoria",             d: "Top usuarios, top valor, eventos de fraude." },
    { n: "8", t: "Notificaciones y despachar FIFO",   d: "Drenar el buzon en orden de llegada." }
  ];
  pasos.forEach((p, i) => {
    const col = i % 2, row = Math.floor(i / 2);
    const x = 0.4 + col * 4.7;
    const y = 1.35 + row * 0.95;
    s.addShape("ellipse", { x, y: y + 0.05, w: 0.5, h: 0.5, fill: { color: TEAL }, line: { type: "none" } });
    s.addText(p.n, {
      x, y: y + 0.05, w: 0.5, h: 0.5,
      fontFace: HEAD, fontSize: 14, bold: true, color: WHITE, align: "center", valign: "middle", margin: 0
    });
    s.addText(p.t, {
      x: x + 0.6, y: y, w: 3.85, h: 0.32,
      fontFace: HEAD, fontSize: 12, bold: true, color: NAVY, margin: 0
    });
    s.addText(p.d, {
      x: x + 0.6, y: y + 0.33, w: 3.85, h: 0.55,
      fontFace: BODY, fontSize: 10.5, color: SLATE, margin: 0
    });
  });

  addFooter(s, "Equipo", 16, TOTAL);

  // ====================== SLIDE 17 - COBERTURA ENUNCIADO ======================
  s = pres.addSlide();
  s.background = { color: WHITE };
  addTitle(s, "Cobertura del enunciado", "Todos los requisitos cubiertos");

  const cobertura = [
    [
      { text: "Bloque del PDF", options: { bold: true, color: WHITE, fill: { color: NAVY } } },
      { text: "Cubierto en",     options: { bold: true, color: WHITE, fill: { color: NAVY } } }
    ],
    [ "4.1 Usuarios y billeteras",        "UsuarioService, BilleteraService" ],
    [ "4.2 Operaciones financieras",      "TransaccionService" ],
    [ "4.3 Operaciones programadas",      "ProgramacionService + PriorityQueue" ],
    [ "4.4 Recompensas (puntos)",         "PoliticaPuntos + Usuario" ],
    [ "4.5 Niveles",                       "NivelUsuario + ascenso automatico" ],
    [ "4.6 Reversion",                     "ReversionService + ArrayDeque" ],
    [ "4.7 Alertas y notificaciones",     "NotificacionService + LinkedList FIFO" ],
    [ "4.8 Analitica",                     "AnaliticaService + TreeSet" ],
    [ "4.9 Deteccion de comportamiento", "FraudeService + ventanas temporales" ],
    [ "5.6 Grafos",                        "GrafoService + lista de adyacencia" ],
    [ "9. Entregables (informe, diagrama)", "docs/INFORME_TECNICO.md, DIAGRAMA_CLASES.md" ]
  ];
  s.addTable(cobertura, {
    x: 0.5, y: 1.3, w: 9, colW: [4.5, 4.5],
    rowH: 0.30,
    fontFace: BODY, fontSize: 10.5, color: SLATE,
    border: { pt: 0.5, color: "E2E8F0" }, valign: "middle"
  });

  addFooter(s, "Equipo", 17, TOTAL);

  // ====================== SLIDE 18 - CIERRE ======================
  s = pres.addSlide();
  s.background = { color: NAVY };

  s.addText("Gracias", {
    x: 0.5, y: 1.5, w: 9, h: 1.2,
    fontFace: HEAD, fontSize: 60, bold: true, color: WHITE, align: "center", margin: 0
  });
  s.addText("Preguntas y comentarios", {
    x: 0.5, y: 2.75, w: 9, h: 0.5,
    fontFace: BODY, fontSize: 18, color: MINT, italic: true, align: "center", margin: 0
  });

  // Stats finales en cajas
  const stats = [
    { num: "9",   lbl: "modulos" },
    { num: "8",   lbl: "estructuras" },
    { num: "16",  lbl: "commits" },
    { num: "100%", lbl: "requisitos" }
  ];
  stats.forEach((st, i) => {
    const x = 0.7 + i * 2.2;
    s.addShape("rect", {
      x, y: 3.6, w: 2.0, h: 1.1,
      fill: { color: DEEP }, line: { color: TEAL, width: 0.75 }
    });
    s.addText(st.num, {
      x, y: 3.6, w: 2.0, h: 0.65,
      fontFace: HEAD, fontSize: 28, bold: true, color: WHITE, align: "center", valign: "middle", margin: 0
    });
    s.addText(st.lbl, {
      x, y: 4.20, w: 2.0, h: 0.45,
      fontFace: BODY, fontSize: 11, color: MINT, align: "center", valign: "middle", margin: 0
    });
  });

  s.addText("github.com/tomassuaza/billetera-fintech", {
    x: 0.5, y: 5.05, w: 9, h: 0.3,
    fontFace: MONO, fontSize: 11, color: MINT, align: "center", margin: 0
  });

  // ---- Save ----
  await pres.writeFile({ fileName: "Billetera_Fintech.pptx" });
  console.log("OK -> Billetera_Fintech.pptx");
}

build().catch(e => { console.error(e); process.exit(1); });
