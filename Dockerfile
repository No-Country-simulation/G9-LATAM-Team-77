FROM ubuntu:24.04

# Evitar prompts interactivos
ENV DEBIAN_FRONTEND=noninteractive

# Instalar dependencias necesarias: Java 17 y Python 3 (Python 3.12 en Ubuntu 24.04)
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar el entorno Data Science
COPY DataScient/ /app/DataScient/

# Configurar el entorno virtual y dependencias de Python
RUN cd /app/DataScient && \
    python3 -m venv venv && \
    ./venv/bin/pip install --upgrade pip && \
    ./venv/bin/pip install -r requirements.txt

# Copiar el backend
COPY Backend/financeia-backend/ /app/Backend/

# Construir el backend
RUN chmod +x /app/Backend/mvnw && \
    cd /app/Backend && \
    ./mvnw clean package -DskipTests

# Variables de entorno para que Spring Boot encuentre a Python
ENV PYTHON_COMMAND=/app/DataScient/venv/bin/python
ENV DATA_SCIENCE_SCRIPT=/app/DataScient/src/predict.py
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/Backend/target/financeia-backend-0.0.1-SNAPSHOT.jar"]
