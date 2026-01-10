# Структура модулей API service

Все “модули” API service находятся внутри одного Gradle‑модуля: `server/rent-control-server` и оформляются как **пакеты/фичи** (feature modules) внутри `src/main/kotlin`.

Цель: единый шаблон для всех доменов (auth/objects/inspections/…), чтобы код было легко:
- расширять без “больших файлов”
- тестировать по уровням (unit → api → integration)
- сопровождать без циклических зависимостей

---

## 1) Пакеты верхнего уровня (`server/rent-control-server`)

Рекомендуемая структура:

```
server/rent-control-server/
  src/main/kotlin/org/igorv8836/rentcontrol/server/
    app/                 # bootstrap, routing, ktor plugins, module registration
    foundation/          # общие вещи: errors, auth context, db, pagination, config, time
    modules/             # feature modules (доменные модули)
      auth/
      me/
      users/
      ref/
      objects/
      tenants/
      inspections/
      defects/
      expenses/
      meters/
      attachments/
      notifications/
      sync/
      audit/
    integrations/        # внешние интеграции (реализации портов): otp/mock, storage/local|cdn, push/fcm
  src/test/kotlin/...    # тесты зеркалят пакеты
  src/main/resources/
    application.conf
    db/migration/        # миграции (если используем Flyway/Liquibase)
```

### Роли пакетов

- `app/`
  - поднимает Ktor, подключает плагины, собирает зависимости (manual DI), регистрирует роуты модулей.
- `foundation/`
  - общие типы и инфраструктура, которые нужны всем модулям (ошибки, транзакции, контекст пользователя, пагинация, idempotency, время/clock).
- `modules/<name>/`
  - доменная логика и HTTP слой конкретной фичи.
- `integrations/`
  - конкретные реализации внешних зависимостей (сейчас: mock OTP, позже SMTP; сейчас: storage local/CDN; push: FCM).

---

## 2) Список feature modules (что в каком модуле)

Модуль = зона ответственности + набор эндпоинтов из `docs/server-api.md`.

- `modules/auth` — `/auth/*` (регистрация/OTP/логин/refresh/logout/reset).
- `modules/me` — `/me`, `/me/sessions/logout-all` (профиль/настройки, включая `sync.*` настройки).
- `modules/users` — `/users/*` (админка, блок/разблок, список для назначений).
- `modules/ref` — `/ref/*` (справочники, шаблоны чек‑листов).
- `modules/objects` — `/objects/*` (список/карточка/архив) + агрегаты.
- `modules/tenants` — `/tenants/*` + link/unlink tenant ↔ object.
- `modules/inspections` — `/inspections/*` + чек‑лист.
- `modules/defects` — `/defects/*` + комментарии.
- `modules/expenses` — `/expenses/*` + подтверждения.
- `modules/meters` — `/objects/{objectId}/meters/*`.
- `modules/attachments` — `/attachments/*` (upload/metadata/delete), proxy upload через BDUI-server.
- `modules/notifications` — `/notifications/*`, `/push/token` (FCM).
- `modules/sync` — `/sync/*` (batch/since, конфликты, auto‑LWW/ server‑wins).
- `modules/audit` — `/audit/events` + `/…/audit/events`.

---

## 3) Шаблон структуры внутри модуля

Один модуль (фича) держим “вертикальным слайсом”:

```
modules/<feature>/
  api/                   # Ktor routes + request/response DTO + validation
    <Feature>Routes.kt
    dto/
      ...
  domain/                # use-cases, бизнес-правила, статусы, политики
    model/
      ...
    service/
      <Feature>Service.kt
    policy/
      ...
    port/                # интерфейсы внешних зависимостей (storage/email/push/clock)
      ...
  data/                  # репозитории, DB маппинг, Exposed queries
    repo/
      <Feature>Repository.kt
      Exposed<Feature>Repository.kt
    db/
      <Feature>Tables.kt (или отдельные файлы таблиц)
  module/                # сборка зависимостей/регистрация роутов модуля
    <Feature>Module.kt    # содержит функцию вида `fun Route.<feature>Module(...)`
```

Правило зависимостей внутри фичи:
- `api` зависит от `domain` (вызывает use-cases).
- `domain` зависит от `data` только через интерфейсы репозиториев/портов.
- `data` зависит от `foundation/db` и Exposed.
- реализации портов могут жить в `integrations/*` и инжектиться в `module`.

---

## 4) Пример: `modules/auth` (структура одного модуля)

```
modules/auth/
  api/
    AuthRoutes.kt
    dto/
      RegisterRequest.kt
      ConfirmRequest.kt
      LoginRequest.kt
      RefreshRequest.kt
      TokenPairResponse.kt
  domain/
    model/
      UserId.kt
      SessionId.kt
      TokenPair.kt
      OtpPurpose.kt
    policy/
      PasswordPolicy.kt
      AuthRateLimitPolicy.kt
    port/
      OtpSender.kt              # интерфейс (реализации: mock сейчас, smtp позже)
      PasswordHasher.kt         # интерфейс (bcrypt/argon2 реализация)
      TokenIssuer.kt            # интерфейс (JWT)
      Clock.kt                  # обычно в foundation/time
    service/
      AuthService.kt
  data/
    db/
      AuthTables.kt             # otp_codes, refresh_sessions (users table обычно в users/me модуле)
    repo/
      AuthRepository.kt
      ExposedAuthRepository.kt
  module/
    AuthModule.kt               # создаёт AuthService и регистрирует `AuthRoutes` (функция `authModule`)
```

Что важно для `auth`:
- OTP sender — порт (`domain/port/OtpSender.kt`), а mock реализация лежит в `integrations/email/MockOtpSender.kt`.
- refresh‑токен хранится на клиенте и проксируется через BDUI-server, поэтому `/auth/refresh` остаётся обычным HTTP эндпоинтом API service.

---

## 5) Где держать “общие вещи”

Чтобы не размазывать утилиты по фичам:

- `foundation/http` — Ktor плагины (ContentNegotiation, StatusPages, CallId/traceId).
- `foundation/errors` — единый формат ошибок + маппинг исключений.
- `foundation/security` — общая модель `UserContext`/`Principal`, RBAC helpers.
- `foundation/db` — Database init, транзакции, helpers, базовые DAO.
- `foundation/paging` — пагинация/сортировка/фильтры (общие модели).
- `foundation/time` — Clock/TimeProvider.

---

## 6) Тесты (как зеркалим структуру)

Тесты держим зеркально пакетам:

- unit: `modules/<feature>/domain/*` (без I/O)
- api: `modules/<feature>/api/*` (Ktor test host)
- integration: `modules/<feature>/*` + Testcontainers (Postgres + storage mock)

Полный список — `docs/api-service/testing.md`.
