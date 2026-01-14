# XSL Tester

Transform your XML with XSLT online.

## Features

- **Saxon 12 HE** - Full XSLT 3.0/3.1 support
- **Xalan 2.7.3** - XSLT 1.0 processor
- PDF generation with Apache FOP 2.11
- HTML preview
- Save and share transformations

## Tech Stack

- Play Framework 3.0
- Java 21
- MariaDB 11
- Bootstrap 5
- ACE Editor

## Running with Docker

### Prerequisites

- Docker and Docker Compose installed
- Docker BuildKit enabled (for faster builds)

### Quick Start

```bash
# Build and run
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

The application will be available at http://localhost:9000

### Docker Commands

```bash
# Start services
docker-compose up

# Stop services
docker-compose down

# Rebuild after code changes
docker-compose build app
docker-compose up

# Rebuild without cache (after dependency changes)
docker-compose build --no-cache app
docker-compose up

# View logs
docker-compose logs -f app

# Reset database
docker-compose down -v
docker-compose up --build
```

### Configuration

Environment variables in `docker-compose.yml`:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | MariaDB JDBC URL | `jdbc:mariadb://db:3306/fiddles` |
| `DB_USER` | Database user | `xsltester` |
| `DB_PASSWORD` | Database password | `xsltester` |

### Ports

| Service | Port | Description |
|---------|------|-------------|
| App | 9000 | Web application |
| MariaDB | 3306 | Database (optional, for debugging) |

## Development

Requires Java 21.

```bash
# Using SDKMAN
sdk use java 21.0.9-tem

# Run in development mode (requires local MariaDB)
./sbt run
```

### Local MariaDB Setup

```bash
# Start only the database
docker-compose up db

# Then run the app locally
./sbt run
```

## Changelog

### Version 2.0
- Migrated to Play Framework 3.0
- Updated to Java 21
- Saxon 12 HE (removed EE and Saxon 6)
- Xalan 2.7.3
- Apache FOP 2.11
- MariaDB database
- Docker support with optimized multi-stage builds
- Modern JavaScript (no jQuery)
- Bootstrap 5

### Version 1.2
- Preview of PDF or HTML when results are in the correct format/doctype
- Added Saxon EE (XSLT 3.0)

### Version 1.1
- Added ability to switch between XSLT engines

### Version 1.0
- Initial release
