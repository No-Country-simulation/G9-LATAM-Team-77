import { API_URL } from '../../lib/api';
import type { APIRoute } from 'astro';
import { sendPasswordResetEmail } from '../../lib/email';

export const POST: APIRoute = async ({ request }) => {
  try {
    const body = await request.json();
    const { email } = body;

    if (!email) {
      return new Response(JSON.stringify({ error: "Email is required" }), {
        status: 400,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    // Call backend API to request password reset token if necessary, or just mock it here
    const res = await fetch(`${API_URL}/api/v1/auth/reset-password`, {
      method: "POST",
      body: JSON.stringify({ email }),
      headers: { "Content-Type": "application/json" }
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({}));
      return new Response(JSON.stringify({ error: errorData.message || "Failed to request password reset" }), {
        status: res.status,
        headers: { 'Content-Type': 'application/json' }
      });
    }

    const data = await res.json();
    const token = data.token || "mock-token-if-backend-does-not-return-one";

    // Send password reset email
    await sendPasswordResetEmail(email, token);

    return new Response(JSON.stringify({ success: true, message: "Email sent" }), {
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
