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

    const backendUrl = process.env.INTERNAL_BACKEND_URL || 'http://127.0.0.1:8080';
    // Call backend API to register
    const res = await fetch(`${backendUrl}/api/v1/auth/register`, {
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
  } catch (error: any) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' }
    });
  }
};
