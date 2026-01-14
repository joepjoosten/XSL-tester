# XSL Tester

Transform your XML with XSLT online.

## Features

- **Saxon 12 HE** - Full XSLT 3.0/3.1 support
- **Xalan 2.7.3** - XSLT 1.0 processor
- PDF generation with Apache FOP
- HTML preview
- Save and share transformations

## Tech Stack

- Play Framework 3.0
- Java 21
- MariaDB
- Bootstrap 5
- ACE Editor

## Running with Docker

```bash
docker-compose up --build
```

The application will be available at http://localhost:9000

## Development

Requires Java 21.

```bash
# Using SDKMAN
sdk use java 21.0.9-tem

# Run in development mode
./sbt run
```

## Changelog

### Version 2.0
- Migrated to Play Framework 3.0
- Updated to Java 21
- Saxon 12 HE (removed EE and Saxon 6)
- Xalan 2.7.3
- MariaDB database
- Docker support
- Modern JavaScript (no jQuery)
- Bootstrap 5

### Version 1.2
- Preview of PDF or HTML when results are in the correct format/doctype
- Added Saxon EE (XSLT 3.0)

### Version 1.1
- Added ability to switch between XSLT engines

### Version 1.0
- Initial release
