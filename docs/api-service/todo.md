# TODO: RentControl API service (полный чек‑лист)

Документ — основной backlog для разработки API service. Ставим галочки по мере выполнения.

Связанные документы:
- Спека эндпоинтов: `docs/server-api.md`
- Этапы/майлстоуны: `docs/api-service/plan.md`
- План тестирования: `docs/api-service/testing.md`

---

## M0 — Foundation

### Контракты и стандарты API

- [ ] Зафиксировать базовые конвенции API: `/api/v1`, JSON, кодировки, ISO‑8601, UTC, nullability.
- [ ] Зафиксировать стандарт ошибок (единый `error.code`, `message`, `details[]`, `traceId`).
- [ ] Зафиксировать правила пагинации (page/pageSize или cursor), сортировки и фильтров (единый подход для списков).
- [ ] Зафиксировать идемпотентность: `Idempotency-Key`/`operationId` + политика хранения/дедупликации.
- [ ] Зафиксировать политику совместимости (backward compatibility) и версионирования контракта.
- [ ] Описать схему авторизации (RBAC + границы доступа по данным).
- [ ] Принять решение: ведём ли OpenAPI (рекомендуется) и где хранится файл спеки.

### Проектная структура и инфраструктура

- [x] Разбить API service на модули/пакеты (auth/users/ref/objects/inspections/defects/expenses/meters/attachments/notifications/sync/audit).
- [x] Настроить `ContentNegotiation` (kotlinx.serialization), единый `Json` (strictness/unknown keys).
- [x] Настроить `StatusPages` с маппингом исключений → стандарт ошибок.
- [x] Настроить `CallId`/`traceId` (генерация/проброс) и структурные логи.
- [ ] Настроить конфиг (env + `application.conf`) и profiles (dev/test/prod).
- [ ] Подготовить локальный dev‑стенд (docker compose: Postgres + optional MinIO + mail mock).
- [ ] Добавить seed/test fixtures для локальной разработки (данные для ролей/справочников/шаблонов).

### База данных

- [ ] Зафиксировать основную ER‑модель (таблицы + связи + индексы) под требования `docs/server-api.md`.
- [ ] Добавить миграции (Flyway/Liquibase/своя система) вместо “автосоздания” таблиц на старте (или явно принять это как решение).
- [ ] Добавить стратегию конкурентных изменений (optimistic locking: version/updatedAt).
- [ ] Добавить аудитные поля (createdAt/updatedAt/createdBy/updatedBy) где нужно.
- [ ] Добавить индексы под ключевые выборки (поиск/фильтры: objectId, status, assigneeId, дедлайны, периоды).

### Security

- [x] Выбрать алгоритм хеширования пароля (Argon2id/bcrypt) + параметры и обновление параметров со временем.
- [x] Реализовать JWT (access) + refresh (rotation, revoke) или выбранную альтернативу.
- [x] Зафиксировать поток: BDUI-server не валидирует токен локально, а на каждый запрос вызывает API service (например `GET /me`) для валидации access‑токена и получения user context.
- [ ] Реализовать RBAC‑проверки на уровне маршрутов/сервисов.
- [ ] Добавить rate limiting для auth/reset/OTP и защиту от brute force.
- [ ] Добавить политики блокировки аккаунта и журналирование auth‑событий.
- [ ] Установить лимиты на body/файлы, защиту от oversized payload.

### Observability

- [ ] Метрики (Prometheus/Micrometer): latency по эндпоинтам, ошибки, DB pool, очередь background jobs.
- [x] Healthchecks: `/health`, `/health/db` (readiness/liveness), зависимость от storage/email/push (опционально).
- [ ] Корреляция: traceId в логах и в ответах ошибок.

---

## M1 — Auth (`/auth/*`)

### Модель данных

- [x] Таблицы/структуры: пользователи, статусы, OTP коды (регистрация/сброс), refresh‑сессии, лимиты попыток.
- [x] Политика TTL для OTP и refresh, правила повторной отправки (cooldown).

### Эндпоинты

