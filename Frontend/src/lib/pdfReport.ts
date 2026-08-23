import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export interface UserExpedienteData {
  userName: string;
  userEmail: string;
  currency: string;
  transactions: Array<{
    id?: number | string;
    description?: string;
    category?: string;
    amount?: number | string;
    type?: string;
    date?: string;
    createdAt?: string;
  }>;
}

export function generateExpedientePDF(data: UserExpedienteData) {
  const jsPDFConstructor = (typeof window !== 'undefined' && (window as any).jspdf?.jsPDF) || jsPDF;
  const doc = new jsPDFConstructor({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4'
  });

  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const margin = 14;

  // Formateador de moneda
  const currencySymbol = data.currency === 'EUR' ? '€' : '$';
  const formatMoney = (val: number) => {
    return `${currencySymbol}${val.toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${data.currency}`;
  };

  // Cálculos financieros
  let totalIngresos = 0;
  let totalGastos = 0;

  const validTransactions = Array.isArray(data.transactions) ? data.transactions : [];

  validTransactions.forEach(t => {
    const rawAmt = typeof t.amount === 'string' ? parseFloat(t.amount) : (t.amount || 0);
    const amt = isNaN(rawAmt) ? 0 : Math.abs(rawAmt);
    const typeStr = (t.type || '').toUpperCase();

    if (typeStr === 'INGRESO' || typeStr === 'INCOME') {
      totalIngresos += amt;
    } else {
      totalGastos += amt;
    }
  });

  const balanceNeto = totalIngresos - totalGastos;
  const fechaEmision = new Date().toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  const folioUnico = `FAI-ARCO-${Date.now().toString().slice(-6)}`;

  // ─── 1. HEADER / ENCABEZADO CORPORATIVO ───
  // Fondo de cabecera
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

  // Badge derecho: Certificado ARCO
  doc.setFillColor(16, 185, 129); // emerald-500
  doc.roundedRect(pageWidth - margin - 58, 7, 58, 14, 2, 2, 'F');
  doc.setTextColor(255, 255, 255);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(7.5);
  doc.text('CERTIFICADO OFICIAL ARCO', pageWidth - margin - 55, 12);
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(6.5);
  doc.text('Derecho de Acceso a Datos', pageWidth - margin - 55, 17);

  // ─── 2. TÍTULO DEL EXPEDIENTE ───
  let currentY = 36;
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(13);
  doc.text('Expediente del Titular y Registro de Transacciones', margin, currentY);

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
  // Columna 1
  doc.setTextColor(100, 116, 139);
  doc.text('Nombre del Titular:', margin + 4, currentY + 7);
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.text(data.userName || 'Usuario FinanceAI', margin + 4, currentY + 14);

  // Columna 2
  const col2X = margin + 65;
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Correo Electrónico:', col2X, currentY + 7);
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.text(data.userEmail || 'Sin registrar', col2X, currentY + 14);

  // Columna 3
  const col3X = margin + 130;
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Divisa Principal:', col3X, currentY + 7);
  doc.setTextColor(16, 185, 129);
  doc.setFont('helvetica', 'bold');
  doc.text(`${data.currency} (${currencySymbol})`, col3X, currentY + 14);

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
  doc.text(formatMoney(totalIngresos), margin + 3, currentY + 14);

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
  doc.text(formatMoney(totalGastos), card2X + 3, currentY + 14);

  // Tarjeta 3: Balance
  const card3X = card2X + cardWidth + 4;
  const isPositive = balanceNeto >= 0;
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
  doc.text(formatMoney(balanceNeto), card3X + 3, currentY + 14);

  currentY += 24;

  // ─── 5. TABLA DE TRANSACCIONES DETALLADA ───
  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(10.5);
  doc.text(`Detalle de Movimientos Registrados (${validTransactions.length})`, margin, currentY);

  currentY += 3;

  const tableRows = validTransactions.map((t, idx) => {
    const rawAmt = typeof t.amount === 'string' ? parseFloat(t.amount) : (t.amount || 0);
    const amt = isNaN(rawAmt) ? 0 : Math.abs(rawAmt);
    const isIncome = (t.type || '').toUpperCase() === 'INGRESO' || (t.type || '').toUpperCase() === 'INCOME';
    const typeLabel = isIncome ? 'Ingreso (+)' : 'Gasto (-)';

    let dateStr = 'Reciente';
    if (t.date) {
      dateStr = new Date(t.date).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
    } else if (t.createdAt) {
      dateStr = new Date(t.createdAt).toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' });
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

  // ─── 6. CERTIFICADO LEGAL Y PIE DE PÁGINA ───
  const pageCount = (doc.internal as any).getNumberOfPages();

  for (let i = 1; i <= pageCount; i++) {
    doc.setPage(i);

    // Separador inferior
    doc.setDrawColor(226, 232, 240);
    doc.line(margin, pageHeight - 20, pageWidth - margin, pageHeight - 20);

    // Texto legal
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(6.5);
    doc.setTextColor(148, 163, 184);
    doc.text(
      'Documento expedido en cumplimiento de los Arts. 22-35 de la Ley de Protección de Datos (LFPDPPP) y Arts. 15-20 GDPR.',
      margin,
      pageHeight - 15
    );
    doc.text(
      'FinanceAI garantiza que esta información es privada y no ha sido compartida ni comercializada con terceros.',
      margin,
      pageHeight - 11
    );

    // Número de página
    doc.setFontSize(7.5);
    doc.setTextColor(100, 116, 139);
    doc.text(`Página ${i} de ${pageCount}`, pageWidth - margin - 20, pageHeight - 13);
  }

  // Descarga directa en el navegador
  const sanitizedName = (data.userName || 'Usuario').replace(/[^a-zA-Z0-9]/g, '_');
  doc.save(`Expediente_FinanceAI_${sanitizedName}_${Date.now()}.pdf`);
}

if (typeof window !== 'undefined') {
  (window as any).generateExpedientePDF = generateExpedientePDF;
}
