export interface HeaderBalanceSelection {
  amount: number;
  label: 'Balance mes:' | 'Ahorro estimado:' | 'Sin movimientos:';
  source: 'monthly-transactions' | 'latest-analysis' | 'empty';
}
export function selectHeaderBalance(summary: any, history: any): HeaderBalanceSelection {
  const income = Number(summary?.ingresosMesActual) || 0;
  const expenses = Number(summary?.gastosMesActual) || 0;

  if (income !== 0 || expenses !== 0) {
    return {
      amount: Number(summary?.balanceMesActual) || 0,
      label: 'Balance mes:',
      source: 'monthly-transactions',
    };
  }

  const analyses = Array.isArray(history) ? history : [];
  const latestAnalysis = analyses.reduce((latest, current) => {
    if (!latest) return current;
    const latestTime = Date.parse(latest.fechaHora || '') || 0;
    const currentTime = Date.parse(current?.fechaHora || '') || 0;
    return currentTime > latestTime ? current : latest;
  }, null);

  if (latestAnalysis) {
    return {
      amount: Number(latestAnalysis.ahorroEstimado) || 0,
      label: 'Ahorro estimado:',
      source: 'latest-analysis',
    };
  }

  return { amount: 0, label: 'Sin movimientos:', source: 'empty' };
}
