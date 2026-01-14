# XSL-Tester Project

## Overview
XSL-Tester is an online XSLT transformation tool that allows users to test and debug XSLT stylesheets against XML documents. It supports multiple XSLT engines and provides features like PDF generation, HTML preview, and fiddle saving/sharing.

## Technology Stack
- **Framework**: Play Framework 3.0 (Java)
- **Build Tool**: SBT 1.10.x
- **Java Version**: 21 (LTS)
- **Database**: MariaDB 11.x
- **ORM**: JPA with Hibernate
- **XSLT Engines**:
  - Saxon 12 HE (XSLT 3.0/3.1)
  - Xalan 2.7.3 (XSLT 1.0)
- **PDF Generation**: Apache FOP 2.9
- **Frontend**: Bootstrap 5, ACE Editor, vanilla JavaScript

## Project Structure
```
├── app/
│   ├── controllers/      # Play controllers
│   ├── models/           # JPA entities
│   ├── plugins/          # XSLT transformer plugins
│   ├── services/         # Business logic
│   └── views/            # Twirl templates
├── conf/
│   ├── application.conf  # App configuration
│   ├── routes            # URL routing
│   └── evolutions/       # Database migrations
├── public/
│   ├── javascripts/      # Client-side JS
│   ├── stylesheets/      # CSS files
│   └── plugins/          # XSLT engine JARs
├── docker/               # Docker-related files
├── build.sbt             # SBT build config
├── docker-compose.yml    # Docker Compose config
└── legacy/               # Old Play 2.2.4 code (reference)
```

## Key Commands

### Development
```bash
# Run in development mode
sbt run

# Run tests
sbt test

# Compile
sbt compile

# Package for production
sbt stage
```

### Docker
```bash
# Build and run with Docker Compose
docker-compose up --build

# Stop containers
docker-compose down

# View logs
docker-compose logs -f app
```

## Architecture

### XSLT Plugin System
Transformers are loaded dynamically via a custom `JarClassLoader`. Each plugin implements `TransformerPlugin` interface:
- `Saxon12HEPlugin` - Saxon 12 Home Edition
- `Xalan2Plugin` - Apache Xalan 2.7.3

### Database Schema
- `fiddle` - Main entity storing fiddle metadata
- `fiddle_revision` - Stores XML/XSL content for each revision

### API Routes
- `GET /` - Main editor page
- `POST /` - Execute transformation
- `POST /save` - Save fiddle
- `POST /pdf` - Generate PDF
- `GET /:id` - Load fiddle by short ID
- `GET /:id/:revision` - Load specific revision

## Configuration
Key configuration in `conf/application.conf`:
- Database connection (DB_URL, DB_USER, DB_PASSWORD env vars)
- Play secret key
- JPA/Hibernate settings

## Development Notes
- XSLT engine JARs are loaded at runtime from `public/plugins/`
- Fiddle IDs use Base58 encoding for short URLs
- PDF generation uses Apache FOP with XSL-FO input
