import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

interface HistoryChartItem {
  fechaHora?: string;
  perfilFinanciero?: string;
  probabilidad?: number;
  ingresoMensual?: number;
  totalGastos?: number;
  ahorroEstimado?: number;
  nivelEndeudamiento?: number;
  resumenGastos?: Record<string, unknown>;
}

interface HistoryChartOptions {
  items: HistoryChartItem[];
  currencyCode?: string;
  currencySymbol?: string;
}

let trendChart: Chart | null = null;
let categoryChart: Chart | null = null;

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#06B6D4', '#84CC16', '#F97316', '#64748B'];

function scoreOf(item: HistoryChartItem): number {
  const probability = Number(item.probabilidad || 0);
  return Math.round(probability > 1 ? probability : probability * 100);
}

function percentage(value: number, income: number): number {
  return income > 0 ? Number(((value / income) * 100).toFixed(1)) : 0;
}

function setText(id: string, value: string): void {
  const element = document.getElementById(id);
  if (element) element.textContent = value;
}

function setTone(id: string, tone: 'positive' | 'warning' | 'neutral'): void {
  const element = document.getElementById(id);
  if (!element) return;
  element.className = tone === 'positive'
    ? 'mt-1 text-sm font-black text-emerald-600 dark:text-emerald-400'
    : tone === 'warning'
      ? 'mt-1 text-sm font-black text-amber-600 dark:text-amber-400'
      : 'mt-1 text-sm font-black text-blue-600 dark:text-blue-400';
}

function chartTheme() {
  const dark = document.documentElement.classList.contains('dark');
  return {
    text: dark ? '#CBD5E1' : '#475569',
    muted: dark ? '#94A3B8' : '#64748B',
    grid: dark ? 'rgba(71, 85, 105, 0.28)' : 'rgba(148, 163, 184, 0.24)',
    tooltip: dark ? '#0F172A' : '#FFFFFF',
    tooltipText: dark ? '#F8FAFC' : '#0F172A',
  };
}

