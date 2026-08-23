import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export interface FinancialTransaction {
  id?: number | string;
  description?: string;
  amount?: number | string;
  category?: string;
  type?: string;
  date?: string;
  createdAt?: string;
}

export interface FinancialReportData {
  userName?: string;
  userEmail?: string;
  currency?: string;
  currencyCode?: string;
  currencySymbol?: string;
  transactions?: FinancialTransaction[];
  analysis?: {
    score?: number | string;
    profile?: string;
    summary?: string;
    period?: string;
    debtRatio?: number;
    recommendations?: string[];
  };
}

export const CURRENCY_SYMBOLS: Record<string, string> = {
  USD: '$',
  MXN: '$',
  EUR: '€',
  CRC: '₡',
  COP: '$',
  ARS: '$',
  CLP: '$',
  PEN: 'S/',
  HNL: 'L',
  BRL: 'R$',
  GTQ: 'Q',
  NIO: 'C$',
  DOP: 'RD$',
  BOB: 'Bs',
  UYU: '$U',
  PYG: '₲'
};

export function summarizeTransactions(transactions: FinancialTransaction[]) {
  return (Array.isArray(transactions) ? transactions : []).reduce(
    (summary, transaction) => {
      const rawAmt = typeof transaction.amount === 'string' ? parseFloat(transaction.amount) : Number(transaction.amount);
      const amount = isNaN(rawAmt) ? 0 : Math.abs(rawAmt);
      const typeStr = String(transaction.type || '').toUpperCase();
      if (typeStr === 'INGRESO' || typeStr === 'INCOME') {
        summary.income += amount;
      } else {
        summary.expenses += amount;
      }
      summary.balance = summary.income - summary.expenses;
      return summary;
    },
    { income: 0, expenses: 0, balance: 0 },
  );
}

