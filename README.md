# LibrePass Server

> [!WARNING]
> **Status: Unmaintained** — This project is no longer actively developed as of October 2024.

LibrePass Server is the cloud backend infrastructure for the LibrePass password manager, built with a focus on **security, scalability, and reliability**. The server provides APIs, database services, and synchronization capabilities that enable secure password management across multiple devices.

---

## ⚠️ Project Status

This project was a **hobby project** that served as an incredible learning experience. It taught me valuable lessons about:

- **Backend architecture** and microservices patterns with Spring Boot
- **Cryptography and security** implementation in backend systems
- **Database design** and optimization for sensitive data storage
- **RESTful API design** and authentication mechanisms
- **Multi-module project structure** with shared libraries
- **Docker containerization** and deployment strategies
- **Cloud synchronization** protocols and conflict resolution

While the project is no longer maintained, the codebase remains a testament to these learnings and serves as a reference implementation for secure backend password management systems.

---

## 🔐 Core Features

- ✅ **End-to-End Encryption** — All passwords encrypted on client-side before transmission
- ✅ **Secure API Authentication** — Token-based authentication with database storage
- ✅ **Password Vault Management** — Create, read, update, and delete encrypted vaults
- ✅ **Cross-Device Synchronization** — Sync encrypted data across multiple devices
- ✅ **User Account Management** — Registration, login, and account settings
- ✅ **Database Persistence** — Reliable storage with Spring Data JPA
- ✅ **Docker Support** — Ready-to-deploy containerized application
- ✅ **RESTful APIs** — Clean, documented API endpoints for client applications
- ✅ **TOTP Support** — Two-factor authentication via Time-based One-Time Passwords
- ✅ **Offline Sync** — Handle client offline changes with server reconciliation

---

## 🛠️ Technology Stack

### Framework & Platform
- **Spring Boot** — Modern Java/Kotlin web framework (v3.3.2)
- **Kotlin** — Primary language for type-safe development (v2.0.0)
- **Java** — JDK 21 (via Eclipse Temurin)

### Data & Security
- **Spring Data JPA** — Database abstraction and ORM
- **Cryptography** — Custom libcrypto library for encryption/decryption
- **GSON** — JSON serialization and deserialization

### Deployment
- **Docker** — Multi-stage builds for optimized container images
- **Maven** — Project build and dependency management

---

## 📋 Project Structure

The project is organized as a multi-module Maven structure:

```
LibrePass-Server/
├── server/          # Spring Boot REST API server
├── client/          # Client library for interacting with APIs
├── shared/          # Shared utilities and models
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

For detailed setup instructions, API documentation, and deployment guides, please refer to the [LibrePass Documentation](https://github.com/LibrePass/LibrePass-Docs/tree/main/docs).

---

## 🔐 Security Considerations

This backend implements several security measures:

1. **End-to-End Encryption** — Passwords encrypted before transmission to server
2. **Secure Authentication** — Database-stored tokens for request validation
3. **HTTPS Communication** — All API traffic encrypted in transit
4. **Password Hashing** — User credentials hashed using industry-standard algorithms
5. **No Plaintext Storage** — Passwords never stored unencrypted in database
6. **Input Validation** — All API inputs validated and sanitized
7. **Rate Limiting** — Protection against brute force and DOS attacks

> **Note:** This is a hobby project. For production use, consider security audits by professional security experts and follow established security best practices for sensitive data systems.

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
- ✅ Building scalable Spring Boot applications with multi-module architecture
- ✅ RESTful API design principles and best practices
- ✅ Spring Data JPA for database abstraction and ORM
- ✅ Dependency injection and inversion of control patterns
- ✅ Exception handling and error response standardization
- ✅ API versioning and backward compatibility

### Cryptography & Security
- ✅ Implementing end-to-end encryption in backend systems
- ✅ Secure password handling and hashing strategies
- ✅ Token generation and database-based validation
- ✅ User authentication and authorization flows
- ✅ HTTPS/TLS communication security
- ✅ Protecting sensitive user data at rest and in transit
- ✅ Rate limiting and DOS protection mechanisms

### Database & Persistence
- ✅ Relational database design for encrypted data
- ✅ JPA entity relationships and constraints
- ✅ Database migration strategies
- ✅ Indexing and query optimization
- ✅ Handling concurrent access and transactions

### DevOps & Deployment
- ✅ Multi-stage Docker builds for optimized images
- ✅ Docker Compose for local development and testing
- ✅ Environment configuration and secrets management
- ✅ Containerized application deployment patterns

### Architecture & Design Patterns
- ✅ Microservices and modular architecture
- ✅ Cloud synchronization protocols
- ✅ Data consistency and conflict resolution
- ✅ Separation of concerns and SOLID principles
- ✅ Testing strategies for backend services

This project was a comprehensive learning experience that combined practical backend development with real-world security challenges. It provided insights into how secure cloud services work and the complexity involved in protecting sensitive user credentials.

---

**Built with ❤️ as a learning experience in backend development and security**