- [x] `POST /auth/register` — регистрация + генерация OTP + отправка письма.
- [x] `POST /auth/confirm` — подтверждение OTP + активация + выдача токенов.
- [x] `POST /auth/login` — логин + выдача токенов.
- [x] `POST /auth/refresh` — обновление access по refresh + rotation.
- [x] Поток refresh через BDUI-server: клиент хранит refresh‑токен и вызывает `/auth/refresh` через BDUI-server (проксирование без изменения тела/заголовков).
- [x] `POST /auth/logout` — отзыв текущей refresh‑сессии.
- [x] `POST /auth/password/reset/request` — OTP на сброс пароля.
- [x] `POST /auth/password/reset/confirm` — подтверждение OTP + установка нового пароля.
- [x] Mock‑реализация OTP доставки для dev/test (получение кода без реальной почты) + контракт тестов на заменяемость реализаций.

---

## M2 — Профиль и пользователи

### `/me`

- [x] `GET /me` — профиль, роль, настройки.
- [x] `PATCH /me` — обновление профиля/настроек (язык/тема/уведомления/контакты + настройки sync конфликтов).
- [ ] Зафиксировать поля настроек sync в профиле (пример): `sync.autoResolveConflicts` + `sync.conflictStrategy = lastWriteWins` (или аналогичная схема).
- [x] `POST /me/sessions/logout-all` — отзыв всех сессий пользователя.

### `/users` (администрирование/назначения)

- [ ] `GET /users` — поиск/фильтр по роли, выдача списка для назначений.
- [ ] `GET /users/{userId}` — карточка пользователя.
- [ ] `POST /users` — создание пользователя админом.
- [ ] `PATCH /users/{userId}` — редактирование, роль, активность.
- [ ] `POST /users/{userId}/block` — блокировка.
- [ ] `POST /users/{userId}/unblock` — разблокировка.

---

## M3 — Справочники (`/ref/*`)

- [ ] `GET /ref/roles`
- [ ] `GET /ref/object-types`
- [ ] `GET /ref/occupancy-statuses`
- [ ] `GET /ref/defect-categories`
- [ ] `GET /ref/defect-statuses`
- [ ] `GET /ref/defect-priorities`
- [ ] `GET /ref/expense-categories`
- [ ] `GET /ref/expense-statuses`
- [ ] `GET /ref/checklist-templates`
- [ ] `GET /ref/checklist-templates/{templateId}` — структура шаблона (секции/пункты/порядок).

---

## M4 — Объекты и арендаторы

### Объекты (`/objects/*`)

- [x] `GET /objects` — поиск/фильтры/сортировка, пагинация.
- [x] `POST /objects` — создание объекта.
- [x] `GET /objects/{objectId}` — карточка объекта (основные поля + арендатор).
- [x] `GET /objects/{objectId}` — агрегаты (дефекты/осмотры/показания) в ответе.
- [ ] `GET /objects/{objectId}` — агрегаты (расходы) в ответе.
- [x] `PATCH /objects/{objectId}` — редактирование.
- [x] `POST /objects/{objectId}/archive`
- [x] `POST /objects/{objectId}/unarchive`
- [x] `GET /objects/{objectId}/activity` — журнал активности/событий по объекту.

### Арендаторы (`/tenants/*` + link/unlink)

- [x] `GET /tenants` — список для выбора/привязки.
- [x] `POST /tenants` — создание.
- [x] `GET /tenants/{tenantId}` — карточка.
- [x] `PATCH /tenants/{tenantId}` — редактирование.
- [x] `POST /objects/{objectId}/tenant/link` — привязка.
- [x] `POST /objects/{objectId}/tenant/unlink` — отвязка.

---

## M5 — Осмотры (`/inspections/*`)

### Модель данных и статусы

- [ ] Зафиксировать статусы осмотра и допустимые переходы (scheduled → inProgress → finished → approved / rejected / canceled).
- [ ] Модель чек‑листа: пункты, статусы (OK/Problem/NotChecked), комментарии, вложения.
- [ ] Модель “создать дефект из проблемного пункта” (опционально, но зафиксировать контракт).

### Эндпоинты

- [ ] `GET /inspections` — фильтры по объекту/статусу/исполнителю/периоду + поиск.
- [ ] `POST /inspections` — создание + назначение исполнителя + шаблон чек‑листа.
- [ ] `GET /inspections/{inspectionId}` — детали + результаты чек‑листа + вложения.
- [ ] `PATCH /inspections/{inspectionId}` — редактирование до начала выполнения.
- [ ] `POST /inspections/{inspectionId}/cancel` — отмена с причиной.
- [ ] `POST /inspections/{inspectionId}/start` — старт выполнения.
- [ ] `POST /inspections/{inspectionId}/finish` — завершение + итоговый комментарий + опциональная генерация дефектов.
- [ ] `POST /inspections/{inspectionId}/approve` — утверждение.
- [ ] `POST /inspections/{inspectionId}/reject` — отклонение с причиной.
- [ ] `PATCH /inspections/{inspectionId}/checklist` — обновление результатов чек‑листа.

