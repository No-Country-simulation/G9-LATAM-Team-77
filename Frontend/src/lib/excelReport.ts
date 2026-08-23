import ExcelJS from 'exceljs';

export interface HistoryWorkbookRow {
  fechaHora?: string;
  perfilFinanciero?: string;
  probabilidad?: number;
  ingresoMensual?: number;
  totalGastos?: number;
  ahorroEstimado?: number;
  nivelEndeudamiento?: number;
  frecuenciaAhorro?: string;
  resumenGastos?: Record<string, unknown>;
  recomendaciones?: unknown[];
}

export interface HistoryWorkbookOptions {
  rows: HistoryWorkbookRow[];
  currencyCode: string;
  currencySymbol: string;
  filterSummary?: string;
  generatedAt?: Date;
}

const NAVY = '0F172A';
const SLATE = '1E293B';
const BLUE = '2563EB';
const EMERALD = '059669';
const AMBER = 'D97706';
const RED = 'DC2626';
const WHITE = 'FFFFFF';
const MUTED = '64748B';
const LIGHT_BLUE = 'EFF6FF';

function scoreOf(row: HistoryWorkbookRow): number {
  const probability = Number(row.probabilidad || 0);
  return Math.round(probability > 1 ? probability : probability * 100);
}

function profileColor(profile: string, score: number): string {
  const normalized = profile.toLowerCase();
  if (score < 45 || normalized.includes('riesgo') || normalized.includes('crít')) return RED;
  if (score < 75 || normalized.includes('moderado') || normalized.includes('observación')) return AMBER;
  return EMERALD;
}

function expenseSummary(value: Record<string, unknown> | undefined): string {
  if (!value || typeof value !== 'object') return 'Sin desglose disponible';
  const entries = Object.entries(value);
  if (!entries.length) return 'Sin desglose disponible';
  return entries.map(([category, amount]) => `${category}: ${Number(amount || 0).toLocaleString('es-HN')}`).join('\n');
}

function recommendationSummary(value: unknown[] | undefined): string {
  if (!Array.isArray(value) || !value.length) return 'Sin recomendaciones disponibles';
  return value.map((item, index) => `${index + 1}. ${String(item)}`).join('\n');
}

