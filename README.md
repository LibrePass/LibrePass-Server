# LibrePass Server

> [!WARNING]
> **Status: Unmaintained** — This project is no longer actively developed as of October 2024.

LibrePass Server is the cloud backend infrastructure for the LibrePass password manager, built with a focus on **security and reliability**. The server provides REST APIs, database services, and synchronization that enable secure password management across multiple devices. It is designed as a **single-instance, self-hosted** service intended to run behind a reverse proxy.

---

## ⚠️ Project Status

This project was a **hobby project** that served as a comprehensive learning experience. It taught me valuable lessons about:

- **Backend architecture** and modular monolith design with Spring Boot
- **Cryptography and security** implementation in backend systems (client-side encryption)
- **Database design** for sensitive, encrypted data
- **RESTful API design** and authentication mechanisms
- **Multi-module project structure** with shared libraries
- **Docker containerization** and deployment behind a reverse proxy
- **Cloud synchronization** with timestamp-based sync

While the project is no longer maintained, the codebase remains a reference implementation for secure backend password management.

---

## 🔐 Core Features

- ✅ **End-to-End Encryption** — Vaults are encrypted client-side with AES-256-GCM before transmission; server never sees plaintext (`shared/utils/EncryptedString.kt:10`)
- ✅ **Secure API Authentication** — Opaque bearer tokens (`lp_` + 32 random bytes, `server/database/TokenTable.kt:26`) stored in database, validated per request
- ✅ **Password Vault Management** — Create, read, update, and delete encrypted vaults (`/api/cipher/sync`)
- ✅ **Cross-Device Synchronization** — Timestamp-based sync (`lastSyncTimestamp` filter, `server/controllers/api/Cipher.kt:130`) with last-write-wins
- ✅ **User Account Management** — Registration, login, email verification, and account settings
- ✅ **Database Persistence** — Spring Data JPA with PostgreSQL (production) / H2 (dev)
- ✅ **Docker Support** — Multi-stage build (`Dockerfile:1-23`, `eclipse-temurin:21-jre`) + `docker-compose.yml` for local development
- ✅ **RESTful APIs** — Clean endpoints for `auth`, `cipher`, `collection`, `user`
- ✅ **TOTP Support** — Two-factor authentication via `dev.medzik:otp:1.0.1`
- ✅ **Partial Offline Sync** — Server accepts batched `updated` + `deleted` via `POST /api/cipher/sync`; no automatic conflict merge

---

## 🛠️ Technology Stack

### Framework & Platform
- **Spring Boot** — v3.3.2
- **Kotlin** — v2.0.0
- **Java** — JDK 21 (Eclipse Temurin). Parent `pom.xml:43` defines `java.version 1.8` for `shared/client`, overridden to 21 in `server/pom.xml:16`.

### Data & Security
- **Spring Data JPA** — PostgreSQL (`runtime`) + H2 `2.3.230` (dev)
- **Cryptography** — `dev.medzik:libcrypto:1.2.0` (AES-256-GCM `Aes.GCM`, Argon2id, X25519), `dev.medzik:hsauth:1.0.0` (HSAuth V1 handshake), `dev.medzik:otp:1.0.1` (TOTP)
- **Client-side key derivation** — Argon2id (`parallelism=3, memory=64MiB, iterations=4, salt=email, 32B`) → X25519 keypair → self-ECDH (`Cryptography.kt:21` `computeSharedSecret(private, publicFromPrivate)`) → AES-GCM key
- **GSON 2.11.0** — JSON serialization (Jackson excluded via `server/pom.xml:42`)
- **Validation** — `spring-boot-starter-validation` + `hibernate-validator 8.0.1`, basic `hexValidator` (`Validator.kt:13`) and length/email checks
- **Rate Limiting** — `bucket4j-core 8.10.1` (Auth 20 req/min, Cipher 200 req/min; `Collection` and some `User` endpoints not rate-limited — known limitation)

### Deployment
- **Docker** — Multi-stage build, `docker-compose.yml` (`librepass:8080` + `postgres`)
- **Reverse Proxy** — Designed to run behind **Caddy** (production), NGINX or Cloudflare terminating TLS; app listens on plain HTTP `:8080`, respects `HTTP_IP_HEADER` (`X-Forwarded-For` / `CF-Connecting-IP`, `application.properties:7`, `.env.schema:7`) for correct IP-based rate limiting and `CORS_ALLOWED_ORIGINS`
- **Maven** — Multi-module build, Java 21 Virtual Threads (`spring.threads.virtual.enabled=true` `application.properties:44`)

---

## 📋 Project Structure

The project is organized as a multi-module Maven structure:

