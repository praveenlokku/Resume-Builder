---
title: Resume-Builder
emoji: 📄
colorFrom: indigo
colorTo: slate
sdk: docker
app_port: 8080
---

# ResumePRO - Professional ATS Resume Builder

A high-fidelity, multi-template resume builder with professional settings, auto-save, and PDF export.

## Deployment
Deployed on Hugging Face Spaces using Docker.

## Setup
1. Clone the repository.
2. Run `mvn spring-boot:run` for local development (H2 Database).
3. Use the `prod` profile for PostgreSQL/Neon DB deployment.
