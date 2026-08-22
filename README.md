# 📊 FinanceAI - Asistente Inteligente de Salud Financiera

![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow)
![Fase](https://img.shields.io/badge/Fase-Implementación%20y%20Pruebas-blue)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen)
![Data Science](https://img.shields.io/badge/AI%2FML-Python-blue)

> **Nota:** Este repositorio contiene el Producto Mínimo Viable (MVP) de **FinanceAI**. La arquitectura base, los modelos de Machine Learning y los endpoints principales de la API ya se encuentran implementados e integrados.

## 🎯 El Desafío
En la actualidad, el mercado de fintechs, bancos digitales y plataformas de educación financiera está en constante expansión. Muchas personas tienen acceso a los datos brutos de sus transacciones, pero **tienen gran dificultad para transformar esa información en conocimiento útil** para la toma de decisiones financieras a nivel personal.

## 💡 Nuestro Objetivo
**FinanceAI** es una solución inteligente orientada a usuarios de carteras digitales e instituciones financieras. Nuestro objetivo principal es **analizar el comportamiento financiero de un usuario a partir de sus transacciones para generar una visión completa y clara de su salud financiera.**

Buscamos transformar datos financieros aislados en información procesable que permita a los usuarios:
- Organizar y categorizar automáticamente sus gastos e ingresos.
- Entender exactamente hacia dónde se está dirigiendo su dinero mediante dashboards interactivos.
- Identificar hábitos financieros y perfiles de usuario (ej. *Saludable, En riesgo*).
- Recibir recomendaciones simples y objetivas de mejora.

## 🛠️ Arquitectura y Tecnologías
El proyecto está estructurado en módulos especializados para garantizar escalabilidad, seguridad y eficiencia:

### ⚙️ Backend (Java / Spring Boot)
El núcleo de la aplicación es una API REST robusta que gestiona la lógica de negocio y la seguridad:
- **Framework:** Spring Boot (Java).
- **Seguridad:** Spring Security con autenticación basada en tokens JWT.
- **Base de Datos:** Migraciones controladas y gestión de datos transaccionales.
- **Documentación:** Swagger / OpenAPI para la exploración y prueba de endpoints.
- **Módulos principales:** `Auth`, `Dashboard`, `Transaction`, `Analisis`, `User`.

### 🧠 Motor de Clasificación (Data Science)
Procesamiento de datos y modelado predictivo:
- **Lenguaje:** Python (Jupyter Notebooks).
- **Machine Learning:** Modelos de clasificación para categorizar transacciones automáticamente y determinar el perfil financiero.
- **Datos:** Datasets estructurados (`dataset_clasificacion_transacciones.csv`, `dataset_perfiles_financieros.csv`).

### ☁️ Infraestructura (Próximamente)
- Despliegue planificado utilizando los servicios de **Oracle Cloud Infrastructure (OCI)** para garantizar alta disponibilidad y escalabilidad.

## 🚀 Módulos de la API
Actualmente, la API expone los siguientes controladores principales:
- `AuthController`: Gestión de registro, inicio de sesión y sincronización (ej. Google Sync).
- `TransactionController`: Registro y consulta del historial de transacciones.
- `AnalisisController`: Evaluación del perfil financiero y categorización de gastos mediante IA.
- `DashboardController`: Generación de métricas y resúmenes para la interfaz de usuario.
- `UserController`: Administración de perfiles de usuario.

## 🗓️ Roadmap del Proyecto
- [x] Construcción y recolección de los conjuntos de datos (Datasets).
- [x] Análisis Exploratorio de Datos (EDA) y limpieza.
- [x] Entrenamiento del modelo de clasificación de transacciones y perfiles financieros.
- [x] Diseño y desarrollo de los endpoints de la API REST (Spring Boot).
- [x] Implementación de capa de seguridad con JWT.
- [x] Documentación de la API con OpenAPI/Swagger.
- [ ] Integración del Frontend con la API REST.
- [ ] Despliegue e integración en la nube (OCI).

---
*Desarrollado por el Equipo 77 - Hackathon LATAM G9.*