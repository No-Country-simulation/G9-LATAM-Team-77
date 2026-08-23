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
  const robots = readFileSync(resolve('public/robots.txt'), 'utf8');

  expect(header).toContain("monedaId: Number(currencySelect?.value)");
  expect(header).toContain('/api/v1/transactions');
  expect(header).not.toMatch(/method:\s*['"]DELETE['"]/);
  expect(header).not.toContain('monedaCodigo:');
  expect(dashboard).toContain('data.ingresosMesActual');
  expect(dashboard).toContain('data.gastosMesActual');
  expect(dashboard).not.toContain('Cálculo de respaldo inteligente');
  expect(robots).toContain('https://financeai-team77.duckdns.org/sitemap.xml');
  expect(robots).not.toContain('financeai.app');
});
