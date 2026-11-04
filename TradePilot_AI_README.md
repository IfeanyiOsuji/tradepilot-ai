# TradePilot AI

**AI-assisted business intelligence for small businesses**

TradePilot AI helps business owners record sales, track expenses, manage inventory, and explore business performance through a conversational assistant. Rather than requiring users to interpret spreadsheets, it brings operational records and financial summaries into one workspace.

## The problem

Small businesses often keep sales, costs, and stock records in disconnected places. This makes it difficult to understand cash flow, identify fast-moving products, or decide what to restock.

## What TradePilot AI does

- **Sales management:** Record product and service sales in supported currencies.
- **Expense tracking:** Record categorized expenses and review spending.
- **Inventory management:** Track products, stock levels, and sales from inventory.
- **Business dashboard:** View revenue, expenses, net cash flow, and daily financial trends.
- **Business analytics:** Explore product performance, low stock, inventory valuation, and period comparisons.
- **Conversational assistant:** Ask natural-language questions about recorded business performance and receive readable explanations.
- **User accounts:** JWT-based authentication with user-owned business records and administrator-wide read access.

> **AI integration note:** Add the exact model/provider and integration details after confirming them in the backend source. Do not describe the assistant as using a particular Meta model unless the code and demo establish that fact.

## Technology stack

| Layer | Technology |
| --- | --- |
| Frontend | React, TypeScript, Vite |
| Backend | Java 21, Spring Boot |
| Database | PostgreSQL |
| Authentication | Spring Security, signed JWT access tokens |
| Build | Maven Wrapper, npm |
| Build | Maven Wrapper, npm |


## Project Repositories

TradePilot AI is a full-stack AI-powered business intelligence platform built with Java, Spring Boot, PostgreSQL, React, and TypeScript.

The project consists of two repositories:

### Backend
https://github.com/IfeanyiOsuji/tradepilot-ai

Contains the REST APIs, authentication, business logic, financial analytics, inventory management, and AI integration.

### Frontend
https://github.com/IfeanyiOsuji/tradepilot-frontend

Contains the React user interface, business dashboard, sales and expense management, inventory screens, and AI assistant interface.

## Architecture

```text
Browser (React + TypeScript)
          |
          | HTTP / JSON + Bearer JWT
          v
Spring Boot REST API
  |       |        |          |
 Auth   Sales   Expenses   Inventory
  |       |        |          |
  +-------+--------+----------+
                  |
              PostgreSQL
                  |
         Analytics / summaries
                  |
          AI assistant service
                  |
        Model integration (muse-spark-1.3)
```

Business data is scoped to the authenticated user in owner-aware services and repository queries. Administrators can view consolidated records; modification permissions should be verified separately.

## Run locally

### Prerequisites

- Java 21
- PostgreSQL
- Node.js and npm compatible with the frontend project
- Git

### Backend

1. Clone the repository:

   ```bash
   git clone https://github.com/IfeanyiOsuji/tradepilot-ai.git
   cd tradepilot-ai/tradepilot-api
   ```
2. Create a PostgreSQL database and configure the backend using your local environment or private application configuration. **Do not commit passwords, API keys, or JWT keystores.**
3. Set the environment variables required by the current backend configuration, including JWT keystore credentials and any AI-provider credentials.
4. Run on Windows:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

The API runs locally on `http://localhost:8080` with endpoints under `/api/v1` in the current development configuration.

### Frontend

Open another terminal:

```powershell
cd tradepilot-ai\tradepilot-frontend
npm install
npm run dev
```

Open the URL printed by Vite (often `http://localhost:5173`; it may select another port if that port is occupied).

The current frontend uses `http://localhost:8080/api/v1` as its API base URL. Update that setting when using another backend address.

## Example demo questions

- “Compare this week's revenue with last week's revenue.”
- “How is my business performing?”
- “Which products are selling best?”
- “Which inventory items need restocking?”

The responses depend on the records available to the signed-in user and the assistant capabilities configured in the running backend.

## Security notes

- Access tokens are configured to expire after **15 minutes**.
- The frontend has a **15-minute inactivity timeout** and logout functionality.
- With the current stateless JWT approach, browser logout clears local authentication state; it does not revoke an already-issued token on the server before its expiry.
- The local frontend currently uses `localStorage` for authentication state. Production deployment should evaluate secure cookies, token revocation, XSS defenses, and additional security testing.

## Demo

**3-minute demonstration:** _Add video URL after recording._

The demo walks through the dashboard, sales and inventory operations, and a natural-language financial comparison.

## Project status

Hackathon prototype. The application has been demonstrated running locally; final automated verification, provider/model identification, and release checks should be completed before submission.

## Author

[Ifeanyi Osuji](https://github.com/IfeanyiOsuji)
