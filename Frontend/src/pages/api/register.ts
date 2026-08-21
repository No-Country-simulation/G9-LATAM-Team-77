import { API_URL } from '../../lib/api';
import type { APIRoute } from 'astro';
import { sendWelcomeEmail } from '../../lib/email';

export const POST: APIRoute = async ({ request }) => {
  try {
    const body = await request.json();
    const { email, password, name } = body;

    if (!email || !password) {
      return new Response(JSON.stringify({ error: "Email and password are required" }), {
        status: 400,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    // Call backend API to register
    const res = await fetch(`${API_URL}/api/v1/auth/register`, {
      method: "POST",
      body: JSON.stringify({ email, password, name }),
      headers: { "Content-Type": "application/json" }
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      return new Response(JSON.stringify({ error: errorData.message || "Registration failed" }), {
        status: res.status,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    const data = await res.json();

    // Send welcome email after successful registration
    await sendWelcomeEmail(email);

    return new Response(JSON.stringify({ success: true, data }), {
      status: 200,
      headers: { 'Content-Type': 'application/json' }
    });
  } catch {
    return new Response(JSON.stringify({ error: 'No fue posible completar el registro' }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' }
    });
  }
};
