# BDUI (клиент): документация

Эта директория описывает **клиентскую** часть BDUI (модули `libraries/bdui/*`) и формат данных, которые приходят с бэкенда.

## Где “истина” в коде

- **Контракт (модель экрана и компонентов):** `libraries/bdui/contract/src/commonMain/kotlin/org/igorv8836/bdui/contract/*`
- **Рендер (Compose):** `libraries/bdui/renderer/src/commonMain/kotlin/org/igorv8836/bdui/renderer/*`
- **UI-компоненты (Compose-реализации):** `libraries/bdui/components/src/commonMain/kotlin/org/igorv8836/bdui/components/*`
- **Экшены и хендлеры:** `libraries/bdui/actions/*` + инфраструктура `libraries/bdui/core/*`

## Документы

- `docs/bdui/screen-model.md` — модель `RemoteScreen`, секции, скролл, sticky, scaffold, bottom bar, настройки.
- `docs/bdui/components.md` — список всех `ComponentNode` (готовых элементов) и что они означают.
- `docs/bdui/rendering.md` — как сейчас устроен пайплайн рендера на клиенте и где происходит обработка экшенов/переменных.
- `docs/bdui/todo-mvp.md` — TODO-лист для MVP (что сделать в первую очередь).
- `docs/bdui/todo-full.md` — TODO-лист для “полной версии” поверх MVP.

## Для нативной реализации (iOS/Android)

Текущий подход уже “компонентный”:

1. Бэкенд возвращает JSON → клиент десериализует в `RemoteScreen`.
2. `RemoteScreen.layout` содержит дерево `ComponentNode` (и/или список `Section`).
3. Рендер делает **диспетчеризацию по типу узла** и рисует платформенный UI.

Если цель — “из примитивов собрать любые сложные элементы”, то это достигается двумя способами:

- **Композиция** из базовых элементов (`Container(Column/Row/Overlay)`, `TextElement`, `ButtonElement`, …).
- **Отдельные “сложные” элементы** в контракте (например, `CardElement`, `TabsElement`, `ModalElement`, …), которые вы реализуете на платформе как готовые виджеты или как композицию примитивов.

Важно: “Stack” (наложение элементов) уже есть — это `Container(direction = Overlay)`.
