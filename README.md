# 📊 FinanceAI - Plataforma Integral de Salud Financiera[cite: 2]

> **Plataforma Integral de Salud Financiera con Inteligencia Artificial, Arquitectura Multi-Moneda y Monitoreo en Tiempo Real.**[cite: 2]
> *Versión Corporativa Oficial Extendida y Definitiva — G9-LATAM-Team-77*[cite: 2]

![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow)
![Fase](https://img.shields.io/badge/Fase-Implementación-blue)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203-brightgreen)[cite: 2]
![Frontend](https://img.shields.io/badge/Frontend-Astro%205-orange)[cite: 2]
![AI](https://img.shields.io/badge/AI-Python%203-blue)[cite: 2]

## 👥 Equipo de Desarrollo (G9-LATAM-Team-77)[cite: 2]
- **Data Scientists:** Luisa Fernanda Bedoya, Edmar Mario Urquiza Quispe, Giselle Jacqueline Morales de la Cruz, Kevin Aron Sumire Ccahuana[cite: 2].
- **Backend Developers:** Fernando Jose Reynosa Vidal, Justin Alejandro Aguirre Navarro, Irwing Jonathan Ramirez Paz[cite: 2].

## 🎯 Visión General[cite: 2]
FinanceAI soluciona la dificultad de los usuarios para entender sus hábitos financieros y diagnosticar su nivel de sobreendeudamiento[cite: 2]. El sistema automatiza la ingesta y clasificación de gastos mediante Procesamiento de Lenguaje Natural (NLP) y genera recomendaciones personalizadas con un motor de Inteligencia Artificial Explicable (XAI)[cite: 2]. Actúa como un habilitador para la inclusión financiera mediante una arquitectura multi-moneda que elimina fronteras[cite: 2].

## 🛠️ Arquitectura del Sistema[cite: 2]
El sistema está construido bajo un enfoque modular y fuertemente desacoplado[cite: 2]. El entorno oficial está estandarizado en Java 17 LTS, Spring Boot 3 y Node.js 22 LTS[cite: 2]. Se utiliza una arquitectura centralizada de variables de entorno (archivo `.env` único) para evitar desincronizaciones entre todas las capas[cite: 2].

- **Frontend:** Renderizado del lado del servidor (SSR) desarrollado en Astro 5 y Tailwind CSS[cite: 2].
- **Backend:** API REST con Spring Boot 3 y persistencia en MySQL 8.0 gestionada con migraciones Flyway[cite: 2].
- **Data Science:** Scripts en Python 3 utilizando Scikit-Learn, Pandas y Numpy[cite: 2].
- **Integración IA-Backend:** Ejecución segura de subprocesos mediante `ProcessBuilder` que emite JSON a través de `stdout`[cite: 2].

## 🧠 Capa de Inteligencia Artificial y Ciencia de Datos[cite: 2]
- **Clasificador NLP Jerárquico:** Utiliza `LinearSVC` con vectorización `TfidfVectorizer` para categorizar transacciones, alcanzando un Macro F1 de 0.9781 en pruebas externas reales[cite: 2].
- **Perfilador Financiero:** Emplea `RandomForestClassifier` para calcular la etiqueta de riesgo (Saludable, Riesgo, Crítico) con una precisión superior al 0.98[cite: 2].
- **Motor XAI (Explicabilidad):** Arquitectura de 3 capas que diagnostica el puntaje, identifica la causa raíz evaluando pilares de gasto/ahorro/deuda, y provee una acción recomendada[cite: 2].
- **Neutralidad Multidivisa:** Transforma los datos financieros en ratios adimensionales para garantizar equidad predictiva sin importar la moneda de origen[cite: 2].

## ⚙️ Backend y API REST[cite: 2]
- **Catálogo de Endpoints:** Exposición de 13 endpoints principales protegidos mediante tokens JWT (Stateless)[cite: 2].
- **Gestión de Usuarios:** Soporta flujos de registro, sincronización OAuth2 de Google, y recuperación de contraseñas mediante tokens OTP temporales[cite: 2].
- **Transacciones y Divisas:** El sistema normaliza automáticamente la divisa aplicando conversiones por matriz de paridad al momento de persistir los datos[cite: 2].

## 💻 Frontend y Experiencia de Usuario[cite: 2]
- **Motor Multidivisa en Tiempo Real:** Las conversiones se procesan en memoria (base USD) para actualizar dinámicamente KPIs, gráficas Chart.js y tablas sin saturar al servidor[cite: 2].
- **Seguridad UI y Feedback:** Implementación de una regla estricta Anti-Emojis mediante Regex para prevenir inyecciones, validación de contraseñas con 5 reglas de complejidad visualizadas en tiempo real, y notificaciones Toast semánticas[cite: 2].
- **Danger Zone:** Capacidad para que el usuario elimine su cuenta permanentemente, activando un borrado atómico en cascada en la base de datos[cite: 2].

## 🛡️ Cumplimiento Legal, Seguridad y Despliegue[cite: 2]
- **Marcos Legales y Privacidad:** Cumplimiento de leyes LFPDPPP, derechos ARCO (con botón para descargar expediente JSON), GDPR (Art. 22 cubierto por el motor XAI) y CCPA[cite: 2].
- **Seguridad OWASP:** Mitigación de riesgos del OWASP Top 10, incluyendo aislamiento estricto por `userId`, contraseñas con BCrypt, y prevención de inyecciones SQL y CLI[cite: 2].
- **Infraestructura (OCI):** Despliegue en Oracle Cloud Infrastructure utilizando contenedores orquestados con Docker Compose y un proxy inverso Caddy Server para la renovación automática de certificados SSL[cite: 2].
