# Task Manager

REST API для управления задачами с ролевой моделью доступа, JWT-аутентификацией
и назначением исполнителей. Написан на Spring Boot 3, работает в Docker.

## 📋 Содержание

- [Возможности](#-возможности)
- [Технологии](#-технологии)
- [Архитектура](#-архитектура)
- [Быстрый старт](#-быстрый-старт)
- [Конфигурация](#-конфигурация)
- [API](#-api)
- [Разработка](#-разработка)
- [Тестирование](#-тестирование)
- [Структура проекта](#-структура-проекта)

## ✨ Возможности

### Пользователи
- Регистрация и аутентификация через JWT
- Просмотр и редактирование профиля
- Смена пароля
- Мягкое удаление и восстановление пользователей
- Управление ролями (`USER`, `ADMIN`, `SUPER_ADMIN`)

### Задачи
- Создание задач с заголовком, описанием, дедлайном и приоритетом
- Назначение одного или нескольких исполнителей
- Разграничение прав: обычный пользователь создаёт задачи на себя,
  `ADMIN`/`SUPER_ADMIN` — на любых пользователей
- Обновление, удаление (мягкое) и восстановление задач
- Фильтрация и пагинация

### Безопасность
- JWT-токены с настраиваемым временем жизни
- Разграничение доступа по ролям через `@PreAuthorize`
- Валидация входных данных (`@Valid`, Bean Validation)
- Централизованная обработка исключений
- Аудит создания/изменения сущностей (`@CreatedBy`, `@LastModifiedBy`)

## 🛠 Технологии

| Слой | Технология |
|---|---|
| Язык | Java 21 |
| Фреймворк | Spring Boot 3.3.5 |
| Безопасность | Spring Security + JWT (jjwt) |
| Данные | Spring Data JPA, Hibernate 6.5 |
| БД | PostgreSQL 18 |
| Миграции | Hibernate DDL (`ddl-auto`) |
| Сборка | Maven |
| Контейнеризация | Docker, Docker Compose |
| Тесты | JUnit 5, Mockito, Spring Boot Test |
| Утилиты | Lombok, MapStruct (опционально) |

## 🏗 Архитектура

Приложение построено по слоистой архитектуре:

```
Controller  →  Service  →  Repository  →  Database
    ↑             ↑
   DTO         Entity
```

- **Controller** — принимает HTTP-запросы, валидирует DTO, возвращает ответы.
- **Service** — бизнес-логика, транзакции, проверка прав.
- **Repository** — доступ к данным через Spring Data JPA.
- **DTO** — контракты API, отделены от сущностей.
- **Entity** — маппинг на таблицы БД.
- **Exception Handler** — централизованная обработка ошибок (`@RestControllerAdvice`).

## 🚀 Быстрый старт

### Требования

- Docker 24+
- Docker Compose v2+
- (Опционально) Java 21 и Maven 3.9+ — для локальной разработки

### Запуск

1. **Клонируйте репозиторий:**

   ```bash
   git clone https://github.com/your-username/task-manager.git
   cd task-manager
   ```

2. **Создайте `.env` из шаблона:**

   ```bash
   cp .env.example .env
   ```

3. **Отредактируйте `.env`** — сгенерируйте секрет:

   ```bash
   openssl rand -base64 32
   ```

   Вставьте результат в `JWT_SECRET`.

4. **Запустите:**

   ```bash
   ./run.sh
   ```

   Или вручную:

   ```bash
   docker compose up --build
   ```

5. **Проверьте:**

   ```
   http://localhost:8080/api/actuator/health
   ```

### Остановка

```bash
./stop.sh
```

Или:

```bash
docker compose down          # остановить, данные БД сохранить
docker compose down -v       # остановить + удалить данные БД
```

## ⚙️ Конфигурация

### Переменные окружения

| Переменная | Описание | Пример |
|---|---|---|
| `JWT_SECRET` | Секрет для подписи JWT (мин. 32 символа) | `openssl rand -base64 32` |
| `JWT_EXPIRATION` | Время жизни токена в мс | `86400000` (24 часа) |
| `SPRING_DATASOURCE_URL` | JDBC URL БД | `jdbc:postgresql://db:5432/taskmanager` |
| `SPRING_DATASOURCE_USERNAME` | Пользователь БД | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД | `postgres` |

### Файлы конфигурации

- `application.yml` — базовые настройки
- `.env` — секреты для Docker Compose (не коммитится)
- `.env.example` — шаблон (коммитится)

## 🌐 API

Базовый путь: `/api`

### Аутентификация

| Метод | Endpoint | Описание |
|---|---|---|
| `POST` | `/auth/register` | Регистрация |
| `POST` | `/auth/login` | Логин, возвращает JWT |
| `POST` | `/auth/refresh` | Обновление токена |

### Пользователи

| Метод | Endpoint | Доступ | Описание |
|---|---|---|---|
| `GET` | `/users/me` | USER | Текущий пользователь |
| `PATCH` | `/users/me` | USER | Обновить профиль |
| `PATCH` | `/users/me/password` | USER | Сменить пароль |
| `GET` | `/users` | ADMIN | Список пользователей |
| `PATCH` | `/users/{id}/role` | ADMIN | Изменить роль |
| `DELETE` | `/users/{id}` | ADMIN | Мягко удалить |
| `POST` | `/users/{id}/restore` | SUPER_ADMIN | Восстановить |

### Задачи

| Метод | Endpoint | Доступ | Описание |
|---|---|---|---|
| `POST` | `/tasks` | USER | Создать задачу на себя |
| `POST` | `/tasks/admin` | ADMIN | Создать с исполнителями |
| `GET` | `/tasks` | USER | Список задач (с фильтрами) |
| `GET` | `/tasks/{id}` | USER | Задача по ID |
| `PATCH` | `/tasks/{id}` | USER/ADMIN | Обновить |
| `DELETE` | `/tasks/{id}` | USER/ADMIN | Мягко удалить |
| `POST` | `/tasks/{id}/assignees` | ADMIN | Назначить исполнителей |
| `DELETE` | `/tasks/{id}/assignees/{userId}` | ADMIN | Снять исполнителя |

### Примеры

**Регистрация:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "secret123"
  }'
```

**Логин:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "secret123"
  }'
```

Ответ:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400000
}
```

**Создание задачи:**

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Написать README",
    "description": "Подробное описание проекта",
    "deadline": "2026-12-31T23:59:59"
  }'
```

**Список задач с пагинацией:**

```bash
curl "http://localhost:8080/api/tasks?page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer <token>"
```

### Формат ошибок

```json
{
  "timestamp": "2026-09-19T08:45:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Поле title обязательно",
  "path": "/api/tasks"
}
```

| Код | Когда |
|---|---|
| `400` | Ошибка валидации |
| `401` | Не аутентифицирован |
| `403` | Нет прав |
| `404` | Ресурс не найден |
| `409` | Конфликт (дубликат) |
| `500` | Внутренняя ошибка |

## 💻 Разработка

### Локальный запуск (без Docker)

1. **Запустите PostgreSQL** локально (или через Docker):

   ```bash
   docker run -d --name postgres-dev \
     -e POSTGRES_DB=taskmanager \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:18-alpine
   ```

2. **Задайте переменные окружения** в IDE:

   ```
   JWT_SECRET=<ваш-секрет>
   JWT_EXPIRATION=86400000
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/taskmanager
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=postgres
   ```

3. **Запустите:**

   ```bash
   ./mvnw spring-boot:run
   ```

### Полезные команды

```bash
# Сборка JAR
./mvnw clean package

# Сборка без тестов
./mvnw clean package -DskipTests

# Проверка зависимостей на уязвимости
./mvnw dependency-check:check

# Форматирование кода
./mvnw spotless:apply

# Логи приложения в Docker
docker compose logs -f app

# Зайти в контейнер с БД
docker compose exec db psql -U postgres -d taskmanager
```

### Профили

| Профиль | Назначение |
|---|---|
| `default` | Локальная разработка |
| `docker` | Запуск в контейнере |
| `test` | Тесты |

## 🧪 Тестирование

```bash
# Все тесты
./mvnw test

# Конкретный класс
./mvnw test -Dtest=TaskServiceTest

```

Покрытие: **52 теста**, все зелёные.

## 📁 Структура проекта

```
task-manager/
src/
├── main
│     ├── java
│     │ └── com
│     │     └── javarush
│     │         ├── config
│     │         │     ├── ApplicationAuditorAware.java
│     │         │     ├── FilterConfig.java
│     │         │     ├── JpaConfig.java
│     │         │     ├── MdcFilter.java
│     │         │     ├── OpenApiConfig.java
│     │         │     └── SecurityConfig.java
│     │         ├── controller
│     │         │    ├── AuthController.java
│     │         │    ├── TaskController.java
│     │         │    └── UserController.java
│     │         ├── dto
│     │         │     ├── error
│     │         │     │     └── ErrorResponse.java
│     │         │     ├── task
│     │         │     │     ├── AssigneesRequest.java
│     │         │     │     ├── TaskCreateRequest.java
│     │         │     │     ├── TaskListDto.java
│     │         │     │     ├── TaskResponseDto.java
│     │         │     │     ├── UpdateDeadlineRequest.java
│     │         │     │     ├── UpdateDescriptionRequest.java
│     │         │     │     ├── UpdatePriorityRequest.java
│     │         │     │     ├── UpdateStatusRequest.java
│     │         │     │     └── UpdateTitleRequest.java
│     │         │     └── user
│     │         │       ├── ChangePasswordDto.java
│     │         │       ├── ChangeRoleDto.java
│     │         │       ├── JwtResponse.java
│     │         │       ├── UserAuthenticatedDto.java
│     │         │       ├── UserRegistrationDto.java
│     │         │       ├── UserResponseDto.java
│     │         │       ├── UserShortDto.java
│     │         │       └── UserUpdateDto.java
│     │         ├── exception
│     │         │       ├── GlobalExceptionHandler.java
│     │         │       ├── InvalidOperationException.java
│     │         │       ├── InvalidPasswordException.java
│     │         │       ├── TaskAlreadyDeleteException.java
│     │         │       ├── TaskAlreadyExistsException.java
│     │         │       ├── TaskNotDeletedException.java
│     │         │       ├── TaskNotFoundException.java
│     │         │       ├── UnauthorizedException.java
│     │         │       ├── UserAlreadyAssignedException.java
│     │         │       ├── UserAlreadyDeleteException.java
│     │         │       ├── UserAlreadyExistsException.java
│     │         │       ├── UserNotAssignedException.java
│     │         │       ├── UserNotDeletedException.java
│     │         │       └── UserNotFoundException.java
│     │         ├── model
│     │         │       ├── entity
│     │         │       │        ├── enums
│     │         │       │        │        ├── Role.java
│     │         │       │        │        ├── TaskPriority.java
│     │         │       │        │        └── TaskStatus.java
│     │         │       │        ├── Task.java
│     │         │       │        └── User.java
│     │         │       └── repository
│     │         │                ├── TaskRepository.java
│     │         │                └── UserRepository.java
│     │         ├── security
│     │         │       ├── JwtAuthenticationEntryPoint.java
│     │         │       ├── JwtAuthenticationFilter.java
│     │         │       ├── JwtTokenProvider.java
│     │         │       └── UserPrincipal.java
│     │         ├── service
│     │         │       ├── CustomUserDetailsService.java
│     │         │       ├── TaskService.java
│     │         │       └── UserService.java
│     │         └── TaskManagerApp.java
│     └── resources
│           ├── application-test.yaml
│           ├── application.yaml
│           └── schema.sql
└── test
    └── java
        └── com
            └── javarush
                ├── controller
                │       ├── AuthControllerIT.java
                │       ├── TaskControllerIT.java
                │       └── UserControllerIT.java
                ├── exception
                │       └── GlobalExceptionHandlerTest.java
                └── service
                    ├── TaskServiceTest.java
                    └── UserServiceTest.java
├── Dockerfile                       # Multi-stage сборка
├── docker-compose.yml               # db + app
├── .env.example                     # Шаблон переменных
├── .dockerignore
├── .gitignore
├── run.sh                           # Запуск
├── stop.sh                          # Остановка
├── pom.xml
└── README.md
```

## 🔒 Безопасность

- Пароли хешируются **BCrypt**
- JWT подписывается **HS256**, секрет минимум 32 байта
- Секреты **не коммитятся** — только через `.env`
- Секреты **не попадают в Docker-образ** — через `.dockerignore`
- Валидация всех входных данных
- Разграничение доступа по ролям

## 📝 Лицензия

Учебный проект. Используйте свободно.

## 👤 Автор

**Ваше Имя**

- GitHub: [@your-username](https://github.com/your-username)
- Email: your.email@example.com

---

Сделано с ❤️ на Java и Spring Boot