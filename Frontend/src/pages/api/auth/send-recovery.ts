import type { APIRoute } from 'astro';
import { sendRecoveryEmail } from '../../../lib/mailer';

export const POST: APIRoute = async ({ request }) => {
    try {
        const body = await request.json();
        const { email, code } = body;

        if (!email || !code) {
            return new Response(JSON.stringify({ error: 'Faltan parámetros obligatorios (email o código).' }), {
                status: 400,
                headers: { 'Content-Type': 'application/json' }
            });
        }

        await sendRecoveryEmail(email, code);

        return new Response(JSON.stringify({ success: true, message: 'Código de recuperación enviado exitosamente por correo.' }), {
            status: 200,
            headers: { 'Content-Type': 'application/json' }
        });
    } catch (error: any) {
        console.error('Error al enviar correo de recuperación con Nodemailer:', error);
        return new Response(JSON.stringify({ error: 'No se pudo enviar el correo de recuperación.', details: error.message }), {
            status: 500,
            headers: { 'Content-Type': 'application/json' }
        });
    }
};
