# Модель экрана (`RemoteScreen`)

Ниже — **контракт**, который приходит с бэкенда и используется клиентом для рендера.

Исходники (клиент): `libraries/bdui/contract/src/commonMain/kotlin/org/igorv8836/bdui/contract/ScreenModels.kt`

## `RemoteScreen`

- `id: String` — идентификатор экрана (роута).
- `version: Int` — версия экрана (можно использовать для кеширования/инвалидации).
- `layout: Layout` — верстка экрана.
- `actions: List<Action>` — список экшенов, на которые ссылаются элементы через `actionId`.
- `triggers: List<Trigger>` — триггеры на изменения переменных/ивенты экрана.
- `settings: ScreenSettings` — скролл, пагинация, pull-to-refresh.
- `lifecycle: ScreenLifecycle` — сценарии `onOpen/onAppear/...` (список `UiEvent`).
- `context: ExecutionContext` — параметры/метаданные выполнения (то, что пришло с бэкенда).

## `Layout`

`Layout` может быть описан двумя способами:

1) **Root-tree:**
- `root: ComponentNode?` — корневой узел дерева компонентов.

2) **Посекционно (рекомендуется для сложных экранов):**
- `sections: List<Section>` — список секций.

Дополнительно:
- `scaffold: Scaffold?` — “обвязка” вокруг контента (top/bottom/bottom bar).

## `Scaffold`

- `top: ComponentNode?` — верхняя часть (например, заголовок, шапка, тулбар).
- `bottom: ComponentNode?` — нижняя часть (например, футер).
- `bottomBar: BottomBar?` — нижняя навигация (tabs).

## `BottomBar` и `BottomTab`

`BottomBar` задается **с бэкенда** (текст, иконки, бейджи и цвета):

- `tabs: List<BottomTab>`
- `selectedTabId: String?`
- `containerColor/selectedIconColor/unselectedIconColor/selectedLabelColor/unselectedLabelColor: Color?`

`BottomTab`:

- `id: String` — id таба.
- `title: String` — заголовок таба.
- `actionId: String` — экшен, который будет вызван при выборе таба.
- `iconUrl: String?` — URL иконки (если иконка простая/удобнее так).
- `badge: String?` — текст бейджа.
- `badgeTextColor/badgeBackgroundColor: Color?`
- `label: ComponentNode?` — кастомная “label” (любая верстка).
- `icon: ComponentNode?` — кастомная иконка (любая верстка).
- `visibleIf: Condition?` — условие видимости.

## `Section`

Секция — единица построения экрана, которая может иметь собственные правила скролла и “липкости”.

- `id: String` — уникальный id секции (используется как key в списке).
- `content: ComponentNode` — содержимое секции.
- `sticky: Sticky?` — “липкая” секция (см. ниже).
- `scroll: SectionScroll` — скролл внутри секции.
- `visibleIf: Condition?` — условие видимости секции.

### Sticky

`sticky` описывается структурой:

- `edge: StickyEdge` — `Top` или `Bottom`.
- `mode: StickyMode` — режим показа.

`StickyMode`:

- `Always` — если секция “прошла” вверх (стала выше текущего viewport), она отображается как pinned/overlay всегда.
- `OnScrollTowardsEdge` — отображается только когда пользователь скроллит **к соответствующему краю**:
  - для `Top` — показывается при скролле вверх,
  - для `Bottom` — показывается при скролле вниз.

Текущая реализация pinned sticky на клиенте — это **overlay поверх списка** (контент не сдвигается).

## `SectionScroll`

Позволяет включать скролл внутри секции:

- `enabled: Boolean`
- `orientation: ScrollOrientation` — `Vertical` или `Horizontal`
- `userScrollEnabled: Boolean`
- `contentPadding: Float?`

Важно: если секция уже находится внутри основного `LazyColumn`, вертикальный скролл внутри секции намеренно ограничен, чтобы не создавать `LazyColumn` внутри `verticalScroll()` (и наоборот), т.к. это ломает измерение в Compose.

## `ScreenSettings`

- `scrollable: Boolean` — если `true`, то root-экран может быть `verticalScroll` (когда нет секций и нет `LazyList` внутри).
- `pagination: PaginationSettings?` — если включено, клиент может вызывать `onLoadNextPage`.
- `pullToRefresh: PullToRefresh?` — если включено, клиент может вызывать `onRefresh`.

## `Trigger` / `ScreenLifecycle`

`Trigger` запускает экшены при событиях:

- `source: TriggerSource`
  - `VariableChanged` (изменение переменной)
  - `ScreenEvent` (например `OnOpen`, `OnAppear`, `RefreshCompleted`, …)
- `condition: Condition?`
- `actions: List<Action>`

`ScreenLifecycle` — это декларативное описание “ивентов экрана” (`onOpen`, `onAppear`, …) как списка `UiEvent`.
