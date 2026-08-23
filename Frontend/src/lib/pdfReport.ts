import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export interface FinancialTransaction {
  id?: number;
  description?: string;
  amount?: number | string;
  category?: string;
  type?: string;
  date?: string;
}

export interface FinancialReportData {
  userName: string;
  userEmail: string;
  currencyCode: string;
  currencySymbol: string;
  transactions: FinancialTransaction[];
}

export function summarizeTransactions(transactions: FinancialTransaction[]) {
  return transactions.reduce(
    (summary, transaction) => {
      const amount = Math.abs(Number(transaction.amount) || 0);
      if (String(transaction.type || '').toUpperCase() === 'INGRESO') {
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

export function generateFinancialReport(data: FinancialReportData): void {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
  const transactions = Array.isArray(data.transactions) ? data.transactions : [];
  const summary = summarizeTransactions(transactions);
  const currency = data.currencyCode || 'HNL';
  const symbol = data.currencySymbol || 'L';
  const money = (value: number) =>
    `${symbol} ${value.toLocaleString('es-HN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${currency}`;

  doc.setFillColor(15, 23, 42);
  doc.rect(0, 0, 210, 30, 'F');
  doc.setTextColor(255, 255, 255);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(18);
  doc.text('FinanceAI', 14, 13);
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(9);
  doc.text('Reporte personal de movimientos financieros', 14, 20);

  doc.setTextColor(15, 23, 42);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(13);
  doc.text('Resumen de la cuenta', 14, 42);
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(9);
  doc.text(`Titular: ${data.userName || 'Usuario FinanceAI'}`, 14, 50);
  doc.text(`Correo: ${data.userEmail || 'No disponible'}`, 14, 56);
  doc.text(`Generado: ${new Date().toLocaleString('es-HN')}`, 14, 62);

  autoTable(doc, {
    startY: 70,
    theme: 'grid',
    head: [['Ingresos', 'Gastos', 'Balance']],
    body: [[money(summary.income), money(summary.expenses), money(summary.balance)]],
    headStyles: { fillColor: [30, 64, 175] },
    styles: { fontSize: 9 },
  });

  const summaryEnd = (doc as jsPDF & { lastAutoTable?: { finalY: number } }).lastAutoTable?.finalY ?? 88;
  autoTable(doc, {
    startY: summaryEnd + 10,
    theme: 'striped',
    head: [['Fecha', 'Descripción', 'Categoría', 'Tipo', 'Monto']],
    body: transactions.length
      ? transactions.map((transaction) => [
          transaction.date || '—',
          transaction.description || 'Sin descripción',
          transaction.category || 'General',
          transaction.type || '—',
          money(Math.abs(Number(transaction.amount) || 0)),
        ])
      : [['—', 'No hay movimientos registrados', '—', '—', money(0)]],
    headStyles: { fillColor: [30, 41, 59] },
    styles: { fontSize: 8, cellPadding: 2.5 },
    columnStyles: { 4: { halign: 'right' } },
    margin: { left: 14, right: 14, bottom: 22 },
  });

  const pages = doc.getNumberOfPages();
  for (let page = 1; page <= pages; page += 1) {
    doc.setPage(page);
    doc.setDrawColor(226, 232, 240);
    doc.line(14, 278, 196, 278);
    doc.setFontSize(7);
    doc.setTextColor(100, 116, 139);
    doc.text('Reporte informativo generado con los datos registrados por el usuario.', 14, 284);
    doc.text(`Página ${page} de ${pages}`, 174, 284);
  }

  const safeName = (data.userName || 'usuario').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-zA-Z0-9_-]/g, '_');
  doc.save(`FinanceAI_reporte_${safeName}.pdf`);
}