export function generateExpedientePDF(data: FinancialReportData): void {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const margin = 14;

  const validTransactions = Array.isArray(data.transactions) ? data.transactions : [];
  const summary = summarizeTransactions(validTransactions);

  const currencyCode = (data.currencyCode || data.currency || 'HNL').toUpperCase();
  const currencySymbol = data.currencySymbol || CURRENCY_SYMBOLS[currencyCode] || '$';

  const formatMoney = (val: number) => {
    return `${currencySymbol} ${val.toLocaleString('es-HN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })} ${currencyCode}`;
  };

  const fechaEmision = new Date().toLocaleDateString('es-HN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  const folioUnico = `FAI-REP-${Date.now().toString().slice(-6)}`;

  // ─── 1. HEADER / ENCABEZADO CORPORATIVO ───
  doc.setFillColor(15, 23, 42); // slate-900
  doc.rect(0, 0, pageWidth, 28, 'F');

  // Logo / Título
  doc.setTextColor(255, 255, 255);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(16);
  doc.text('FinanceAI', margin, 12);

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(9);
  doc.setTextColor(148, 163, 184); // slate-400
  doc.text('Plataforma de Inteligencia Financiera & Control Patrimonial', margin, 18);

  // ─── 2. TÍTULO DEL EXPEDIENTE ───
  let currentY = 36;
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(13);
  doc.text(
    data.analysis
      ? 'Expediente de Análisis Financiero con IA'
      : 'Expediente del Titular y Registro de Transacciones',
    margin,
    currentY,
  );

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(8.5);
  doc.setTextColor(100, 116, 139);
  doc.text(`Emisión: ${fechaEmision}   |   Folio: ${folioUnico}`, margin, currentY + 5);

  currentY += 12;

  // ─── 3. FICHA DE DATOS DEL TITULAR ───
  doc.setFillColor(248, 250, 252); // slate-50
  doc.setDrawColor(226, 232, 240); // slate-200
  doc.roundedRect(margin, currentY, pageWidth - (margin * 2), 22, 2, 2, 'FD');

  doc.setFontSize(8.5);
  // Columna 1: Titular
  doc.setTextColor(100, 116, 139);
  doc.text('Nombre del Titular:', margin + 4, currentY + 7);
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.text(data.userName || 'Usuario FinanceAI', margin + 4, currentY + 14);

  // Columna 2: Email
  const col2X = margin + 65;
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Correo Electrónico:', col2X, currentY + 7);
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.text(data.userEmail || 'No disponible', col2X, currentY + 14);

  // Columna 3: Divisa
  const col3X = margin + 130;
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Divisa Principal:', col3X, currentY + 7);
  doc.setTextColor(16, 185, 129);
  doc.setFont('helvetica', 'bold');
  doc.text(`${currencyCode} (${currencySymbol})`, col3X, currentY + 14);

  currentY += 28;

  // ─── 4. TARJETAS DE RESUMEN FINANCIERO ───
  const cardWidth = (pageWidth - (margin * 2) - 8) / 3;

  // Tarjeta 1: Ingresos
  doc.setFillColor(240, 253, 244); // emerald-50
  doc.setDrawColor(187, 247, 208); // emerald-200
  doc.roundedRect(margin, currentY, cardWidth, 18, 2, 2, 'FD');
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(7.5);
  doc.setTextColor(22, 101, 52);
  doc.text('TOTAL INGRESOS', margin + 3, currentY + 6);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(10.5);
  doc.text(formatMoney(summary.income), margin + 3, currentY + 14);

  // Tarjeta 2: Gastos
  const card2X = margin + cardWidth + 4;
  doc.setFillColor(254, 242, 242); // rose-50
  doc.setDrawColor(254, 202, 202); // rose-200
  doc.roundedRect(card2X, currentY, cardWidth, 18, 2, 2, 'FD');
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(7.5);
  doc.setTextColor(153, 27, 27);
  doc.text('TOTAL GASTOS', card2X + 3, currentY + 6);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(10.5);
  doc.text(formatMoney(summary.expenses), card2X + 3, currentY + 14);

  // Tarjeta 3: Balance
  const card3X = card2X + cardWidth + 4;
  const isPositive = summary.balance >= 0;
  if (isPositive) {
    doc.setFillColor(240, 249, 255); // sky-50
    doc.setDrawColor(186, 230, 253);
    doc.setTextColor(3, 105, 161);
  } else {
    doc.setFillColor(255, 241, 242); // rose-50
    doc.setDrawColor(254, 205, 211);
    doc.setTextColor(190, 18, 60);
  }
  doc.roundedRect(card3X, currentY, cardWidth, 18, 2, 2, 'FD');
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(7.5);
  doc.text('BALANCE NETO', card3X + 3, currentY + 6);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(10.5);
  doc.text(formatMoney(summary.balance), card3X + 3, currentY + 14);

  currentY += 24;

  // ─── 5. RESULTADO DEL ANÁLISIS DE IA (cuando el reporte nace del Dashboard) ───
  if (data.analysis) {
    const analysis = data.analysis;
    const recommendations = Array.isArray(analysis.recommendations)
      ? analysis.recommendations.filter(Boolean).slice(0, 3)
      : [];
    const summaryLines = doc.splitTextToSize(
      analysis.summary || 'Diagnóstico financiero generado por FinanceAI.',
      pageWidth - (margin * 2) - 45,
    );
    const recommendationLines = recommendations.flatMap((item, index) =>
      doc.splitTextToSize(`${index + 1}. ${item}`, pageWidth - (margin * 2) - 8).slice(0, 2),
    );
    const analysisHeight = 25 + (recommendationLines.length * 4);

    doc.setFillColor(239, 246, 255);
    doc.setDrawColor(191, 219, 254);
    doc.roundedRect(margin, currentY, pageWidth - (margin * 2), analysisHeight, 2, 2, 'FD');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9.5);
    doc.setTextColor(30, 64, 175);
    doc.text('DIAGNÓSTICO FINANCIERO CON IA', margin + 4, currentY + 6);

    doc.setFontSize(15);
    doc.setTextColor(15, 23, 42);
    doc.text(`${analysis.score ?? '--'}/100`, margin + 4, currentY + 14);
    doc.setFontSize(9.5);
    doc.text(analysis.profile || 'Análisis completado', margin + 25, currentY + 12);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(7.5);
    doc.setTextColor(71, 85, 105);
    const analysisMeta = [
      analysis.period ? `Período: ${analysis.period}` : '',
      Number.isFinite(Number(analysis.debtRatio)) ? `Endeudamiento: ${Number(analysis.debtRatio).toFixed(1)}%` : '',
    ].filter(Boolean).join('  |  ');
    if (analysisMeta) doc.text(doc.splitTextToSize(analysisMeta, pageWidth - (margin * 2) - 25)[0], margin + 25, currentY + 17);
    doc.text(summaryLines.slice(0, 2), margin + 45, currentY + 10);

    if (recommendationLines.length) {
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(30, 64, 175);
      doc.text('Recomendaciones principales', margin + 4, currentY + 22);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(51, 65, 85);
      recommendationLines.forEach((line, index) => {
        doc.text(line, margin + 4, currentY + 27 + (index * 4));
      });
    }

    currentY += analysisHeight + 6;
  }

  // ─── 6. TABLA DE TRANSACCIONES DETALLADA ───
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(10.5);
  doc.text(`Detalle de Movimientos Registrados (${validTransactions.length})`, margin, currentY);

  currentY += 3;

  const tableRows = validTransactions.map((t, idx) => {
    const rawAmt = typeof t.amount === 'string' ? parseFloat(t.amount) : Number(t.amount || 0);
    const amt = isNaN(rawAmt) ? 0 : Math.abs(rawAmt);
    const isIncome = String(t.type || '').toUpperCase() === 'INGRESO' || String(t.type || '').toUpperCase() === 'INCOME';
    const typeLabel = isIncome ? 'Ingreso (+)' : 'Gasto (-)';

    let dateStr = 'Reciente';
    if (t.date) {
      dateStr = (t.date.includes('T') ? t.date.split('T')[0] : t.date).slice(0, 10);
    } else if (t.createdAt) {
      dateStr = (t.createdAt.includes('T') ? t.createdAt.split('T')[0] : t.createdAt).slice(0, 10);
    }

    return [
      (idx + 1).toString(),
      dateStr,
      t.description || 'Sin descripción',
      t.category || 'General',
      typeLabel,
      formatMoney(amt)
    ];
  });

  if (tableRows.length === 0) {
    tableRows.push(['-', '-', 'No hay transacciones registradas actualmente', '-', '-', formatMoney(0)]);
  }

  const tableOptions = {
    startY: currentY,
    head: [['#', 'Fecha', 'Concepto / Descripción', 'Categoría', 'Tipo', 'Monto']],
    body: tableRows,
    theme: 'striped',
    headStyles: {
      fillColor: [30, 41, 59], // slate-800
      textColor: [255, 255, 255],
      fontStyle: 'bold',
      fontSize: 8,
      halign: 'left'
    },
    bodyStyles: {
      fontSize: 8,
      textColor: [51, 65, 85]
    },
    columnStyles: {
      0: { cellWidth: 8, halign: 'center' },
      1: { cellWidth: 24 },
      2: { cellWidth: 'auto' },
      3: { cellWidth: 32 },
      4: { cellWidth: 24, fontStyle: 'bold' },
      5: { cellWidth: 28, halign: 'right', fontStyle: 'bold' }
    },
    didParseCell: (hookData: any) => {
      if (hookData.section === 'body' && hookData.column.index === 4) {
        const val = hookData.cell.raw as string;
        if (val && val.includes('Ingreso')) {
          hookData.cell.styles.textColor = [16, 185, 129];
        } else if (val && val.includes('Gasto')) {
          hookData.cell.styles.textColor = [239, 68, 68];
        }
      }
    },
    margin: { left: margin, right: margin, bottom: 26 }
  };

  if (typeof (doc as any).autoTable === 'function') {
    (doc as any).autoTable(tableOptions);
  } else if (typeof autoTable === 'function') {
    autoTable(doc, tableOptions);
  } else if (typeof window !== 'undefined' && typeof (window as any).jspdf?.autoTable === 'function') {
    (window as any).jspdf.autoTable(doc, tableOptions);
  }

  // ─── 7. PIE DE PÁGINA Y AVISO LEGAL ───
  const pageCount = (doc.internal as any).getNumberOfPages ? (doc.internal as any).getNumberOfPages() : doc.getNumberOfPages();

  for (let i = 1; i <= pageCount; i++) {
    doc.setPage(i);

    doc.setDrawColor(226, 232, 240);
    doc.line(margin, pageHeight - 20, pageWidth - margin, pageHeight - 20);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(6.5);
    doc.setTextColor(148, 163, 184);
    doc.text(
      'Documento informativo generado con los movimientos registrados en FinanceAI.',
      margin,
      pageHeight - 15
    );
    doc.text(
      'Las cifras se expresan en la moneda principal configurada por el usuario.',
      margin,
      pageHeight - 11
    );

    doc.setFontSize(7.5);
    doc.setTextColor(100, 116, 139);
    doc.text(`Página ${i} de ${pageCount}`, pageWidth - margin - 20, pageHeight - 13);
  }

  const sanitizedName = (data.userName || 'usuario').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-zA-Z0-9_-]/g, '_');
  doc.save(`FinanceAI_reporte_${sanitizedName}.pdf`);
}

// Alias para retrocompatibilidad
export const generateFinancialReport = generateExpedientePDF;

if (typeof window !== 'undefined') {
  (window as any).generateExpedientePDF = generateExpedientePDF;
  (window as any).generateFinancialReport = generateFinancialReport;
}
