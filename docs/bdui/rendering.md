# Как устроен рендер на клиенте

Цель этого документа — дать “карту” того, как `RemoteScreen` превращается в UI и где выполняются экшены/биндинги.

## Ключевая идея

На клиенте рендер — это **диспетчер**:

- вход: дерево `ComponentNode` и/или список `Section`
- выход: платформенный UI
- действие пользователя → `actionId` → поиск экшена в `RemoteScreen.actions` → `ActionRegistry.dispatch(...)`

## Основные файлы

- `libraries/bdui/renderer/host/ScreenHost.kt` — точка входа в UI (Compose).
- `libraries/bdui/renderer/screen/RenderScreen.kt` — сборка контента экрана (sections/root + scaffold + bottom bar).
- `libraries/bdui/renderer/section/SectionsHost.kt` — рендер секций + sticky overlay.
- `libraries/bdui/renderer/node/NodeRenderer.kt` — `when(node)` диспетчер по типам.
- `libraries/bdui/components/*` — Compose-реализации компонент.
- `libraries/bdui/renderer/binding/BindingResolver.kt` — интерполяция `@{var}` и условия `visibleIf/enabledIf`.

## Поток данных (высокоуровнево)

```text
RemoteScreen (JSON)
  -> deserialize -> RemoteScreen (Kotlin)
  -> ScreenState(status=Ready, remoteScreen=...)
  -> ScreenHost(state)
      -> RenderScreen(remoteScreen)
          -> RenderSections(...) | RenderNode(root) (+ scaffold)
              -> RenderNode(...) dispatch -> components/*
```

## `ScreenHost`

`ScreenHost` принимает:

- `ScreenState` (Idle/Loading/Ready/Error),
- `ActionRegistry` (исполнение экшенов),
- `VariableStore` (переменные),
- `Navigator` (навигация),
- колбэки `onRefresh/onLoadNextPage/onAppear/onFullyVisible/onDisappear`.

Когда экран `Ready`:

1) находит экшен по `actionId` в `remoteScreen.actions`  
2) вызывает `actionRegistry.dispatch(action, ActionContext(...))`

## `RenderScreen`

`RenderScreen` решает, как рисовать контент:

- если `layout.sections` не пуст → рендер через `RenderSections` (основной путь для “посекционных” экранов)
- иначе → рендер `layout.root`
  - если внутри `root` есть `LazyListElement`, используется безопасная компоновка, чтобы не получить `LazyColumn` внутри `verticalScroll()`

Также `RenderScreen`:

- поднимает `BindingResolver` (привязан к `screenId` и версии переменных),
- включает pull-to-refresh / pagination (если переданы колбэки и включено в `ScreenSettings`),
- рисует `Scaffold.top/bottom`,
- рисует `Scaffold.bottomBar` (bottom navigation), если задан.

## `BindingResolver`

Сейчас поддерживается:

- **Интерполяция переменных:** `@{ variableName }` внутри строки.
- **Поиск переменных:** сначала `VariableScope.Screen(screenId)`, затем `VariableScope.Global`.
- **Условия:** `visibleIf` и `enabledIf` через `Condition`.

Если переменной нет — подставляется пустая строка.

## `RenderSections` (sticky и скролл)

Основной список секций — `LazyColumn`.

Sticky реализован как overlay:

- вычисляется “последняя секция выше viewport”, у которой `sticky.edge == Top/Bottom`
- и при необходимости рисуется поверх списка в `Alignment.TopCenter` или `Alignment.BottomCenter`
- `StickyMode.OnScrollTowardsEdge` дополнительно учитывает направление скролла

Важно: sticky overlay **не занимает место** в списке (контент под ним не сдвигается).

Скролл внутри секции (`Section.scroll`) включается только когда секция рисуется **вне** основного списка (например, sticky overlay), чтобы избежать конфликтов измерения в Compose.

## `RenderNode`

`RenderNode` — центральный диспетчер:

```kotlin
when (node) {
  is TextElement -> TextComponent(...)
  is ButtonElement -> ButtonComponent(...)
  is Container -> ContainerComponent(renderChild = { RenderNode(child) })
  ...
}
```

То есть “система компонентов” на клиенте уже построена так, что **каждый `ComponentNode` соответствует отдельной платформенной реализации**.

## Что это означает для нативной реализации (Swift/Android View system)

Чтобы реализовать тот же подход нативно:

- сделать аналог `RenderNode` (dispatcher по типу/`type`),
- для каждого `ComponentNode` реализовать платформенный компонент,
- для `Container(direction=Overlay)` реализовать “Stack” (наложение детей),
- прокинуть:
  - `onAction(actionId)` (нажатия/изменения),
  - `BindingResolver` (или эквивалент),
  - выбор `Color(light/dark)` по теме.

Если хочется “собирать всё из примитивов” — можно реализовать сложные элементы (`CardElement`, `TabsElement`, …) как композицию базовых контейнеров/текста/кнопок, но **контракт сейчас уже содержит** эти типы, поэтому их всё равно нужно распознавать.
