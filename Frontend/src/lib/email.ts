import nodemailer from 'nodemailer';

function getTransporter() {
  const user = process.env.GMAIL_USER || import.meta.env.GMAIL_USER || '';
  const pass = (process.env.GMAIL_PASS || import.meta.env.GMAIL_PASS || '').replace(/\s+/g, '');

  return nodemailer.createTransport({
    service: 'gmail',
    auth: {
      user: user,
      pass: pass
    }
  });
}

export const sendWelcomeEmail = async (email: string) => {
  const user = process.env.GMAIL_USER || import.meta.env.GMAIL_USER || '';
  const transporter = getTransporter();

  const mailOptions = {
    from: `"FinanceAI" <${user}>`,
    to: email,
    subject: 'Bienvenido a FinanceAI',
    text: 'Gracias por registrarte en nuestra plataforma de salud financiera. ¡Estamos felices de tenerte!'
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    return info;
  } catch (error) {
    console.error('Error enviando correo de bienvenida:', error);
  }
};

export const sendPasswordResetEmail = async (email: string, token: string) => {
  const user = process.env.GMAIL_USER || import.meta.env.GMAIL_USER || '';
  const transporter = getTransporter();

  const mailOptions = {
    from: `"FinanceAI Seguridad" <${user}>`,
    to: email,
    subject: 'Restablecer contraseña - FinanceAI',
    text: `Para restablecer tu contraseña, usa el siguiente token de seguridad: ${token}`
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    return info;
  } catch (error) {
    console.error('Error enviando correo de restablecimiento:', error);
  }
};
