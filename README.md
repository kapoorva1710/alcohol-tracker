# 🍷 Alcohol Tracker

**Live Dashboard:** [https://kapoorva1710.github.io/alcohol-tracker/](https://kapoorva1710.github.io/alcohol-tracker/)

An automated, privacy-focused Full-Stack application designed to seamlessly track your alcohol consumption without the friction of manual logging. It integrates with your email for automated receipt parsing and provides secure webhooks for mobile automation apps.

## ✨ Features
*   **Automated Email Parsing:** A background service that securely scans your inbox via IMAP for digital receipts (e.g., Uber Eats, local bars) and automatically logs drinks based on keyword detection.
*   **Mobile Webhooks API:** Built-in REST endpoints designed to connect with Apple Shortcuts or Android Tasker, allowing you to log drinks directly via text messages or health data (sleep quality, HRV).
*   **Interactive Dashboard:** A sleek, mobile-responsive frontend built with a modern "Samsung Health" aesthetic, utilizing Chart.js for visualization.
*   **Decoupled Architecture:** The frontend is hosted entirely on GitHub Pages, communicating dynamically with the live Java backend server.

## 🛠️ Technology Stack
*   **Backend:** Java 17, Spring Boot, Spring Data JPA, Spring Mail (IMAP)
*   **Frontend:** Vanilla HTML5, CSS3, JavaScript, Chart.js
*   **Database:** H2 (In-Memory) for local development, PostgreSQL ready for production
*   **Deployment:** Dockerized for Render/Railway (Backend), GitHub Pages (Frontend)

## 🚀 How to Run Locally

### Prerequisites
*   Java 17+
*   Maven

### 1. Clone & Configure
Clone the repository and open `src/main/resources/application.properties`. 
Update the IMAP credentials to test the email scanner. Note: You must use an **App Password**, not your standard email password.
```properties
tracker.mail.host=imap.gmail.com
tracker.mail.username=your-email@gmail.com
tracker.mail.password=your-app-password
```

### 2. Run the Server
Use the included Maven wrapper to start the Spring Boot application:
```bash
./mvnw spring-boot:run
```
The backend API will now be running on `http://localhost:8080`.

## ☁️ Deployment Architecture
This project is configured for split deployment:
1.  **Frontend:** The `docs/` folder is configured to be served via **GitHub Pages**.
2.  **Backend:** The root `Dockerfile` and `application.properties` are configured for one-click deployment to PaaS providers like **Render** or **Railway**. The application relies on environment variables (`DB_URL`, `DB_USER`, `DB_PASS`) to connect to a production PostgreSQL database.

---
*Created as a personal project to automate health tracking and learn decoupled Full-Stack architecture.*