```
LibrePass-Server/
├── server/          # Spring Boot REST API server
├── client/          # Client library for interacting with APIs
├── shared/          # Shared utilities and models (Cipher, EncryptedCipher, Cryptography)
├── docker-compose.yml
├── Dockerfile       # Multi-stage Docker build
└── pom.xml          # Parent POM configuration
```

### Modules
- **server** — Main Spring Boot application with REST endpoints and business logic
- **client** — Library for clients to interact with the LibrePass Server API
- **shared** — Shared code, models, and utilities used across modules

---

## 📚 Documentation

For detailed setup instructions, API documentation, and deployment guides, please refer to the [LibrePass Documentation](https://github.com/LibrePass/LibrePass-Docs/tree/main/docs) and `docs/self-hosting.md` (covers `.env` variables, `HTTP_IP_HEADER` for proxies, and `docker compose up -d`).

---

## 🔐 Security Considerations

This backend implements several security measures:

1. **End-to-End Encryption** — Vaults encrypted client-side with AES-256-GCM before transmission; server stores only `protectedData` hex strings
2. **Secure Authentication** — Opaque bearer tokens stored in DB (`TokenTable.kt:26`); no JWT. Server X25519 keypair generated at startup (`Auth.kt:44` `ServerPrivateKey`) for HSAuth V1 handshake (`X25519.computeSharedSecret`)
3. **HTTPS via Reverse Proxy** — TLS terminated at **Caddy** (production) / NGINX / Cloudflare in front of the app; app itself serves HTTP. Configure `HTTP_IP_HEADER=X-Forwarded-For` when behind proxy for accurate rate limiting.
4. **Password Hashing** — Client-side Argon2id (email as salt); server stores `argon2` parameters + X25519 `publicKey` (`UserTable.kt:23`), never sees password/hash
5. **No Plaintext Storage** — Only encrypted `protectedData` stored; `ddl-auto=update` (`application.properties:21`), no Flyway/Liquibase migrations — known limitation
6. **Input Validation** — Basic validation via `Validator.hexValidator` + Spring Validation (`@Email`, `@Max`, length checks); not exhaustive sanitization
7. **Rate Limiting** — Bucket4j on `Auth` and `Cipher` controllers; `Collection` and some `User` endpoints (e.g. `changeEmail`, `changePassword`, `verifyNewEmail` for external calls `User.kt:110`) are not/partially limited — opportunity for hardening

> **Note:** This is a hobby project. For production use, consider professional security audits, proper DB migrations (Flyway), indexes (`@Index`), and full rate-limit coverage. The codebase intentionally shows trade-offs of a single-instance hobby deployment.

---

## 📄 License

This project is licensed under the **GNU Affero General Public License v3.0** (AGPL-3.0)

---

## 🔗 Related Projects

- **[LibrePass Android](https://github.com/LibrePass/LibrePass-Android)** — Mobile client for Android

---

## 💡 What I Learned

This hobby project taught me invaluable lessons about:

### Backend Development
- ✅ Building Spring Boot applications with modular monolith and multi-module architecture
- ✅ RESTful API design principles and error handling (`ServerException.kt:32`)
- ✅ Spring Data JPA for database abstraction
- ✅ Dependency injection and inversion of control patterns
- ✅ Exception handling and standardized error responses

### Cryptography & Security
- ✅ End-to-end encryption with client-side key derivation (Argon2id → X25519 → AES-GCM)
- ✅ Token generation and database-based validation (opaque tokens, no JWT)
- ✅ User authentication via X25519 + HSAuth V1
- ✅ Designing for TLS termination at reverse proxy (Caddy) and proxy-aware rate limiting
- ✅ Protecting sensitive user data at rest and in transit
- ✅ Rate limiting with Bucket4j and its limits in a single-instance, in-memory setup

### Database & Persistence
- ✅ Relational database design for encrypted data
- ✅ JPA entity relationships and constraints
- ✅ Handling concurrent access (Virtual Threads + Coroutines) — and the limitation of `ddl-auto=update` without migrations

### DevOps & Deployment
- ✅ Multi-stage Docker builds for optimized images
- ✅ Docker Compose for local development and testing
- ✅ Environment configuration and secrets management via `spring-dotenv` and `.env.schema`
- ✅ Deploying behind Caddy as reverse proxy with `X-Forwarded-For` handling

### Architecture & Design Patterns
- ✅ Modular monolith and separation of concerns (vs. microservices)
- ✅ Timestamp-based sync (`lastSyncTimestamp` / `lastServerSync`) with last-write-wins and its conflict-resolution limits
- ✅ Separation of concerns and SOLID principles
- ✅ Testing strategies for backend services (`shared/src/test` exists, `server` tests are skipped `server/pom.xml:133`)

This project was a comprehensive learning experience that combined practical backend development with real-world security challenges and the trade-offs of self-hosting a password manager.

---

**Built with ❤️ as a learning experience in backend development and security**
