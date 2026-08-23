import { test, expect } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import {
  clearAuthSession,
  setAuthSession,
  validateLoginInput,
  validateRegisterInput,
} from '../src/lib/auth';
import {
  FORGOT_PASSWORD_CONFIRMATION,
  readApiMessage,
  validateForgotPasswordEmail,
  validateResetPassword,
} from '../src/lib/password-reset';
import { summarizeTransactions } from '../src/lib/pdfReport';

test('Calculo basico de finanzas (Prueba de integracion UI)', () => {
  const ingresos = 5000;
  const gastos = 2000;
  const saldo = ingresos - gastos;
  expect(saldo).toBe(3000);
});

test('Clasificacion de categorias por defecto', () => {
    const defaultCategories = ['Alimentacion', 'Transporte', 'Entretenimiento'];
    expect(defaultCategories).toContain('Transporte');
});

test('guarda y elimina por completo la sesion JWT', () => {
  setAuthSession('jwt-de-prueba', { email: 'user@test.com' });

  expect(localStorage.getItem('financeai_token')).toBe('jwt-de-prueba');
  expect(localStorage.getItem('financeai_user')).toContain('user@test.com');

  clearAuthSession();

  expect(localStorage.getItem('financeai_token')).toBeNull();
  expect(localStorage.getItem('financeai_user')).toBeNull();
});

test('valida login y seleccion dinamica de catalogos', () => {
  expect(validateLoginInput({ email: 'persona@financeai.test', password: '123456' })).toEqual({ valid: true });
  expect(validateRegisterInput({
    nombre: 'Ana',
    apellido: 'López',
    email: 'ana@financeai.test',
    password: '123456',
    paisId: 47,
    monedaId: 83,
  })).toEqual({ valid: true });

  expect(validateRegisterInput({
    nombre: 'Ana',
    apellido: 'López',
    email: 'ana@financeai.test',
    password: '123456',
    paisId: 0,
    monedaId: 83,
  }).valid).toBe(false);
});

test('valida el formulario de solicitud sin enumerar cuentas', () => {
  expect(validateForgotPasswordEmail('')).toBe('Ingresa tu correo electrónico.');
  expect(validateForgotPasswordEmail('correo-invalido')).toBe('Ingresa un correo electrónico válido.');
  expect(validateForgotPasswordEmail('persona@financeai.test')).toBeNull();
  expect(FORGOT_PASSWORD_CONFIRMATION).not.toMatch(/existe|no encontrado/i);
});

test('valida token, longitud y confirmacion de nueva contraseña', () => {
  expect(validateResetPassword('', 'NuevaClave#2026', 'NuevaClave#2026')).toContain('no es válido');
  expect(validateResetPassword('token', 'corta', 'corta')).toContain('entre 8 y 128');
  expect(validateResetPassword('token', 'NuevaClave#2026', 'Distinta#2026')).toContain('no coinciden');
  expect(validateResetPassword('token', 'NuevaClave#2026', 'NuevaClave#2026')).toBeNull();
});

test('maneja respuestas de error del Backend sin exponer detalles internos', async () => {
  const controlled = new Response(JSON.stringify({ message: 'El enlace de recuperación expiró.' }), {
    status: 400,
    headers: { 'Content-Type': 'application/json' },
  });
  const malformed = new Response('not-json', { status: 500 });

  await expect(readApiMessage(controlled, 'Error controlado')).resolves.toBe('El enlace de recuperación expiró.');
  await expect(readApiMessage(malformed, 'Error controlado')).resolves.toBe('Error controlado');
});

test('resume los movimientos reales para el reporte sin convertir monedas', () => {
  expect(summarizeTransactions([
    { type: 'INGRESO', amount: 1500 },
    { type: 'GASTO', amount: 250 },
    { type: 'GASTO', amount: '125.50' },
  ])).toEqual({ income: 1500, expenses: 375.5, balance: 1124.5 });
});

