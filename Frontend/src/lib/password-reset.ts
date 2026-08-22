export const FORGOT_PASSWORD_CONFIRMATION =
  'Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.';

export function validateForgotPasswordEmail(email: string): string | null {
  const normalized = email.trim();
  if (!normalized) return 'Ingresa tu correo electrónico.';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalized)) {
    return 'Ingresa un correo electrónico válido.';
  }
  return null;
}

export function validateResetPassword(
  token: string,
  newPassword: string,
  confirmation: string,
): string | null {
  if (!token.trim()) return 'El enlace de recuperación no es válido.';
  if (newPassword.length < 8 || newPassword.length > 128) {
    return 'La contraseña debe tener entre 8 y 128 caracteres.';
  }
  if (newPassword !== confirmation) return 'Las contraseñas no coinciden.';
  return null;
}

export async function readApiMessage(response: Response, fallback: string): Promise<string> {
  try {
    const data = await response.json();
    return typeof data?.message === 'string' && data.message.trim()
      ? data.message
      : fallback;
  } catch {
    return fallback;
  }
}
