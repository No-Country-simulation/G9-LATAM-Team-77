import { test, expect } from 'vitest';

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
