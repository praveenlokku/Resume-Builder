# 🚀 Resume-Builder: The AI-Powered Career Accelerator

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-8-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![SambaNova AI](https://img.shields.io/badge/AI--Powered-SambaNova-orange?style=for-the-badge)](https://sambanova.ai/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

**Resume-Builder** is an ultra-premium, high-fidelity career platform designed to help job seekers create recruiter-ready, ATS-friendly resumes in minutes. Powered by the **SambaNova AI API** (Meta-Llama-3.1-8B-Instruct), it provides real-time ATS scoring, intelligent job matching, and seamless PDF generation.

---

## 🌟 Key Features

- **💎 Ultra-Premium UI/UX**: A stunning "Zinc & Slate" design system with atmospheric gradients and Bento-style feature grids.
- **🤖 AI-Powered ATS Analysis**: Instantly rate your resume's structural integrity and readability for automated tracking systems.
- **🎯 Smart Job Matching**: Compare your resume against a target job description to identify matched and missing keywords.
- **📄 High-Fidelity PDF Export**: Generate pixel-perfect, one-page A4 resumes that respect professional layouts.
- **🔄 Real-Time Synchronization**: Auto-save functionality ensures your progress is never lost.
- **🔐 Secure Authentication**: Robust login and signup system to manage your personal resume portfolio.
- **🛠️ Smart Defaults**: Pre-fill resumes using your profile settings for a friction-less experience.

---

## 🛠️ Tech Stack

### Core Backend
- **Framework**: Spring Boot 2.7.18
- **Language**: Java 8
- **Security**: Spring Security
- **Database**: H2 (In-memory for Dev), PostgreSQL (Production)
- **ORM**: Spring Data JPA

### Premium Frontend
- **Templating**: Thymeleaf
- **Styles**: Vanilla CSS (Zinc & Slate Design System)
- **Features**: Interactive Bento Grids, Glassmorphism, Responsive Layouts

### AI Implementation
- **Provider**: SambaNova AI
- **Model**: Meta-Llama-3.1-8B-Instruct
- **Capabilities**: PDF Text Extraction, Semantic Analysis, Keyword Matching

---

## 🚀 Getting Started

### Prerequisites
- **Java 8** or higher
- **Maven 3.6+**
- **SambaNova API Key** (Get it at [SambaNova Cloud](https://cloud.sambanova.ai/))

### Environment Configuration
Set the following environment variables or update `src/main/resources/application.properties`:

```properties
sambanova.api.key=YOUR_API_KEY_HERE
sambanova.api.url=https://api.sambanova.ai/v1/chat/completions
```

### Installation & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/praveenlokku/Resume-Builder.git
   cd Resume-Builder
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   The app will be available at `http://localhost:8080`.

---

## 🐳 Docker Deployment

Build and run using Docker:

```bash
docker build -t resume-builder .
docker run -p 8080:8080 -e SAMBANOVA_API_KEY=your_key resume-builder
```

---

## 📁 Project Structure

```text
src/main/java/com/resumebuilder/
├── controller/     # Web & API Endpoints
├── service/        # AI, PDF & Business Logic
├── model/          # JPA Entities
├── repository/     # Data Access Layer
└── config/         # Security & App Configuration
```

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">Made with ❤️ for Job Seekers by Praveen</p>
