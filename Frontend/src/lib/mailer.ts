import nodemailer from 'nodemailer';

function getTransporter() {
    const rawPass = process.env.GMAIL_PASS || import.meta.env.GMAIL_PASS || '';
    const pass = rawPass.replace(/\s+/g, '');
    const user = (process.env.GMAIL_USER || import.meta.env.GMAIL_USER || '').trim();

    return nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: user,
            pass: pass
        }
    });
}

/**
 * Plantilla Base de Correo Electrónico Elegante
 */
function createEmailTemplate(title: string, subtitle: string, code: string, messageText: string, footerText: string) {
    return `
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${title}</title>
        <style>
            @import url('https://fonts.googleapis.com/css2?family=Josefin+Sans:wght@400;600;700&family=Inter:wght@400;500;600&display=swap');
            body {
                margin: 0;
                padding: 0;
                background-color: #0b132b;
                font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                color: #e2e8f0;
            }
            .container {
                max-width: 540px;
                margin: 40px auto;
                background: linear-gradient(180deg, #111c44 0%, #0b132b 100%);
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 28px;
                overflow: hidden;
                box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
            }
            .header {
                padding: 40px 40px 20px 40px;
                text-align: center;
            }
            .logo-dots {
                display: inline-flex;
                gap: 6px;
                margin-bottom: 12px;
            }
            .dot {
                width: 10px;
                height: 10px;
                border-radius: 50%;
                display: inline-block;
            }
            .dot-blue { background-color: #3b82f6; }
            .dot-red { background-color: #ef4444; }
            .dot-yellow { background-color: #f59e0b; }
            .logo-text {
                font-family: 'Josefin Sans', Georgia, serif;
                font-size: 26px;
                font-weight: 700;
                color: #ffffff;
                letter-spacing: -0.5px;
                margin: 0;
            }
            .content {
                padding: 20px 40px 40px 40px;
                text-align: center;
            }
            .title {
                font-family: 'Josefin Sans', Georgia, serif;
                font-size: 24px;
                font-weight: 700;
                color: #ffffff;
                margin: 0 0 10px 0;
            }
            .subtitle {
                font-size: 14px;
                color: #94a3b8;
                line-height: 1.6;
                margin: 0 0 30px 0;
            }
            .code-box {
                background: rgba(255, 255, 255, 0.04);
                border: 1px solid rgba(59, 130, 246, 0.3);
                border-radius: 20px;
                padding: 24px;
                margin: 20px 0 30px 0;
                box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.15);
            }
            .code-label {
                font-size: 11px;
                font-weight: 600;
                color: #60a5fa;
                text-transform: uppercase;
                letter-spacing: 2px;
                margin-bottom: 8px;
            }
            .code-value {
                font-family: 'Josefin Sans', monospace, sans-serif;
                font-size: 38px;
                font-weight: 700;
                color: #ffffff;
                letter-spacing: 12px;
                margin-left: 12px;
            }
            .info-text {
                font-size: 13px;
                color: #64748b;
                line-height: 1.5;
            }
            .footer {
                padding: 25px 40px;
                background-color: rgba(0, 0, 0, 0.3);
                border-top: 1px solid rgba(255, 255, 255, 0.05);
                text-align: center;
                font-size: 11px;
                color: #64748b;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <div class="logo-dots">
                    <span class="dot dot-blue"></span>
                    <span class="dot dot-red"></span>
                    <span class="dot dot-yellow"></span>
                </div>
                <h1 class="logo-text">FinanceAI</h1>
            </div>
            <div class="content">
                <h2 class="title">${title}</h2>
                <p class="subtitle">${subtitle}</p>
                <div class="code-box">
                    <div class="code-label">Código de Verificación</div>
                    <div class="code-value">${code}</div>
                </div>
                <p class="info-text">${messageText}</p>
            </div>
            <div class="footer">
                ${footerText}<br>
                © 2026 FinanceAI Technologies. Todos los derechos reservados.
            </div>
        </div>
    </body>
    </html>
    `;
}

/**
 * Enviar Código de Verificación de Cuenta
 */
export async function sendVerificationEmail(toEmail: string, code: string, name?: string) {
    const user = (process.env.GMAIL_USER || import.meta.env.GMAIL_USER || '').trim();
    const transporter = getTransporter();
    const greeting = name ? `Hola ${name}` : 'Hola';
    const html = createEmailTemplate(
        'Verifica tu Cuenta',
        `${greeting}, gracias por unirte a FinanceAI. Para completar la creación de tu cuenta y proteger tus datos, utiliza el siguiente código de seguridad.`,
        code,
        'Este código es válido durante los próximos 15 minutos. Si tú no solicitaste esta cuenta, puedes ignorar este mensaje de forma segura.',
        'Mensaje de seguridad generado automáticamente por el sistema de verificación.'
    );

    const plainText = `${greeting},\n\nTu código de verificación para FinanceAI es: ${code}\n\nEste código es válido por 15 minutos.\n\n© 2026 FinanceAI Technologies.`;

    return transporter.sendMail({
        from: `"FinanceAI" <${user}>`,
        to: toEmail,
        subject: `FinanceAI - Tu código de verificación: ${code}`,
        text: plainText,
        html: html
    });
}

/**
 * Enviar Código de Restablecimiento de Contraseña
 */
export async function sendRecoveryEmail(toEmail: string, code: string) {
    const user = (process.env.GMAIL_USER || import.meta.env.GMAIL_USER || '').trim();
    const transporter = getTransporter();
    const html = createEmailTemplate(
        'Recuperación de Contraseña',
        'Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en FinanceAI.',
        code,
        'Introduce este código en la pantalla de recuperación para crear una nueva clave. Por motivos de seguridad, nunca compartas este código con nadie.',
        'Si no solicitaste este cambio, ponte en contacto de inmediato con soporte.'
    );

    const plainText = `Hemos recibido una solicitud para restablecer tu contraseña en FinanceAI.\n\nTu código de recuperación es: ${code}\n\nIntroduce este código para crear una nueva contraseña.\n\n© 2026 FinanceAI Technologies.`;

    return transporter.sendMail({
        from: `"FinanceAI" <${user}>`,
        to: toEmail,
        subject: `FinanceAI - Código de recuperación: ${code}`,
        text: plainText,
        html: html
    });
}
