# syntax=docker/dockerfile:1.4

# Base image with sbt pre-installed (cached layer)
FROM eclipse-temurin:21-jdk AS sbt-base
RUN apt-get update && apt-get install -y curl gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --dearmor -o /usr/share/keyrings/sbt-archive-keyring.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/sbt-archive-keyring.gpg] https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    apt-get update && apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

# Dependencies stage - cached unless build.sbt or project/ changes
FROM sbt-base AS dependencies
WORKDIR /app
COPY build.sbt .
COPY project/build.properties project/
COPY project/plugins.sbt project/

# Use BuildKit cache mount for sbt/coursier cache
RUN --mount=type=cache,target=/root/.cache/coursier \
    --mount=type=cache,target=/root/.sbt \
    --mount=type=cache,target=/root/.ivy2 \
    sbt update

# Build stage
FROM dependencies AS builder
WORKDIR /app

# Copy source code
COPY app app
COPY conf conf
COPY public public

# Build with cached dependencies
RUN --mount=type=cache,target=/root/.cache/coursier \
    --mount=type=cache,target=/root/.sbt \
    --mount=type=cache,target=/root/.ivy2 \
    sbt stage

# Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Create non-root user
RUN groupadd -r xsltester && useradd -r -g xsltester xsltester

# Copy the staged application
COPY --from=builder /app/target/universal/stage /app

RUN chown -R xsltester:xsltester /app
USER xsltester

EXPOSE 9000

CMD ["bin/xsl-tester"]