export function renderHistoryCharts(options: HistoryChartOptions): void {
  const items = Array.isArray(options.items) ? options.items : [];
  const trendCanvas = document.getElementById('historyTrendChart') as HTMLCanvasElement | null;
  const categoryCanvas = document.getElementById('historyCategoryChart') as HTMLCanvasElement | null;
  if (!trendCanvas || !categoryCanvas) return;

  trendChart?.destroy();
  categoryChart?.destroy();
  trendChart = null;
  categoryChart = null;

  const trendEmpty = document.getElementById('historyTrendEmpty');
  const categoryEmpty = document.getElementById('historyCategoryEmpty');
  const hasHistory = items.length > 0;
  trendCanvas.classList.toggle('hidden', !hasHistory);
  trendEmpty?.classList.toggle('hidden', hasHistory);

  if (!hasHistory) {
    categoryCanvas.classList.add('hidden');
    categoryEmpty?.classList.remove('hidden');
    setText('historyTrendInsight', 'Sin datos para comparar');
    setText('historyCategoryInsight', 'Sin categorías disponibles');
    setText('historyDebtInsight', 'Sin análisis visibles');
    return;
  }

  const theme = chartTheme();
  const chronological = [...items].sort((a, b) => new Date(a.fechaHora || 0).getTime() - new Date(b.fechaHora || 0).getTime());
  const labels = chronological.map(item => new Intl.DateTimeFormat('es-HN', { day: '2-digit', month: 'short' }).format(new Date(item.fechaHora || 0)));
  const scores = chronological.map(scoreOf);
  const expenseRatios = chronological.map(item => percentage(Number(item.totalGastos || 0), Number(item.ingresoMensual || 0)));
  const savingRatios = chronological.map(item => percentage(Number(item.ahorroEstimado || 0), Number(item.ingresoMensual || 0)));

  trendChart = new Chart(trendCanvas, {
    type: 'line',
    data: {
      labels,
      datasets: [
        { label: 'Score financiero', data: scores, borderColor: '#3B82F6', backgroundColor: 'rgba(59,130,246,.14)', pointBackgroundColor: '#3B82F6', pointRadius: 3, pointHoverRadius: 6, borderWidth: 3, tension: 0.32, fill: true },
        { label: 'Gasto / ingreso', data: expenseRatios, borderColor: '#EF4444', backgroundColor: 'transparent', pointBackgroundColor: '#EF4444', pointRadius: 2.5, pointHoverRadius: 5, borderWidth: 2, tension: 0.32, borderDash: [7, 5] },
        { label: 'Tasa de ahorro', data: savingRatios, borderColor: '#10B981', backgroundColor: 'transparent', pointBackgroundColor: '#10B981', pointRadius: 2.5, pointHoverRadius: 5, borderWidth: 2, tension: 0.32 },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      animation: { duration: 650 },
      plugins: {
        legend: { position: 'bottom', labels: { color: theme.text, usePointStyle: true, boxWidth: 8, padding: 18, font: { size: 10, weight: 600 } } },
        tooltip: {
          backgroundColor: theme.tooltip,
          titleColor: theme.tooltipText,
          bodyColor: theme.tooltipText,
          borderColor: theme.grid,
          borderWidth: 1,
          padding: 12,
          callbacks: { label: context => `${context.dataset.label}: ${Number(context.parsed.y || 0).toFixed(1)}${context.datasetIndex === 0 ? '/100' : '%'}` },
        },
      },
      scales: {
        x: { ticks: { color: theme.muted, maxRotation: 0, autoSkip: true, maxTicksLimit: 9, font: { size: 10 } }, grid: { display: false } },
        y: { beginAtZero: true, suggestedMax: 100, ticks: { color: theme.muted, callback: value => `${value}`, font: { size: 10 } }, grid: { color: theme.grid } },
      },
    },
  });

  const categoryTotals = new Map<string, number>();
  items.forEach(item => {
    if (!item.resumenGastos || typeof item.resumenGastos !== 'object') return;
    Object.entries(item.resumenGastos).forEach(([category, amount]) => {
      categoryTotals.set(category, (categoryTotals.get(category) || 0) + (Number(amount) || 0));
    });
  });
  const categoryEntries = [...categoryTotals.entries()].filter(([, amount]) => amount > 0).sort((a, b) => b[1] - a[1]);
  const hasCategories = categoryEntries.length > 0;
  categoryCanvas.classList.toggle('hidden', !hasCategories);
  categoryEmpty?.classList.toggle('hidden', hasCategories);

  if (hasCategories) {
    categoryChart = new Chart(categoryCanvas, {
      type: 'doughnut',
      data: {
        labels: categoryEntries.map(([category]) => category),
        datasets: [{ data: categoryEntries.map(([, amount]) => amount), backgroundColor: categoryEntries.map((_, index) => COLORS[index % COLORS.length]), borderColor: document.documentElement.classList.contains('dark') ? '#0F172A' : '#FFFFFF', borderWidth: 3, hoverOffset: 7 }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '64%',
        animation: { duration: 650 },
        plugins: {
          legend: { position: 'bottom', labels: { color: theme.text, usePointStyle: true, boxWidth: 8, padding: 13, font: { size: 10, weight: 600 } } },
          tooltip: {
            backgroundColor: theme.tooltip,
            titleColor: theme.tooltipText,
            bodyColor: theme.tooltipText,
            borderColor: theme.grid,
            borderWidth: 1,
            padding: 12,
            callbacks: { label: context => `${context.label}: ${options.currencySymbol || 'L'} ${Number(context.parsed || 0).toLocaleString('es-HN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${options.currencyCode || 'HNL'}` },
          },
        },
      },
    });
  }

  const firstScore = scores[0] || 0;
  const lastScore = scores.at(-1) || 0;
  const scoreDelta = lastScore - firstScore;
  setText('historyTrendInsight', scoreDelta === 0 ? 'Score estable' : `${scoreDelta > 0 ? '+' : ''}${scoreDelta} puntos`);
  setTone('historyTrendInsight', scoreDelta > 0 ? 'positive' : scoreDelta < 0 ? 'warning' : 'neutral');

  const dominant = categoryEntries[0];
  const totalCategories = categoryEntries.reduce((total, [, amount]) => total + amount, 0);
  setText('historyCategoryInsight', dominant ? `${dominant[0]} · ${percentage(dominant[1], totalCategories).toFixed(1)}%` : 'Sin categorías disponibles');
  setTone('historyCategoryInsight', dominant ? 'neutral' : 'warning');

  const averageDebt = items.reduce((total, item) => total + Number(item.nivelEndeudamiento || 0), 0) / items.length;
  setText('historyDebtInsight', `${averageDebt.toFixed(1)}% promedio`);
  setTone('historyDebtInsight', averageDebt <= 30 ? 'positive' : averageDebt <= 50 ? 'warning' : 'warning');
}
