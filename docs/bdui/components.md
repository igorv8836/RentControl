# Компоненты (`ComponentNode`)

Истина (клиент): `libraries/bdui/contract/src/commonMain/kotlin/org/igorv8836/bdui/contract/Components.kt`

Все компоненты — это узлы дерева `ComponentNode` (у каждого есть `id`), которые рендерятся на клиенте.

## Общие правила

- `id` должен быть **уникальным в рамках экрана** (и внутри секций тоже).
- `visibleIf` поддерживается большинством элементов: если условие `false`, элемент не рендерится.
- `actionId` — это ссылка на экшен по `id` из `RemoteScreen.actions`.
- Цвета задаются через `Color(light, dark?)`. На клиенте выбирается вариант по текущей теме (dark/light).

## Цвета

### `Color`

- `light: String` — hex (например `#FF0000`).
- `dark: String?` — hex для тёмной темы (если `null`, используется `light`).

## Layout / контейнеры

### `Container`

**Назначение:** группировка детей и базовая компоновка.

- `direction: ContainerDirection`
  - `Column` — вертикальный список
  - `Row` — горизонтальный ряд
  - `Overlay` — наложение элементов (**Stack**)
- `children: List<ComponentNode>`
- `spacing: Float?` — отступ между дочерними элементами (для `Row/Column`)
- `backgroundColor: Color?`
- `visibleIf: Condition?`

> `Overlay` — это текущий эквивалент “Stack”: дети рисуются поверх друг друга в порядке списка.

### `SpacerElement`

**Назначение:** пустое пространство.

- `width: Float?`
- `height: Float?`
- `visibleIf: Condition?`

### `DividerElement`

**Назначение:** разделитель.

- `thickness: Float?`
- `color: Color?`
- `insetStart: Float?`
- `visibleIf: Condition?`

### `LazyListElement`

**Назначение:** список, который сам является скроллируемым контейнером (LazyColumn-подобный).

- `items: List<ComponentNode>` — элементы списка
- `placeholderCount: Int`
- `backgroundColor: Color?`
- `visibleIf: Condition?`

> Важно: не вкладывайте `LazyListElement` в `Section` с включенным вертикальным скроллом — это ограничено на клиенте (Compose).

### `CarouselElement`

**Назначение:** горизонтальная карусель.

- `items: List<ComponentNode>`
- `backgroundColor: Color?`
- `visibleIf: Condition?`

## Текст/контент

### `TextElement`

**Назначение:** текст.

- `text: String` — итоговый текст. Поддерживает интерполяцию переменных: `Hello, @{userName}`.
- `style: TextStyle`
- `textColor: Color?`
- `template: String?` — если задано, используется вместо `text` как шаблон для интерполяции.
- `semantics: Semantics?`
- `visibleIf: Condition?`

### `ImageElement`

**Назначение:** картинка (пока placeholder-реализация).

- `url: String`
- `description: String?`
- `backgroundColor: Color?`
- `textColor: Color?`
- `visibleIf: Condition?`

### `MapElement`

**Назначение:** карта (пока placeholder-реализация).

- `title/subtitle: String?`
- `titleColor/subtitleColor/backgroundColor: Color?`
- `visibleIf: Condition?`

## Клики / навигация

### `ButtonElement`

**Назначение:** кнопка.

- `title: String` (поддерживает интерполяцию через `BindingResolver.text`)
- `actionId: String` — вызывается при клике
- `kind: ButtonKind`
- `isEnabled: Boolean`
- `textColor/backgroundColor: Color?`
- `enabledIf: Condition?`
- `visibleIf: Condition?`

### `ListItemElement`

**Назначение:** элемент списка (title/subtitle).

- `title: String`
- `subtitle: String?`
- `actionId: String?`
- `titleColor/subtitleColor/backgroundColor: Color?`
- `enabledIf: Condition?`
- `visibleIf: Condition?`

### `CardElement`

**Назначение:** карточка (title/subtitle/image/badge).

- `title/subtitle/imageUrl/badge`
- `actionId: String?`
- `titleColor/subtitleColor/badgeTextColor/badgeBackgroundColor/backgroundColor: Color?`
- `visibleIf: Condition?`

### `CardGridElement`

**Назначение:** grid карточек.

- `items: List<CardElement>`
- `columns: Int`
- `backgroundColor: Color?`
- `visibleIf: Condition?`

### `TabsElement`

**Назначение:** вкладки.

- `tabs: List<TabItem>`
- `selectedTabId: String?`
- `selectedTabTextColor/unselectedTabTextColor`
- `selectedTabBackgroundColor/unselectedTabBackgroundColor`
- `visibleIf: Condition?`

`TabItem`:

- `id`, `title`, `actionId`
- `badge: String?`
- `textColor/selectedTextColor/backgroundColor/selectedBackgroundColor/badgeTextColor/badgeBackgroundColor`
- `visibleIf: Condition?`

## Формы / инпуты

### `TextFieldElement`

- `label: String`
- `value: String`
- `placeholder: String?`
- `actionId: String?` — вызывается при изменении/подтверждении (зависит от реализации)
- `textColor/labelColor/placeholderColor/backgroundColor`
- `visibleIf: Condition?`

### `DropdownElement`

- `label: String`
- `options: List<String>`
- `selectedIndex: Int?`
- `actionId: String?`
- `labelColor/selectedTextColor/backgroundColor`
- `visibleIf: Condition?`

### `SliderElement`

- `value: Float`, `rangeStart: Float`, `rangeEnd: Float`
- `actionId: String?`
- `textColor/thumbColor/activeTrackColor/inactiveTrackColor`
- `visibleIf: Condition?`

### `SwitchElement`

- `checked: Boolean`
- `title: String`
- `actionId: String?`
- `titleColor/checkedThumbColor/uncheckedThumbColor/checkedTrackColor/uncheckedTrackColor`
- `visibleIf: Condition?`

### `ChipGroupElement`

- `chips: List<ChipItem>`
- `singleSelection: Boolean`
- `chipTextColor/chipBackgroundColor/selectedChipTextColor/selectedChipBackgroundColor`
- `visibleIf: Condition?`

`ChipItem`:

- `id`, `label`, `selected`
- `actionId: String?`
- `textColor/backgroundColor/selectedTextColor/selectedBackgroundColor`
- `visibleIf: Condition?`

## Оверлеи / состояния / прогресс

### `ModalElement`

**Назначение:** модальное окно с контентом.

- `content: ComponentNode`
- `primaryActionId: String?`
- `dismissActionId: String?`
- `backgroundColor/scrimColor`
- `visibleIf: Condition?`

### `SnackbarElement`

- `message: String`
- `actionText: String?`
- `actionId: String?`
- `messageColor/backgroundColor/actionTextColor`
- `visibleIf: Condition?`

### `StateElement`

- `state: StateKind` (`Loading/Empty/Error/Success`)
- `message: String?`
- `actionId: String?`
- `textColor/backgroundColor/actionTextColor`
- `visibleIf: Condition?`

### `ProgressElement`

- `style: ProgressStyle` (`Linear/Circular`)
- `progress: Float?` (если `null` — indeterminate)
- `indicatorColor/trackColor`
- `visibleIf: Condition?`
