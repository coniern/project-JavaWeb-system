# Project Management Platform

A Java-based project management platform prototype with Spring Boot backend endpoints and static web pages for authentication and project views.

## Project Overview

This repository combines:

- a Spring Boot backend prototype
- REST-style controller endpoints
- static HTML / JSP pages under `web/`
- basic user authentication and project management demos

The project appears to be a hybrid prototype that mixes an older Java Web structure with a newer Spring Boot backend direction.

## Tech Stack

- Java 17
- Spring Boot 3
- Maven
- HTML / JSP / JavaScript
- Tailwind CSS via CDN
- Chart.js

## Main Modules

### Backend Controllers

- `AuthController`
- `UserController`
- `ProjectController`
- `ProjectMemberController`
- `DeploymentController`
- `TemplateController`
- `EnvironmentConfigController`
- `AiDeploymentController`

### Frontend Pages

- `web/index.html`
- `web/login.html`
- `web/register.html`
- JSP pages for list / success / failure flows

## Repository Cleanup

This repository originally included many generated or local-only files such as:

- `.idea/`
- `.vscode/`
- `.trae/`
- `out/`
- `target/`
- `logs/`

These should not be versioned in a long-term portfolio repository and are now covered by `.gitignore`.

## Run

```bash
mvn spring-boot:run
```

Default server:

```text
http://localhost:8080
```

## Notes

- The backend and frontend structure is partially mixed, so this repository is best described as a prototype or transition-stage project.
- A future cleanup pass should remove already tracked generated folders from Git history and unify the app structure.
