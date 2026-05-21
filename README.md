# SZP — System Zarządzania Projektami

> **Webowy system do zarządzania projektami** — praca inżynierska.  
> Backend REST API zbudowany w oparciu o **Java 17 + Spring Boot**, z uwierzytelnianiem JWT i bazą danych PostgreSQL.

---

## 📋 Spis treści

- [O projekcie](#o-projekcie)
- [Technologie](#technologie)
- [Architektura](#architektura)
- [Endpointy API](#endpointy-api)
- [Uruchomienie lokalne](#uruchomienie-lokalne)
- [Uruchomienie przez Docker](#uruchomienie-przez-docker)
- [Zmienne środowiskowe](#zmienne-środowiskowe)
- [Struktura projektu](#struktura-projektu)
- [Status projektu](#status-projektu)

---

## O projekcie

SZP to system do zarządzania projektami i zadaniami oparty na metodologii **Kanban**. Umożliwia tworzenie projektów, zarządzanie zespołami i śledzenie postępów zadań poprzez interaktywną tablicę Kanban.

**Główne funkcjonalności:**
- 🔐 Rejestracja i logowanie użytkowników z tokenami JWT
- 📁 Tworzenie i zarządzanie projektami
- 👥 Dodawanie członków do projektów
- ✅ Zarządzanie zadaniami z tablicą Kanban (TODO / IN_PROGRESS / DONE)
- 🔒 Autoryzacja oparta na rolach (ROLE_USER, ROLE_ADMIN)

---

## Technologie

### Backend
| Technologia | Wersja | Zastosowanie |
|-------------|--------|--------------|
| Java | 17 | Język programowania |
| Spring Boot | 4.0.6 | Framework aplikacji |
| Spring Security | 4.0.6 | Autoryzacja i uwierzytelnianie |
| Spring Data JPA | — | Warstwa dostępu do danych |
| Hibernate | — | ORM (mapowanie encji) |
| JJWT | 0.11.5 | Generowanie i weryfikacja tokenów JWT |
| PostgreSQL | 42.7.11 | Relacyjna baza danych |
| Lombok | — | Redukcja boilerplate kodu |
| Maven | — | Zarządzanie zależnościami i budowanie |

### Infrastructure
- **Docker** + **Docker Compose** — konteneryzacja aplikacji i bazy danych

---

## Architektura

```
┌─────────────────────┐          ┌──────────────────────────────────┐
│   Frontend          │          │   Backend (Spring Boot)          │
│   React + TS        │◄────────►│   REST API  :8080                │
│   :5173             │  HTTP/JSON│                                  │
└─────────────────────┘          │  ┌──────────────────────────┐    │
                                 │  │   Spring Security + JWT  │    │
                                 │  └──────────────────────────┘    │
                                 │  ┌──────────────────────────┐    │
                                 │  │   JPA / Hibernate        │    │
                                 │  └──────────────────────────┘    │
                                 └───────────────┬──────────────────┘
                                                 │
                                 ┌───────────────▼──────────────────┐
                                 │   PostgreSQL  :5432              │
                                 │   users, roles, projects, tasks  │
                                 └──────────────────────────────────┘
```

### Schemat bazy danych

```
USERS ──────────── USER_ROLES ──────── ROLES
  │                                     
  │ (owner)
  ▼
PROJECTS ─── PROJECT_MEMBERS ──── USERS (members)
  │
  ▼
TASKS
```

---

## Endpointy API

Wszystkie endpointy poza `/api/auth/**` wymagają nagłówka:
```
Authorization: Bearer <token>
```

### 🔐 Autoryzacja — `/api/auth`

| Metoda | Endpoint | Opis | Dostęp |
|--------|----------|------|--------|
| `POST` | `/api/auth/register` | Rejestracja nowego użytkownika | Publiczny |
| `POST` | `/api/auth/login` | Logowanie, zwraca token JWT | Publiczny |

**Przykład rejestracji:**
```json
POST /api/auth/register
{
  "email": "jan@example.com",
  "password": "SecretPass123!",
  "name": "Jan",
  "surname": "Kowalski"
}
```

**Przykład logowania (odpowiedź):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "jan@example.com"
}
```

### 📁 Projekty — `/api/projects`

| Metoda | Endpoint | Opis |
|--------|----------|------|
| `GET` | `/api/projects` | Lista projektów zalogowanego użytkownika |
| `POST` | `/api/projects` | Tworzenie nowego projektu |
| `PATCH` | `/api/projects/edit/{id}` | Edycja projektu |
| `POST` | `/api/projects/{id}/members` | Dodanie członka (po e-mail) |

**Przykład tworzenia projektu:**
```json
POST /api/projects
{
  "title": "Nowy Projekt",
  "description": "Opis projektu"
}
```

### ✅ Zadania — `/api/projects/{projectId}/tasks` *(w trakcie implementacji)*

| Metoda | Endpoint | Opis |
|--------|----------|------|
| `GET` | `/api/projects/{id}/tasks` | Lista zadań projektu (dane do Kanban) |
| `POST` | `/api/projects/{id}/tasks` | Tworzenie zadania (domyślny status: `TODO`) |
| `PATCH` | `/api/tasks/{taskId}/status` | Zmiana statusu (Drag & Drop) |
| `PUT` | `/api/tasks/{taskId}` | Pełna edycja zadania |
| `DELETE` | `/api/tasks/{taskId}` | Usunięcie zadania |

**Statusy zadań:** `TODO` → `IN_PROGRESS` → `DONE`  
**Priorytety:** `LOW` / `MEDIUM` / `HIGH`

---

## Uruchomienie lokalne

### Wymagania
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### Kroki

**1. Sklonuj repozytorium:**
```bash
git clone https://github.com/<twoj-login>/szp.git
cd szp
```

**2. Utwórz bazę danych PostgreSQL:**
```sql
CREATE DATABASE "szp-db";
```

**3. Skonfiguruj `application.properties`:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/szp-db
spring.datasource.username=postgres
spring.datasource.password=twoje_haslo

security.jwt.secret-key=twoj_bardzo_dlugi_sekret_base64_minimum_32_znaki
security.jwt.expiration-time=3600000

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**4. Uruchom aplikację:**
```bash
./mvnw spring-boot:run
```

API będzie dostępne pod adresem: `http://localhost:8080`

---

## Uruchomienie przez Docker

### Wymagania
- Docker
- Docker Compose

### Kroki

**1. Zbuduj obraz:**
```bash
cd docker
docker build -t szp-backend .
```

**2. Uruchom przez Docker Compose:**
```bash
docker compose up -d
```

Docker Compose uruchomi:
- **szp-backend** — aplikacja Spring Boot na porcie `8080`
- **db** — PostgreSQL na porcie `5432`

**3. Zatrzymaj kontenery:**
```bash
docker compose down
```

---

## Zmienne środowiskowe

| Zmienna | Opis | Domyślna wartość |
|---------|------|-----------------|
| `DB_URL` | URL bazy danych | `jdbc:postgresql://db:5432/postgres` |
| `DB_USER` | Użytkownik bazy danych | `postgres` |
| `DB_PASSWORD` | Hasło bazy danych | — |
| `JWT_SECRET` | Klucz do podpisywania tokenów JWT | — |
| `JWT_EXPIRATION` | Czas ważności tokenu (ms) | `3600000` (1h) |

> ⚠️ **Nie commituj haseł i sekretów do repozytorium!** Użyj zmiennych środowiskowych lub pliku `.env` (dodanego do `.gitignore`).

---

## Struktura projektu

```
szp/
├── docker/
│   ├── compose.yml          # Docker Compose (app + db)
│   └── dockerfile           # Obraz aplikacji (eclipse-temurin:17)
├── src/
│   └── main/
│       ├── java/com/stg/szp/
│       │   ├── config/
│       │   │   ├── SecurityConfig.java      # Konfiguracja Spring Security
│       │   │   ├── JwtAuthFilter.java       # Filtr JWT
│       │   │   ├── JwtService.java          # Generowanie/weryfikacja tokenów
│       │   │   └── DataInitializer.java     # Inicjalizacja ról przy starcie
│       │   ├── controllers/
│       │   │   ├── AuthController.java      # Rejestracja, logowanie
│       │   │   └── ProjectController.java   # CRUD projektów
│       │   ├── services/
│       │   │   ├── AuthService.java         # Logika autoryzacji
│       │   │   └── ProjectService.java      # Logika projektów
│       │   ├── models/
│       │   │   ├── SZP_User.java            # Encja użytkownika
│       │   │   ├── Role.java                # Encja roli
│       │   │   ├── Project.java             # Encja projektu
│       │   │   ├── Task.java                # Encja zadania
│       │   │   ├── TaskStatus.java          # Enum: TODO/IN_PROGRESS/DONE
│       │   │   └── TaskPriority.java        # Enum: LOW/MEDIUM/HIGH
│       │   ├── repos/
│       │   │   ├── UserRepository.java
│       │   │   ├── RoleRepository.java
│       │   │   └── ProjectRepository.java
│       │   └── DTO/
│       │       ├── RegisterUserDTO.java
│       │       ├── LoginDTO.java
│       │       ├── UserResponseDTO.java
│       │       ├── ProjectResponseDTO.java
│       │       ├── CreateProjectDTO.java
│       │       └── EditProjectDTO.java
│       └── resources/
│           └── application.properties
└── pom.xml
```

---

## Status projektu

| Moduł | Status |
|-------|--------|
| Konfiguracja projektu (Spring Boot, Security, JWT) | ✅ Gotowe |
| Encje JPA (User, Role, Project, Task) | ✅ Gotowe |
| Inicjalizacja ról (DataInitializer) | ✅ Gotowe |
| Autoryzacja JWT (rejestracja + logowanie) | ✅ Gotowe |
| Filtr JWT (JwtAuthFilter) | ✅ Gotowe |
| Moduł projektów (CRUD) | ✅ Gotowe |
| Zarządzanie członkami projektu | 🔄 W trakcie |
| Moduł zadań (Kanban) | ⬜ Planowane |
| Frontend (React + TypeScript) | ⬜ Planowane |
| Testy jednostkowe i integracyjne | ⬜ Planowane |

---

## Licencja

Projekt stworzony na potrzeby pracy inżynierskiej.  
**Akademia Nauk Stosowanych w Lesznie** — Informatyka, 2026.
