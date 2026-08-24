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
  confirmPassword: string;
  paisId: number;
  monedaId: number;
  acceptedTerms: boolean;
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
  if (!payload.email || !payload.email.trim() || !payload.email.includes('@')) {
    return { valid: false, error: 'Ingresa un correo electrónico válido.' };
  }
  if (!passwordRequirementStatus(payload.password).valid) {
    return { valid: false, error: 'La contraseña debe cumplir todos los requisitos de seguridad.' };
  }
  if (payload.password !== payload.confirmPassword) {
    return { valid: false, error: 'Las contraseñas no coinciden.' };
  }
  if (!payload.paisId || payload.paisId <= 0) {
    return { valid: false, error: 'Debe seleccionar un país válido.' };
  }
  if (!payload.monedaId || payload.monedaId <= 0) {
    return { valid: false, error: 'Debe seleccionar una moneda válida.' };
  }
  if (!payload.acceptedTerms) {
    return { valid: false, error: 'Debes aceptar los Términos de Servicio y el Aviso de Privacidad.' };
  }
  return { valid: true };
}

export function passwordRequirementStatus(password: string) {
  const value = password || '';
  const requirements = {
    length: value.length >= 8,
    uppercase: /[A-ZÁÉÍÓÚÑ]/.test(value),
    lowercase: /[a-záéíóúñ]/.test(value),
    number: /\d/.test(value),
    special: /[^A-Za-zÁÉÍÓÚÑáéíóúñ0-9]/.test(value),
  };

  return {
    ...requirements,
    valid: Object.values(requirements).every(Boolean),
  };
}