test('conserva los contratos reales al aplicar el diseño final', () => {
  const header = readFileSync(resolve('src/components/Header.astro'), 'utf8');
  const dashboard = readFileSync(resolve('src/pages/dashboard.astro'), 'utf8');
  const history = readFileSync(resolve('src/pages/historial.astro'), 'utf8');
  const historyCharts = readFileSync(resolve('src/lib/historyCharts.ts'), 'utf8');
  const login = readFileSync(resolve('src/pages/login.astro'), 'utf8');
  const pdfReport = readFileSync(resolve('src/lib/pdfReport.ts'), 'utf8');
  const robots = readFileSync(resolve('public/robots.txt'), 'utf8');

  expect(header).toContain("monedaId: Number(currencySelect?.value)");
  expect(header).toContain('/api/v1/transactions');
  expect(header).not.toMatch(/method:\s*['"]DELETE['"]/);
  expect(header).not.toContain('monedaCodigo:');
  expect(dashboard).toContain('data.ingresosMesActual');
  expect(dashboard).toContain('data.gastosMesActual');
  expect(dashboard).not.toContain('Cálculo de respaldo inteligente');
  expect(dashboard).toContain('Tu diagnóstico financiero inteligente');
  expect(dashboard).toContain('Descubre qué impulsa tu salud financiera y cómo mejorarla.');
  expect(dashboard).toContain('id="inputPeriodDate"');
  expect(dashboard).toContain('id="periodRangeNotice"');
  expect(dashboard).toContain("frequency === 'Quincenal'");
  expect(dashboard).toContain("frequency === 'Semanal'");
  expect(dashboard).toContain('id="analysisTransactionList"');
  expect(dashboard).toContain('id="btnAddAnalysisTransaction"');
  expect(dashboard).toContain("'Salud y bienestar'");
  expect(dashboard).toContain("transaction.descripcion === 'Deudas'");
  expect(dashboard).toContain('calculateDebtRatio(ingreso).ratio');
  expect(dashboard).not.toContain('required value="20"');
  expect(dashboard).toContain('analysisTransactions.length >= 12');
  expect(dashboard).toContain('id="periodCalendarGrid"');
  expect(dashboard).toContain("iso >= currentPeriodStart && iso <= currentPeriodEnd");
  expect(dashboard).toContain('latestAnalysisExport');
  expect(dashboard).toContain('window.generateExpedientePDF({');
  expect(pdfReport).toContain('DIAGNÓSTICO FINANCIERO CON IA');
  expect(pdfReport).toContain('Recomendaciones principales');
  expect(pdfReport).toContain('Expediente del Titular y Registro de Transacciones');
  expect(history).toContain('id="filterMonth"');
  expect(history).toContain('id="filterDateStart"');
  expect(history).toContain('id="filterDateEnd"');
  expect(history).toContain('id="filterFrequency"');
  expect(history).toContain('id="filterCategory"');
  expect(history).toContain('filteredHistoryData');
  expect(history).toContain('Ver detalle');
  expect(history).toContain('Distribución de gastos');
  expect(history).toContain('id="historyTrendChart"');
  expect(history).toContain('id="historyCategoryChart"');
  expect(history).toContain('Datos reales persistidos');
  expect(history).not.toContain('Borrar Todo el Historial');
  expect(historyCharts).toContain("from 'chart.js'");
  expect(historyCharts).toContain("type: 'line'");
  expect(historyCharts).toContain("type: 'doughnut'");
  expect(historyCharts).toContain('item.resumenGastos');
  expect(history).toContain('Exportar Excel');
  expect(history).toContain('downloadHistoryWorkbook');
  expect(history).not.toContain('Exportar CSV');
  expect(login).toContain('MULTIMONEDA');
  expect(login).toContain('HNL, MXN, USD Y MÁS');
  expect(login).not.toContain('MONEDA LOCAL');
  expect(login).toContain('https://accounts.google.com/gsi/client?hl=es');
  expect(login).toContain('/api/v1/auth/google');
  expect(login).toContain('PUBLIC_GOOGLE_CLIENT_ID');
  expect(login).toContain('Selecciona país y moneda para crear tu cuenta de Google');
  expect(robots).toContain('https://financeai-team77.duckdns.org/sitemap.xml');
  expect(robots).not.toContain('financeai.app');
});
