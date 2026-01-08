# BDUI Backend Tasks (MVP)

## Scope
Цель: подготовить библиотеку bdui-backend (JVM/Kotlin-first), которую можно подключить к backend-продуктам для генерации экранов по контракту bdui-client. Библиотека модульная: core, dsl, mapper, renderer, runtime (engine), tooling.

## Tasks

1. Core foundation
   - [ ] Базовые модели результатов: `BackendResult<T>` + `BackendError` (Validation, Mapping, Serialization, LimitExceeded).
   - [ ] Общие типы: `ValidationIssue`, лимиты (глубина/размер дерева).
   - [ ] Единые коды/сообщения ошибок для логов и тестов.

2. DSL
   - [ ] Kotlin DSL для декларативной сборки узлов (layout, scaffold, actions, triggers, lifecycle) с дефолтами scroll/pagination/refresh.
   - [ ] Регистрация действий при добавлении в DSL (без ручного registry).
   - [ ] Пресеты/фабрики: header(), list(), button(), bottomBar().

3. Runtime (engine)
   - [ ] Engine без screenId: `render(params: Parameters)`.
   - [ ] Аннотационный реестр: `@ScreenBinding`, `@DraftBinding`, `@ScaffoldBinding`.
   - [ ] FetcherContext с дедупликацией параллельных fetchers.
   - [ ] Параллельная обработка секций + scaffold, сборка RemoteScreen.

4. Mapping
   - [ ] Мапперы секций/scaffold в `RenderingData` (чистая логика без UI).
   - [ ] Поддержка нескольких fetchers в одном маппере, безопасное объединение результатов.
   - [ ] Примеры: home/catalog/details с reused fetchers.

5. Renderer
   - [ ] Рендер `RenderingData` → контрактные ноды (ComponentNode/Scaffold), валидация id, нормализация дефолтов.
   - [ ] Сериализация в JSON (kotlinx.serialization) совместимую с клиентом.
   - [ ] Экспорт схемы (опционально).

6. Tooling
   - [ ] Snapshot-тесты для DSL/renderer (golden JSON).
   - [ ] Линтер DSL/рендера: лимиты, обязательные поля, корректность bindings.
   - [ ] Фабрики тестовых данных/скриншотов.

7. DX & Docs
   - [ ] README для модулей (core/dsl/mapper/renderer/runtime/tooling).
   - [ ] Recipes: список товаров, карточка с actions, экран с overlay/popup, экран с триггерами.
   - [ ] Onboarding: fetcher -> mapper -> render -> serialize.

8. Packaging & Publish
   - [ ] Gradle модули: `bdui-backend-core`, `bdui-backend-dsl`, `bdui-backend-mapper`, `bdui-backend-renderer`, `bdui-backend-runtime`, `bdui-backend-tooling`.
   - [ ] Minimal deps (kotlinx.serialization, coroutines) и target JVM 11.
   - [ ] Sample module/project для интеграции (demo).

## Order of implementation
1) Core foundation (errors/result/context/limits)
2) DSL entry + registries + presets
3) Mapper interface + helpers
4) Renderer (validation + serialization)
5) Tooling (snapshot tests, linters)
6) Data interfaces (retry/timeout policies)
7) Docs/recipes and packaging