export function buildHistoryWorkbook(options: HistoryWorkbookOptions): ExcelJS.Workbook {
  const generatedAt = options.generatedAt || new Date();
  const currencyCode = options.currencyCode || 'HNL';
  const currencySymbol = options.currencySymbol || 'L';
  const currencyFormat = `"${currencySymbol.replace(/"/g, '')}" #,##0.00 "${currencyCode.replace(/"/g, '')}"`;
  const rows = options.rows || [];
  const scores = rows.map(scoreOf);
  const averageScore = scores.length ? Math.round(scores.reduce((total, score) => total + score, 0) / scores.length) : 0;
  const latest = [...rows].sort((a, b) => new Date(b.fechaHora || 0).getTime() - new Date(a.fechaHora || 0).getTime())[0];

  const workbook = new ExcelJS.Workbook();
  workbook.creator = 'FinanceAI Team 77';
  workbook.company = 'FinanceAI';
  workbook.created = generatedAt;
  workbook.modified = generatedAt;
  workbook.calcProperties.fullCalcOnLoad = true;

  const worksheet = workbook.addWorksheet('Historial', {
    properties: { defaultRowHeight: 20 },
    pageSetup: { orientation: 'landscape', fitToPage: true, fitToWidth: 1, fitToHeight: 0 },
    views: [{ state: 'frozen', ySplit: 8, showGridLines: false }],
  });

  worksheet.mergeCells('A1:J1');
  worksheet.getCell('A1').value = 'FinanceAI · Historial de Evaluaciones';
  worksheet.getCell('A1').style = {
    fill: { type: 'pattern', pattern: 'solid', fgColor: { argb: NAVY } },
    font: { name: 'Aptos Display', size: 20, bold: true, color: { argb: WHITE } },
    alignment: { vertical: 'middle', horizontal: 'left' },
  };
  worksheet.getRow(1).height = 38;

  worksheet.mergeCells('A2:J2');
  worksheet.getCell('A2').value = `Registro autenticado · Generado ${generatedAt.toLocaleString('es-HN')} · Moneda ${currencyCode}`;
  worksheet.getCell('A2').style = {
    fill: { type: 'pattern', pattern: 'solid', fgColor: { argb: SLATE } },
    font: { name: 'Aptos', size: 10, color: { argb: 'CBD5E1' } },
    alignment: { vertical: 'middle', horizontal: 'left' },
  };
  worksheet.getRow(2).height = 24;

  const cards = [
    { labelRange: 'A4:B4', valueRange: 'A5:B5', label: 'EVALUACIONES EXPORTADAS', value: { formula: rows.length ? 'ROWS(HistorialTable[Score])' : '0', result: rows.length }, color: BLUE },
    { labelRange: 'C4:D4', valueRange: 'C5:D5', label: 'SALUD FINANCIERA PROMEDIO', value: { formula: rows.length ? 'IFERROR(AVERAGE(HistorialTable[Score]),0)' : '0', result: averageScore }, color: EMERALD },
    { labelRange: 'E4:F4', valueRange: 'E5:F5', label: 'ÚLTIMO PERFIL REGISTRADO', value: latest?.perfilFinanciero || 'Sin registros', color: profileColor(latest?.perfilFinanciero || '', scoreOf(latest || {})) },
    { labelRange: 'G4:J4', valueRange: 'G5:J5', label: 'FILTROS APLICADOS', value: options.filterSummary || 'Todos los registros visibles', color: BLUE },
  ];

  cards.forEach(card => {
    worksheet.mergeCells(card.labelRange);
    worksheet.mergeCells(card.valueRange);
    const label = worksheet.getCell(card.labelRange.split(':')[0]);
    const value = worksheet.getCell(card.valueRange.split(':')[0]);
    label.value = card.label;
    value.value = card.value;
    label.style = {
      fill: { type: 'pattern', pattern: 'solid', fgColor: { argb: LIGHT_BLUE } },
      font: { name: 'Aptos', size: 9, bold: true, color: { argb: MUTED } },
      alignment: { vertical: 'middle', horizontal: 'center' },
      border: { top: { style: 'thin', color: { argb: 'DBEAFE' } }, left: { style: 'thin', color: { argb: 'DBEAFE' } }, right: { style: 'thin', color: { argb: 'DBEAFE' } } },
    };
    value.style = {
      fill: { type: 'pattern', pattern: 'solid', fgColor: { argb: LIGHT_BLUE } },
      font: { name: 'Aptos Display', size: 15, bold: true, color: { argb: card.color } },
      alignment: { vertical: 'middle', horizontal: 'center', wrapText: true },
      border: { bottom: { style: 'thin', color: { argb: 'DBEAFE' } }, left: { style: 'thin', color: { argb: 'DBEAFE' } }, right: { style: 'thin', color: { argb: 'DBEAFE' } } },
    };
  });
  worksheet.getRow(4).height = 22;
  worksheet.getRow(5).height = 32;

  worksheet.getCell('A7').value = 'Detalle de evaluaciones filtradas';
  worksheet.getCell('A7').font = { name: 'Aptos Display', size: 13, bold: true, color: { argb: NAVY } };

  worksheet.addTable({
    name: 'HistorialTable',
    ref: 'A8',
    headerRow: true,
    totalsRow: false,
    style: { theme: 'TableStyleMedium2', showRowStripes: true, showColumnStripes: false },
    columns: [
      { name: 'Fecha y hora' },
      { name: 'Perfil' },
      { name: 'Score' },
      { name: `Ingreso (${currencyCode})` },
      { name: `Gastos (${currencyCode})` },
      { name: `Ahorro (${currencyCode})` },
      { name: 'Endeudamiento' },
      { name: 'Frecuencia' },
      { name: 'Distribución de gastos' },
      { name: 'Recomendaciones de IA' },
    ],
    rows: rows.map(row => [
      row.fechaHora ? new Date(row.fechaHora) : null,
      row.perfilFinanciero || 'No disponible',
      scoreOf(row),
      Number(row.ingresoMensual || 0),
      Number(row.totalGastos || 0),
      Number(row.ahorroEstimado || 0),
      Number(row.nivelEndeudamiento || 0) / 100,
      row.frecuenciaAhorro || 'Sin frecuencia',
      expenseSummary(row.resumenGastos),
      recommendationSummary(row.recomendaciones),
    ]),
  });

  const firstDataRow = 9;
  const lastDataRow = Math.max(firstDataRow, firstDataRow + rows.length - 1);
  worksheet.getColumn(1).width = 21;
  worksheet.getColumn(2).width = 22;
  worksheet.getColumn(3).width = 10;
  worksheet.getColumn(4).width = 18;
  worksheet.getColumn(5).width = 18;
  worksheet.getColumn(6).width = 18;
  worksheet.getColumn(7).width = 18;
  worksheet.getColumn(8).width = 15;
  worksheet.getColumn(9).width = 32;
  worksheet.getColumn(10).width = 62;

  worksheet.getColumn(1).numFmt = 'dd/mm/yyyy hh:mm';
  worksheet.getColumn(3).numFmt = '0"/100"';
  worksheet.getColumn(4).numFmt = currencyFormat;
  worksheet.getColumn(5).numFmt = currencyFormat;
  worksheet.getColumn(6).numFmt = currencyFormat;
  worksheet.getColumn(7).numFmt = '0.0%';
  worksheet.getColumn(9).alignment = { vertical: 'top', wrapText: true };
  worksheet.getColumn(10).alignment = { vertical: 'top', wrapText: true };

  rows.forEach((row, index) => {
    const excelRow = worksheet.getRow(firstDataRow + index);
    excelRow.height = Math.max(30, Math.min(78, 18 + (Math.max(Object.keys(row.resumenGastos || {}).length, row.recomendaciones?.length || 0) * 10)));
    excelRow.alignment = { vertical: 'top' };
    const score = scoreOf(row);
    const color = profileColor(row.perfilFinanciero || '', score);
    excelRow.getCell(2).font = { bold: true, color: { argb: color } };
    excelRow.getCell(3).font = { bold: true, color: { argb: color } };
    excelRow.getCell(5).font = { color: { argb: RED } };
    excelRow.getCell(6).font = { color: { argb: EMERALD } };
  });

  const footerRow = lastDataRow + 3;
  worksheet.mergeCells(`A${footerRow}:J${footerRow}`);
  worksheet.getCell(`A${footerRow}`).value = 'FinanceAI · Orientación financiera, no asesoría profesional. Los datos corresponden únicamente al usuario autenticado.';
  worksheet.getCell(`A${footerRow}`).style = {
    font: { name: 'Aptos', size: 9, italic: true, color: { argb: MUTED } },
    alignment: { horizontal: 'center', vertical: 'middle' },
  };
  worksheet.getRow(footerRow).height = 24;
  worksheet.autoFilter = { from: 'A8', to: `J${lastDataRow}` };
  worksheet.headerFooter.oddFooter = '&LFinanceAI&C&P de &N&RHistorial financiero';

  return workbook;
}

export async function downloadHistoryWorkbook(options: HistoryWorkbookOptions): Promise<void> {
  const workbook = buildHistoryWorkbook(options);
  const buffer = await workbook.xlsx.writeBuffer();
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `FinanceAI_historial_${options.currencyCode || 'HNL'}_${new Date().toISOString().slice(0, 10)}.xlsx`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
