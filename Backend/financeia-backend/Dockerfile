# syntax=docker/dockerfile:1

FROM maven:3.9.11-eclipse-temurin-17 AS backend-build
WORKDIR /workspace/backend

COPY Backend/financeia-backend/pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests dependency:go-offline

COPY Backend/financeia-backend/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package && \
    cp target/financeia-backend-*.jar /workspace/financeia-backend.jar

FROM python:3.12-slim-bookworm AS runtime

RUN apt-get update && \
    apt-get install --yes --no-install-recommends \
        ca-certificates \
        curl \
        openjdk-17-jre-headless && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt/financeai-data-science
COPY financeai-data-science/requirements.txt ./requirements.txt
RUN --mount=type=cache,target=/root/.cache/pip \
    python -m pip install --disable-pip-version-check -r requirements.txt

COPY financeai-data-science/src ./src
COPY financeai-data-science/models ./models

WORKDIR /app
COPY --from=backend-build /workspace/financeia-backend.jar ./financeia-backend.jar

RUN groupadd --system financeai && \
    useradd --system --gid financeai --home-dir /app financeai && \
    chown -R financeai:financeai /app /opt/financeai-data-science

ENV PYTHON_COMMAND=python3 \
    DATA_SCIENCE_SCRIPT=/opt/financeai-data-science/src/predict.py \
    DATA_SCIENCE_TIMEOUT_SECONDS=30

USER financeai
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/financeia-backend.jar"]
