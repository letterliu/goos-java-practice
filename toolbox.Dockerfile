FROM eclipse-temurin:8-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    xvfb \
    libxrender1 \
    libxtst6 \
    libxi6 \
    && apt-get clean

WORKDIR /app