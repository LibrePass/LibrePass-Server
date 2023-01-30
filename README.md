# VaultBox Core Infrastructure

The VaultBox Server project contains a set of APIs, database, and other services
needed for the backend of the VaultBox project.

The server is written in Kotlin using Spring Boot framework and Spring Boot JPA for DataBase integration.

## Setup Guide

First. You will to generate an RSA key pair for Json Web Token authentication:
```bash
# Generate private key
openssl genrsa -out privateKey.pem 2048
# Generate a public key from the private key
openssl rsa -in privateKey.pem -outform PEM -pubout -out publicKey.pem
```

Next. You will set up environment variables:
- Copy the example file `.env.example` to `.env`.
- If you want to use a different database server you need to configure `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_USER`, `POSTGRES_PASSWORD` and `POSTGRES_DB`.

## Building Guide

If you want to build the server you need to use the following commands:
```bash
# Clone the repository
git clone https://github.com/VaultBox/server.git
cd server
# Build using maven
./mvnw clean package
# Now you can run the server
java -jar ./server/target/server-0.0.1-SNAPSHOT.jar
```