---

## M6 — Дефекты (`/defects/*`)

### Модель данных и статусы

- [ ] Зафиксировать статусы дефекта и переходы (например: open → inProgress → resolved → closed, возврат в работу).
- [ ] История событий/статусов + комментарии.
- [ ] Правила просрочки: вычисление “overdue”.

### Эндпоинты

- [ ] `GET /defects` — фильтры по объекту/статусу/приоритету/исполнителю/просрочке + поиск.
- [ ] `POST /defects` — создание (ручное или из осмотра).
- [ ] `GET /defects/{defectId}` — карточка: описание/статус/исполнитель/дедлайн/вложения/комментарии/история.
- [ ] `PATCH /defects/{defectId}` — изменение атрибутов.
- [ ] `POST /defects/{defectId}/status` — смена статуса + опциональный комментарий.
- [ ] `POST /defects/{defectId}/comments` — добавление комментария + вложения.

---

## M7 — Расходы (`/expenses/*`)

### Бизнес‑правила

- [ ] Реализовать правило видимости расходов: только автор + участники подтверждения.
- [ ] Зафиксировать статусы расхода и переходы (draft → pending → approved/rejected, cancel).
- [ ] Сценарий подтверждения: участники, последовательность (параллельно/по очереди), история.

### Эндпоинты

- [ ] `GET /expenses` — фильтры по объекту/категории/типу/статусу/периоду.
- [ ] `POST /expenses` — создание расхода (вложения чеков, сценарий подтверждения).
- [ ] `GET /expenses/{expenseId}` — карточка + вложения + состояние подтверждений.
- [ ] `PATCH /expenses/{expenseId}` — редактирование автором до завершения подтверждений.
- [ ] `POST /expenses/{expenseId}/submit` — из черновика в ожидание подтверждения.
- [ ] `POST /expenses/{expenseId}/approve` — подтвердить.
- [ ] `POST /expenses/{expenseId}/reject` — отклонить с комментарием.
- [ ] `POST /expenses/{expenseId}/cancel` — отменить автором.

---

## M8 — Приборы учёта и показания

- [ ] `GET /objects/{objectId}/meters` — список приборов объекта.
- [ ] `POST /objects/{objectId}/meters` — добавить прибор (если допускается).
- [ ] `PATCH /objects/{objectId}/meters/{meterId}` — редактирование.
- [ ] `GET /objects/{objectId}/meters/{meterId}/readings` — история показаний (пагинация).
- [ ] `POST /objects/{objectId}/meters/{meterId}/readings` — добавить показание + проверки.

---

## M9 — Вложения и медиа (`/attachments/*`)

### Storage/модель данных

- [ ] Ввести интерфейс `AttachmentStorage` (или аналог) и реализовать минимум 2 адаптера: локальный (dev) и CDN (prod/позже).
- [ ] Определить модель метаданных (contentType, size, checksum, owner, entity binding) и хранение ссылок.
- [ ] Определить политику доступа (кто может скачивать/видеть/удалять).
- [ ] Определить лимиты: размер, типы файлов, количество вложений на сущность.
- [ ] Зафиксировать поддерживаемые потоки загрузки:
  - proxy upload (файл идёт через BDUI-server → API service)

### Эндпоинты

- [ ] `POST /attachments` — загрузка файла/медиа, получение `attachmentId`.
- [ ] `GET /attachments/{attachmentId}` — метаданные + ссылка на скачивание.
- [ ] `DELETE /attachments/{attachmentId}` — удаление по правилам.

### (Опционально, будущее) Direct upload в CDN

- [ ] Добавить prepare/complete протокол (pre‑signed URL) и обновить `docs/server-api.md` для `/attachments`.

---

## M10 — Уведомления и push

