import Google from '@auth/core/providers/google';
import Credentials from '@auth/core/providers/credentials';
import { defineConfig } from 'auth-astro';

const apiUrl = process.env.PUBLIC_API_URL || import.meta.env.PUBLIC_API_URL || 'http://localhost:8080';

export default defineConfig({
  providers: [
    Google({
      clientId: process.env.GOOGLE_CLIENT_ID || import.meta.env.GOOGLE_CLIENT_ID || '',
      clientSecret: process.env.GOOGLE_CLIENT_SECRET || import.meta.env.GOOGLE_CLIENT_SECRET || '',
    }),
    Credentials({
      name: 'Credentials',
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" }
      },
      async authorize(credentials) {
        try {
          const res = await fetch(`${apiUrl}/api/v1/auth/login`, {
            method: 'POST',
            body: JSON.stringify({ email: credentials?.email, password: credentials?.password }),
            headers: { "Content-Type": "application/json" }
          });
          
          if (!res.ok) return null;
          
          const user = await res.json();
          if (user) {
            return user;
          }
          return null;
        } catch (error) {
          console.error("Auth error:", error);
          return null;
        }
      }
    })
  ],
  secret: process.env.AUTH_SECRET || import.meta.env.AUTH_SECRET || process.env.JWT_SECRET,
  trustHost: true,
  callbacks: {
    async redirect({ url, baseUrl }) {
      if (url.startsWith("/")) return `${baseUrl}${url}`;
      else if (new URL(url).origin === baseUrl) return url;
      return `${baseUrl}/dashboard`;
    }
  },
  pages: {
    signOut: '/logout'
  }
});
