# BDUI Backend Architecture

## Диаграммы
### Поток экрана
```
Router
  └─ Engine.render(params: Parameters)
       └─ ScreenBuilder (по типу params)
            └─ ScreenDraft: Sections + Scaffold + settings/actions/triggers
                 └─ coroutineScope {
                     ├─ для каждой SectionDraft:
                     │    FetcherContext.fetch(...)  (0..N, с кешем)
                     │    DraftMapper(draft, params, fetchers)
                     │    Renderer(RenderingData) -> ComponentNode
                     ├─ ScaffoldDraft:
                     │    FetcherContext.fetch(...)  (0..N)
                     │    DraftMapper -> RenderingData
                     │    Renderer -> Scaffold parts
                     └─ сборка RemoteScreen (layout.sections + scaffold)
                   }
```

### Автопривязки (по типам/аннотациям)
```
@ScreenBinding(params = P, builder = B)        -> registry: P::class -> B()
@DraftBinding(key = K, mapper = M, renderer = R, fetchers=[F*])
    registry: SectionKey K -> mapper M, fetchers F*
    registry: RenderingData(M) -> renderer R
@ScaffoldBinding(key = K, mapper = M, renderer = R, fetchers=[F*])
    аналогично, но для scaffold draft
FetcherContext: fetch(FetcherClass) -> memoized Deferred<Result>
```

### Engine внутри (параллель секций)
```
ScreenDraft
  sections: [K1, K2, ...]    scaffold: K_s

for each draft in parallel:
  mapper = registry.mapperFor(key)
  data   = mapper.map(draft, params, fetcherCtx)
  renderer = registry.rendererFor(data::class)
  node/scaffoldPart = renderer.render(data)

RemoteScreen:
  layout.sections = rendered sections
  layout.scaffold = rendered scaffold
  actions/triggers/settings = из ScreenDraft
```

## Поток
Router -> Engine.render(params: Parameters) -> ScreenBuilder -> (parallel) Section mappers -> (optional multiple Fetchers) -> Renderer -> RemoteScreen.

## Базовые интерфейсы
- `interface Parameters` — маркер входных параметров экрана. Каждому экране сопоставляется свой класс params.
- `interface ScreenBuilder<P : Parameters>` { fun build(params: P): ScreenDraft }
- `data class ScreenDraft(val sections: List<SectionDraft>, val scaffold: ScaffoldDraft?, val settings: ScreenSettings, val actions: List<Action>, val triggers: List<Trigger>)`
- `interface Draft` — маркер.
  - `data class SectionDraft(val key: SectionKey, val sticky: Sticky?, val scroll: SectionScroll, val visibleIf: Condition?) : Draft`
  - `data class ScaffoldDraft(val key: SectionKey, val visibleIf: Condition?) : Draft` (scaffold обрабатывается так же как секция, но отдаёт scaffold parts)
- `interface SectionKey { val id: String }` — объект для идентификации секции/scaffold.

### Мапперы и фетчеры
- `interface Fetcher<R>` { suspend fun fetch(ctx: FetcherContext): BackendResult<R> }
- `interface DraftMapper<D : Draft, R : RenderingData>` { suspend fun map(draft: D, params: Parameters, fetchers: FetcherContext): BackendResult<R> }
  - Маппер может вызывать несколько fetcher’ов; FetcherContext кэширует результаты по классу fetcher + аргументам (если будут).
- `interface RenderingData` — маркер модели для рендера.

### Рендер
- `interface Renderer<R : RenderingData>` { fun render(data: R): RenderOutput }
  - Для секции RenderOutput = ComponentNode
  - Для scaffold RenderOutput = Scaffold (top/bottom/bottomBar), можно держать отдельный тип.

## Регистрация / автопривязка
- Аннотации:
  - `@ScreenBinding(params = HomeParams::class, builder = HomeScreenBuilder::class, packages = [...])` — связывает Parameters с ScreenBuilder. Engine выбирает builder по типу params (без screenId).
  - `@DraftBinding(key = HomeOffersSection::class, mapper = HomeOffersMapper::class, renderer = HomeOffersRenderer::class, fetchers = [OffersFetcher::class])`
  - `@ScaffoldBinding(key = HomeScaffold::class, mapper = HomeScaffoldMapper::class, renderer = HomeScaffoldRenderer::class, fetchers = [...])`
- AnnotationRegistrar сканирует пакеты, создает инстансы и регистрирует:
  - paramsClass -> ScreenBuilder
  - SectionKey -> mapper (+fetchers)
  - RenderingData -> Renderer
  - fetcher -> класс fetcher (для кэша)

## Engine.render(params: Parameters)
1) Находит ScreenBuilder по классу params (через ScreenBinding registry) и строит ScreenDraft.
2) Запускает coroutineScope, для каждой секции и scaffold параллельно:
   - Берёт mapper по SectionKey, вызывает `map(draft, params, fetcherCtx)`.
   - mapper вызывает fetcherCtx.fetch<F>() — отдаёт закэшированный результат для данного fetcher.
   - Получает RenderingData, ищет Renderer по классу модели, рендерит в ComponentNode или Scaffold.
3) Собирает RemoteScreen: layout.sections из отрендеренных секций, scaffold из отрендеренного scaffold, actions/triggers/settings из ScreenDraft.

## Кэш и шаринг fetcher’ов
- FetcherContext хранит Map<KClass<out Fetcher<*>>, Deferred<Result>>. При повторном запросе того же fetcher возвращается готовый результат.
- Позволяет нескольким секциям использовать один fetcher без дублирующих запросов.

## Правила
- Нет screenId в Engine API, только params.
- Мапперы только секций/scaffold, отдельного маппера экрана нет.
- Привязка основана на типах (Parameters -> ScreenBuilder, SectionKey -> Mapper, RenderingData -> Renderer, Fetcher -> по классу).
- Аннотации обеспечивают регистрацию без ручного кода в модулях; остаётся возможность ручной регистрации для override.

## Что нужно реализовать
- Интерфейсы: Parameters, ScreenBuilder, ScreenDraft, SectionDraft, ScaffoldDraft, Draft, RenderingData, Renderer, DraftMapper, Fetcher, FetcherContext.
- Аннотации: @ScreenBinding, @DraftBinding, @ScaffoldBinding.
- AnnotationRegistrar обновить под новые аннотации и типовые реестры.
- Engine: новый конструктор, новое render(params) без screenId, параллельные мапперы, fetcher кэш, рендер секций и scaffold.
- Обновить демо на новый путь (home): params class, builder, mапперы/рендеры с аннотациями.
