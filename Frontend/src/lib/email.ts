import nodemailer from 'nodemailer';

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: import.meta.env.GMAIL_USER,
    pass: import.meta.env.GMAIL_PASS
  }
});

export const sendWelcomeEmail = async (email: string) => {
  const mailOptions = {
    from: import.meta.env.GMAIL_USER,
    to: email,
    subject: 'Bienvenido a nuestra aplicación',
    text: 'Gracias por registrarte en nuestra plataforma. ¡Estamos felices de tenerte!'
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    return info;
  } catch (error) {
    console.error('Error sending welcome email:', error);
  }
};

export const sendPasswordResetEmail = async (email: string, token: string) => {
  const mailOptions = {
    from: import.meta.env.GMAIL_USER,
    to: email,
    subject: 'Restablecer contraseña',
    text: `Para restablecer tu contraseña, usa el siguiente token o enlace: ${token}` // This will usually be a link in a real app
  };

  try {
    const info = await transporter.sendMail(mailOptions);
    return info;
  } catch (error) {
    console.error('Error sending password reset email:', error);
  }
};
