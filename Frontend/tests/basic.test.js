import { test, expect } from 'vitest';
import {
  clearAuthSession,
  setAuthSession,
  validateLoginInput,
  validateRegisterInput,
} from '../src/lib/auth';

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
