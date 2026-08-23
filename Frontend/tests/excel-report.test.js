import ExcelJS from 'exceljs';
import { describe, expect, test } from 'vitest';
import { buildHistoryWorkbook } from '../src/lib/excelReport';

describe('reporte Excel del historial', () => {
  test('genera un XLSX real con métricas, formatos y datos filtrados', async () => {
    const workbook = buildHistoryWorkbook({
      currencyCode: 'HNL',
      currencySymbol: 'L',
      filterSummary: 'Mes: agosto de 2026 · Orden: Más recientes',
      generatedAt: new Date('2026-08-23T18:00:00-06:00'),
      rows: [{
        fechaHora: '2026-08-23T16:47:29-06:00',
        perfilFinanciero: 'En riesgo',
        probabilidad: 0.52,
        ingresoMensual: 25000,
        totalGastos: 26800,
        ahorroEstimado: 0,
        nivelEndeudamiento: 10,
        frecuenciaAhorro: 'Mensual',
        resumenGastos: { Vivienda: 25000, Deudas: 2500 },
        recomendaciones: ['Reduce las obligaciones de deuda del período.'],
      }],
    });

    const sheet = workbook.getWorksheet('Historial');
    expect(sheet).toBeDefined();
    expect(sheet.getCell('A1').value).toBe('FinanceAI · Historial de Evaluaciones');
    expect(sheet.getCell('A9').value).toBeInstanceOf(Date);
    expect(sheet.getCell('B9').value).toBe('En riesgo');
    expect(sheet.getCell('C9').value).toBe(52);
    expect(sheet.getCell('G9').value).toBe(0.1);
    expect(sheet.getCell('I9').value).toContain('Deudas');
    expect(sheet.getCell('J9').value).toContain('Reduce las obligaciones');

    const buffer = await workbook.xlsx.writeBuffer();
    expect(buffer.byteLength).toBeGreaterThan(5000);

    const reopened = new ExcelJS.Workbook();
    await reopened.xlsx.load(buffer);
    const reopenedSheet = reopened.getWorksheet('Historial');
    expect(reopenedSheet.getCell('D9').value).toBe(25000);
    expect(reopenedSheet.getCell('G9').numFmt).toBe('0.0%');
    expect(reopenedSheet.getColumn(10).width).toBe(62);
  });
});
