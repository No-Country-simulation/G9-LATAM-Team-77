import Google from '@auth/core/providers/google';
import Credentials from '@auth/core/providers/credentials';
import { defineConfig } from 'auth-astro';

export default defineConfig({
  providers: [
    Google({
      clientId: import.meta.env.GOOGLE_CLIENT_ID,
      clientSecret: import.meta.env.GOOGLE_CLIENT_SECRET,
    }),
    Credentials({
      name: 'Credentials',
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" }
      },
      async authorize(credentials) {
        try {
          const res = await fetch("http://localhost:8080/api/v1/auth/login", {
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
          console.error("Auth error", error);
          return null;
        }
      }
    })
  ],
  secret: import.meta.env.AUTH_SECRET || "supersecret"
});
