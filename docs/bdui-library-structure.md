# Backend-driven UI library (KMP): структура модулей

Цель: вынести backend-driven UI в отдельную KMP-библиотеку, чтобы подключать в приложения (Android/iOS) минимальным Swift-кодом и максимумом Kotlin/Compose.

## Каталожная структура
```
libraries/bdui/
  build.gradle.kts          # агрегатор модулей
  contract/                 # DTO/схема, сериализация, валидация payload
  runtime/                  # движок: state, редьюсер, кеш, нетворк
  components/               # библиотека UI-элементов + темизация
  renderer/                 # построение дерева Compose из контракта
  actions/                  # обработка action/navigation/analytics
  platform-android/         # Android-хост (Activity/Fragment/View)
  platform-ios/             # iOS-хост (UIViewController/ComposeView)
  testing/                  # фикстуры, snapshot-тесты, контрактные тесты
  tooling/                  # генераторы/валидаторы схем (CLI/Gradle tasks)
  demo/                     # минимальный host-пример (dev/playground)
```

## Ответственности модулей
- `contract`: модели контракта (screens/layout/actions/theme), `kotlinx.serialization`, schema validation (json schema/proto), версионирование контрактов, генерация stubs/fixtures.
- `runtime`: сетевой слой (Ktor), кеш (memory + SQLDelight/KeyValue), MVI/Reducer, error handling, feature flags, capability negotiation, логирование.
- `components`: Compose Multiplatform компоненты (Text/Image/Button/List/Form), валидаторы инпутов, лэйаут-примитивы, доступность (contentDescription/roles), theming tokens.
- `renderer`: маппинг дерева контракта → Compose UI, фабрики компонентов, async loading состояния (skeleton/placeholder), встраивание действий/валидаторов.
- `actions`: реестр действий (navigation, open_url, submit_form, analytics_event), маршрутизатор, безопасный парс параметров, хуки для платформенных вызовов.
- `platform-android`: контейнеры `Activity/Fragment/View`, lifecycle-интеграция, deeplink/intent bridge, логирование, доступ к системным сервисам через expect/actual.
- `platform-ios`: минимальный Swift-bridge (`UIViewController`/`ComposeUIViewController`), прокидывание lifecycle/windowInsets, mapping deeplink/push → Kotlin. Swift только для хоста и платформенных API.
- `testing`: golden samples для контрактов, snapshot-тесты компонентов/экранов (Compose), фейковый BFF/репозиторий, нагрузочные кейсы (большие формы/списки).
- `tooling`: генерация схем из Kotlin моделей и обратно, CLI для валидации payload в CI, дев-меню для подмены ответов.
- `demo`: простое приложение, собирающее сборку из `renderer` + платформенного хоста; playground для контрактов.

## Зависимости между модулями
- `renderer` ← зависит от `contract`, `components`, `actions`, `runtime`.
- `components` ← зависит от `contract` (для семантики), общих theme-моделей.
- `platform-android` / `platform-ios` ← зависят от `renderer` + `actions` (для навигации) + `runtime`.
- `testing` ← использует `contract`, `renderer`, `components`, фейковый `runtime`.
- `tooling` ← использует `contract`, может быть `jvm`-only.

## Пакеты и API
- Публичное API: `com.yourorg.bdui.*` с подпространствами `contract`, `renderer`, `host`, `actions`, `theme`.
- Внешний вход: `BDUIScreenHost.render(screenId, params, container)` на платформе.
- Вход для iOS: `BDUIScreenViewController(screenId: String, params: NSDictionary = [:])`.
- Плагины: регистрация кастом-компонентов и действий через DI/registry (`ComponentRegistry`, `ActionRegistry`).

## Gradle/Compose базовая настройка
- Общий `build.gradle.kts` с `kotlin("multiplatform")`, `org.jetbrains.compose` и `kotlinx.serialization`.
- Targets: `androidTarget`, `iosX64/iosArm64/iosSimulatorArm64`; shared sourceSets для `commonMain`, `androidMain`, `iosMain`.
- Compose runtime/ui/foundation/material3; Ktor core/json/logging; SQLDelight/KeyValue; napier/кросс-платформенный логгер.
- В `platform-android`: зависимость на `androidx.activity:activity-compose`, разрешения для сети/интернет, proguard-конфиг (keep для сериализации).
- В `platform-ios`: `cinterop` минимальный, подключение `ComposeUIViewController` (Skia), экспортируемый фреймворк `BDUILibrary`.

## Поток разработки
1) Создать `libraries/bdui` с указанными модулями и общим `settings.gradle.kts` include.
2) Сначала `contract` + `runtime` (сеть/кеш/валидация), затем `components` + `renderer`.
3) Добавить `actions` и навигацию; поднять `demo` (Android) для визуальной проверки.
4) Интегрировать `platform-ios` (минимальный Swift-хост) и smoke-тест рендера.
5) Подключить `testing` (snapshot/contract tests) + `tooling` (валидация в CI).
6) Настроить публикацию артефактов (maven local/registry) и версионирование контрактов.

## Что сразу спроектировать
- Стабильное ABI в `contract` (версионирование + default-поля).
- Расширяемость компонентов/действий через плагины и feature flags.
- Диагностика: логирование контрактов/времени рендера, dev-меню для подмены ответов.
- Ограничители: макс. глубина layout, макс. количество нод, контроль payload size.

Дальше можно углубиться в конкретику build-файлов (plugins, dependencies) или расписать API `ComponentRegistry`/`ActionRegistry` для кастомизации.***
