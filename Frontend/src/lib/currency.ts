/**
 * Servicio Oficial de Conversión de Monedas - FinanceAI
 * Tasas de cambio fijas oficiales según matriz de equivalencia base USD.
 */

export interface CurrencyRate {
    name: string;
    code: string;
    usdValue: number; // 1 unidad de esta moneda equivale a X USD
    symbol: string;
}

export const OFFICIAL_CURRENCIES: CurrencyRate[] = [
    { name: 'Dólar estadounidense', code: 'USD', usdValue: 1.00, symbol: '$' },
    { name: 'Peso mexicano', code: 'MXN', usdValue: 0.0591, symbol: '$' },
    { name: 'Euro', code: 'EUR', usdValue: 1.1687, symbol: '€' },
    { name: 'Colón costarricense', code: 'CRC', usdValue: 0.0022, symbol: '₡' },
    { name: 'Peso colombiano', code: 'COP', usdValue: 0.000326, symbol: '$' },
    { name: 'Peso argentino', code: 'ARS', usdValue: 0.000667, symbol: '$' },
    { name: 'Peso chileno', code: 'CLP', usdValue: 0.001093, symbol: '$' },
    { name: 'Sol peruano', code: 'PEN', usdValue: 0.2982, symbol: 'S/' }
];

export const USD_VALUES: { [code: string]: number } = {
    USD: 1.00,
    MXN: 0.0591,
    EUR: 1.1687,
    CRC: 0.0022,
    COP: 0.000326,
    ARS: 0.000667,
    CLP: 0.001093,
    PEN: 0.2982
};

export const CURRENCY_SYMBOLS: { [code: string]: string } = {
    USD: '$',
    MXN: '$',
    EUR: '€',
    CRC: '₡',
    COP: '$',
    ARS: '$',
    CLP: '$',
    PEN: 'S/'
};

/**
 * Convierte un monto de una moneda a otra usando la equivalencia oficial en USD
 */
export function convertCurrency(
    amount: number,
    fromCurrency: string,
    toCurrency: string
): number {
    if (!amount || isNaN(amount)) return 0;
    const from = (fromCurrency || 'USD').toUpperCase();
    const to = (toCurrency || 'USD').toUpperCase();

    if (from === to) return Number(amount);

    const fromVal = USD_VALUES[from] || 1.0;
    const toVal = USD_VALUES[to] || 1.0;

    // Convertir de moneda origen a USD base, luego de USD a moneda destino
    const inUSD = Number(amount) * fromVal;
    const converted = inUSD / toVal;

    return Math.round(converted * 100) / 100;
}

/**
 * Formatea un monto con símbolo y código de divisa
 */
export function formatCurrencyAmount(amount: number, currencyCode: string = 'USD'): string {
    const code = (currencyCode || 'USD').toUpperCase();
    const symbol = CURRENCY_SYMBOLS[code] || '$';
    return `${symbol} ${Number(amount || 0).toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${code}`;
}
