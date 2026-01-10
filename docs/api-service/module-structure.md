# Структура модулей API service

API service оформлен как **Gradle multi-module** внутри `server/rent-control-server`, чтобы:
- доменные фичи развивались независимо (быстрее сборка, проще зависимости)
- инфраструктура (`foundation`) переиспользовалась без копипасты
- `app` оставался тонким entrypoint (wiring + routing)

Цель: единый шаблон для всех доменов (auth/objects/inspections/…), чтобы код было легко расширять, тестировать и сопровождать без циклических зависимостей.

---

## 1) Gradle‑модули верхнего уровня (`server/rent-control-server`)

Рекомендуемая структура:

```
server/rent-control-server/
  build.gradle.kts        # агрегатор (group/version, общие задачи)
  app/                    # entrypoint: Ktor app, routing, DI wiring, resources
    build.gradle.kts
    src/main/kotlin/org/igorv8836/rentcontrol/server/app/
    src/main/resources/
      application.conf
      logback.xml
  foundation/             # инфраструктура: errors/http/security/db и общие типы
    build.gradle.kts
    src/main/kotlin/org/igorv8836/rentcontrol/server/foundation/
  integrations/           # внешние интеграции (реализации портов): otp/mock, storage/local|cdn, push/fcm
    build.gradle.kts
    src/main/kotlin/org/igorv8836/rentcontrol/server/integrations/
  modules/                # доменные фичи (каждая — отдельный Gradle‑модуль)
    auth/
      build.gradle.kts
      src/main/kotlin/org/igorv8836/rentcontrol/server/modules/auth/
    me/
      build.gradle.kts
      src/main/kotlin/org/igorv8836/rentcontrol/server/modules/me/
    users/
      build.gradle.kts
      src/main/kotlin/org/igorv8836/rentcontrol/server/modules/users/
    objects/
      build.gradle.kts
      src/main/kotlin/org/igorv8836/rentcontrol/server/modules/objects/
    ref/ ...              # будущие модули по `docs/server-api.md`
```

### Роли Gradle‑модулей

- `app/`
  - поднимает Ktor, подключает плагины, собирает зависимости (manual DI), регистрирует роуты модулей.
- `foundation/`
  - общие типы и инфраструктура, которые нужны всем фичам (ошибки, HTTP, auth context, DB, time).
- `modules/<name>/` (Gradle‑модуль)
  - доменная логика и HTTP слой конкретной фичи (роуты/DTO, сервисы, репозитории).
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
server/rent-control-server/modules/<feature>/
  build.gradle.kts
  src/main/kotlin/org/igorv8836/rentcontrol/server/modules/<feature>/
    api/                 # Ktor routes + request/response DTO + validation
      dto/
        ...
    domain/              # use-cases, бизнес-правила, статусы, политики
      model/
        ...
      service/
        <Feature>Service.kt
      port/
        ...
    data/                # репозитории, DB маппинг, Exposed queries
      repo/
        Exposed<Feature>Repository.kt
    module/              # Ktor registration: `fun Route.<feature>Module(...)`
      <Feature>Module.kt
```

Правило зависимостей внутри фичи:
- `api` зависит от `domain` (вызывает use-cases).
- `domain` зависит от `data` только через интерфейсы репозиториев/портов.
- `data` зависит от `foundation/db` и Exposed.
- реализации портов могут жить в `integrations/*` и инжектиться в `module`.

---

## 4) Пример: `modules/auth` (структура одного модуля)

```
server/rent-control-server/modules/auth/
  src/main/kotlin/org/igorv8836/rentcontrol/server/modules/auth/
    api/dto/...
    domain/...
    data/repo/...
    module/AuthModule.kt        # содержит функцию `fun Route.authModule(...)`
```

Что важно для `auth`:
- OTP sender — порт (`domain/port/OtpSender.kt`), а mock реализация лежит в `integrations/otp/MockOtpSender.kt`.
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