- [ ] `GET /notifications` — список уведомлений пользователя.
- [ ] `POST /notifications/{notificationId}/read` — пометить прочитанным.
- [ ] `POST /notifications/read-all` — пометить все прочитанными.
- [ ] `POST /push/token` — регистрация push токена устройства.
- [ ] `DELETE /push/token` — удаление push токена устройства.
- [ ] Push провайдер: FCM (интерфейс + реализация/заглушка) + генерация уведомлений на события домена (осмотры/дефекты/расходы) + дедлайны (по расписанию).

---

## M11 — Синхронизация и офлайн

### Протокол

- [ ] Зафиксировать формат операции для `/sync/batch`: `operationId`, `type`, `entity`, `payload`, `clientTime`, `baseVersion`.
- [ ] Зафиксировать статусы результата операции: applied/duplicate/rejected/conflict + детали.
- [ ] Зафиксировать стратегию конфликтов и как возвращаем conflict клиенту/BDUI: `conflict` + server state + подсказки по разрешению.
- [ ] Версионирование сущностей (optimistic locking): `version`/`updatedAt` и проверка `baseVersion` при изменениях.
- [ ] Тесты конфликтов для ключевых сущностей (осмотры/чек‑лист, дефекты, расходы, показания).
- [ ] Поддержать авто‑разрешение конфликтов по стратегии “Last‑Write‑Wins” (после согласия пользователя), с учётом проблем клиентского времени (clock skew).
- [ ] Зафиксировать UX‑интеграцию: при первом конфликте BDUI спрашивает пользователя и сохраняет настройку в профиле (`PATCH /me`):
  - согласен → включаем auto‑LWW
  - не согласен → фиксируем `server wins` и больше не спрашиваем
- [ ] Далее `/sync/batch` применяет выбранную стратегию автоматически.
- [ ] Если пользователь не согласен на auto‑LWW — поведение по умолчанию: `server wins` (конфликтующая операция не применяется; в ответе возвращаем серверное состояние/метаданные для обновления локального кэша).

### Эндпоинты

- [ ] `POST /sync/batch` — обработка пачки операций (идемпотентность, частичный успех).
- [ ] `GET /sync/since` — выгрузка изменений с метки времени/версии (дельта по сущностям).

---

## M12 — Аудит и события

- [ ] Единая модель audit event (entityType/entityId/action/author/time/metadata).
- [ ] `GET /audit/events` — админский список с фильтрами.
- [ ] `GET /objects/{objectId}/audit/events`
- [ ] `GET /defects/{defectId}/audit/events`
- [ ] `GET /inspections/{inspectionId}/audit/events`
- [ ] `GET /expenses/{expenseId}/audit/events`
- [ ] Заполнение аудита из всех ключевых операций (минимум по `docs/project-functions.md`).

---

## M13 — Production hardening / DevOps

- [ ] Dockerfile/compose для API service (prod‑образ) + параметры запуска.
- [ ] CI pipeline: build → unit → integration → (optional) e2e → publish artifacts.
- [ ] Статический анализ (detekt/ktlint) + dependency scanning.
- [ ] Миграции в CI: test apply migrations на чистую БД.
- [ ] Нагрузочные тесты (минимум smoke + профилирование списков).
- [ ] Документация “как деплоить/как откатывать/как дебажить”.

---

## Интеграция с BDUI-server (как потребителем API)

> Этот блок нужен, чтобы “готовый API service” можно было безопасно подключить к BDUI-server.

- [ ] Способ auth: BDUI-server проксирует `Authorization: Bearer ...` в API service и на каждый запрос получает user context через `GET /me` (или интроспекцию).
- [ ] Кэш `GET /me` в BDUI-server: key = access‑токен, short TTL (config, default = 120 секунд), TTL ≤ exp, сброс при 401/403/logout.
- [ ] Проксирование refresh: клиент вызывает `/auth/refresh` через BDUI-server, BDUI-server просто пересылает запрос в API service.
- [ ] Proxy upload: клиент загружает файл в BDUI-server, BDUI-server отправляет его в API service (`POST /attachments`) и возвращает `attachmentId`.
- [ ] Typed client для API (по OpenAPI или вручную) и использование его в BDUI-server.
- [ ] Добавить contract tests (consumer-driven или OpenAPI validation) между BDUI-server ↔ API service.
- [ ] Добавить e2e smoke: “login → открыть Home → создать осмотр → завершить → создать дефект → увидеть в списке”.
