# TODO: BDUI Client MVP

Цель MVP: подключение BDUI к любому приложению с минимальной конфигурацией и стабильный рендер основных экранов.

## 0) Контракт и совместимость

- [ ] Зафиксировать формат сериализации: `type` для всех полиморфных сущностей (components/actions/variables/triggers) + обратная совместимость.
- [ ] Добавить правила версионирования экрана (`RemoteScreen.version`) и стратегию миграций/кеша.
- [ ] Документировать обязательные/опциональные поля и дефолты (что клиент ожидает).

## 1) Public API и “подключение за 5 минут”

- [ ] Единый “вход” для потребителя: фабрика/конфиг для создания engine/host (эндпоинт, таймауты, заголовки, логгер, кеш).
- [ ] Дефолтные реализации по умолчанию: network client, screen repository, variable store, action registry, navigator.
- [ ] Возможность переопределять части извне (custom actions/handlers, navigator, storage), без форка библиотеки.
- [ ] Чёткая схема жизненного цикла: кто вызывает `onOpen/onAppear/onFullyVisible/onDisappear` и как это связано с triggers.

## 2) Рендер и компоновка

- [ ] Гарантировать отсутствие крашей из-за вложенных скроллов (LazyList + verticalScroll) во всех путях (root/sections/scaffold/sticky).
- [ ] Довести bottom bar до “не мигает / не пропадает / корректно сохраняется при навигации”.
- [ ] Sticky секции: стабильное поведение (Top/Bottom, Always/OnScrollTowardsEdge) + сценарии с несколькими sticky.

## 3) Компоненты (покрытие и фичи)

- [ ] Поддержать цвета (`Color(light,dark?)`) для всех элементов/состояний (текст/фон/иконки/бейджи).
- [ ] Договориться про минимальный “набор примитивов” (Container/Text/Button/Image/List/Spacer/Divider) и обеспечить 100% покрытие.
- [ ] Сложные компоненты: реализовать/довести те, что уже есть в контракте (Tabs/CardGrid/Modal/Snackbar/Inputs/Progress/State).
- [ ] Overlay/Stack: зафиксировать поведение `Container(direction=Overlay)` (порядок отрисовки, перекрытие).

## 4) Actions/Variables/Triggers

- [ ] Экшены: стабильная диспетчеризация через `ActionRegistry` (без “fallback” магии), понятные ошибки при неизвестном экшене.
- [ ] Переменные: корректная интерполяция `@{var}` (локальные → глобальные), обновление UI при изменениях.
- [ ] Triggers: базовые источники (`VariableChanged`, `ScreenEvent`) + детерминированность (debounce/throttle/maxExecutions).

## 5) Network + Cache + Errors

- [ ] Стандартизировать ошибки сети/десериализации/валидации (коды, сообщения, debug payload).
- [ ] Кеширование: стратегия (in-memory + демонстрация disk), инвалидирование по version, offline fallback.
- [ ] Логирование: подключаемый логгер (console + composite), уровни, единый набор ключей/сообщений.

## 6) Тесты и демо

- [ ] Unit тесты для: binding resolver, action registry/handlers, variable store, triggers.
- [ ] UI тесты для: sticky sections, bottom bar, базовых интеракций (кнопки/табы/инпуты).
- [ ] Демо-экран/бэкенд сценарии: 2–3 экрана + навигация + вариативный scaffold/bottom bar.
