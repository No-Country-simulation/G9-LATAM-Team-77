export interface UserData {
  id?: number;
  nombre?: string;
  apellido?: string;
  email: string;
  token: string;
  [key: string]: any;
}

export interface RegisterPayload {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  paisId: number;
  monedaId: number;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export const AUTH_STORAGE_KEY = 'financeai_token';
export const USER_STORAGE_KEY = 'financeai_user';

export function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(AUTH_STORAGE_KEY);
}

export function setAuthSession(token: string, userData: any): void {
  localStorage.setItem(AUTH_STORAGE_KEY, token);
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userData));
}

export function clearAuthSession(): void {
  localStorage.removeItem(AUTH_STORAGE_KEY);
  localStorage.removeItem(USER_STORAGE_KEY);
}

export function isAuthenticated(): boolean {
  return !!getAuthToken();
}

export function validateLoginInput(payload: LoginPayload): { valid: boolean; error?: string } {
  if (!payload.email || !payload.email.trim()) {
    return { valid: false, error: 'El correo electrónico es requerido.' };
  }
  if (!payload.email.includes('@')) {
    return { valid: false, error: 'Formato de correo electrónico inválido.' };
  }
  if (!payload.password || payload.password.length < 4) {
    return { valid: false, error: 'La contraseña debe tener al menos 4 caracteres.' };
  }
  return { valid: true };
}

export function validateRegisterInput(payload: RegisterPayload): { valid: boolean; error?: string } {
  if (!payload.nombre || !payload.nombre.trim()) {
    return { valid: false, error: 'El nombre es requerido.' };
  }
  if (!payload.apellido || !payload.apellido.trim()) {
    return { valid: false, error: 'El apellido es requerido.' };
  }
  const loginValidation = validateLoginInput({ email: payload.email, password: payload.password });
  if (!loginValidation.valid) {
    return loginValidation;
  }
  if (!payload.paisId || payload.paisId <= 0) {
    return { valid: false, error: 'Debe seleccionar un país válido.' };
  }
  if (!payload.monedaId || payload.monedaId <= 0) {
    return { valid: false, error: 'Debe seleccionar una moneda válida.' };
  }
  return { valid: true };
}
