# KotlinX: структуры на Kotlin

`ImpactStructures-Ktx` — отдельный Kotlin-фасад над Java API Impact Structures. Он создаёт обычные
`IStructureDefinition`/`IStructureElement` и использует тот же обход, survival-строительство, каналы и голограммы.
Существующий Java API остаётся доступен напрямую.

Полный пример контроллера, компилируемый вместе с тестами:
[ExampleController.kt](../kotlinx/src/test/kotlin/space/impact/api/kotlinx/examples/ExampleController.kt).
Он реализует `IConstructableProvider` и `ISurvivalConstructable`, содержит nullable tier-состояние,
канал выбора уровня, callback подсчёта корпусов и собственный элемент стеклянного окна.
`getStructureDefinition()` возвращает кэшированное определение, чтобы проектор обнаруживал объявленные каналы.
Регистрация TileEntity и блока контроллера выполняется в моде-потребителе.

## Подключение

Используйте одинаковую версию ядра и KotlinX:

```kotlin
val impactVersion: String = providers.gradleProperty("impactVersion").get()

repositories {
    maven("https://maven.accident.space/repository/maven-public/")
}

dependencies {
    implementation("space.impact:ImpactStructures-Ktx:$impactVersion")
}
```

Ядро `space.impact:ImpactStructures` приходит через API-зависимость KotlinX. При использовании Gradle Module Metadata
варианты выбираются средствами Gradle/RFG. Для старого потребителя, читающего только POM, dev-артефакт доступен
с classifier `dev`: `space.impact:ImpactStructures-Ktx:<version>:dev`.

Runtime предоставляет **Forgelin 2.1.3** через зависимость KotlinX. Java-ядро от него не зависит. Отдельные `kotlin-stdlib`, `kotlin-reflect`
или coroutines для KotlinX добавлять не требуется. Это библиотека без отдельного mod-bootstrap;
её использование не заменяет установку Impact Structures и Forgelin в игровой сборке.

## Форма и элементы

```kotlin
val definition: IStructureDefinition<ExampleController> = structure<ExampleController> {
    shape(name = "main") {
        layer(
            "CCC",
            "C~C",
            "CCC",
        )
        layer(
            "CCC",
            "CAC",
            "CCC",
        )
    }
    element(
        symbol = 'C',
        element = block(block = Blocks.stone),
    )
    element(
        symbol = 'A',
        element = air(),
    )
}
```

`layer` задаёт C-срез, строки идут по B, символы — по A, как в Java API. DSL не дополняет строки и не меняет оси.
`~` обозначает контроллер, пробел пропускает позицию. Для формы выше смещения контроллера — `offsetA = 1`,
`offsetB = 1`, `offsetC = 0`. Если нужна перестановка B/C, вызовите `transpose` явно.

Внутри builder тип контекста уже известен: у `block`, `air`, `hint`, `chain`, `tiered`, `customElement`
не нужно повторять тип контроллера. Для вложенных контекстов и отложенного выбора доступны `contextElement`,
`lazyElementFor`, `deferredElementWithTrigger`, `partitionBy` и другие фабрики.

Для текстуры блока с отдельной цветной рамкой используйте `withHint`:

```kotlin
withHint(
    element = block(block = casingBlock),
    color = HintColor.BLUE,
)
```

Текстурный куб остаётся размером `0.5`, рамка — `0.52`, с одноцветным ободком толщиной `0.025` блока.
Рисуются обращённые к камере грани рамки. Цвет рамки не изменяет tint текстуры. Проверки, каналы,
описания и строительство выполняет исходный элемент. Доступен также вызов
`element.withHint(HintColor.BLUE)`; в Java — `StructureUtility.withHint(element, HintColor.BLUE)`.
При наведении название блока и цвет HINT выводятся двумя строками, каждая выровнена по центру под прицелом.
Рамка рисуется вместе с текстурой блока, с той же прозрачностью, освещением и проверкой глубины.

## Callback и состояние контроллера

```kotlin
element(
    symbol = 'C',
    element = onPass(element = block(block = Blocks.stone)) {
        acceptedCasings++
    },
)
```

Receiver callback — конкретный контроллер, переданный как `context`. Счётчики сбрасываются перед новой проверкой,
а не при создании определения. В полном примере `checkMachine()` сбрасывает `acceptedCasings` и `casingTier`.

Уровни можно связать с `KMutableProperty1`:

```kotlin
val tierElement: IStructureElement<ExampleController> = tiered<ExampleController, Int?>(
    extractor = { candidate, _ ->
        when (candidate) {
            Blocks.iron_block -> 1
            Blocks.gold_block -> 2
            else -> null
        }
    },
    notSet = null,
    property = ExampleController::casingTier,
    knownTiers = listOf(
        Pair(
            first = Blocks.iron_block,
            second = 0,
        ),
        Pair(
            first = Blocks.gold_block,
            second = 0,
        ),
    ),
)
```

Этот фрагмент размещается в контроллере/его companion, где доступен setter свойства. Внутри `structure`
эквивалентный вызов короче: `tiered<Int?>(...)`. `notSet = null` означает ещё не определённый уровень;
`null` из extractor означает неподходящий блок и не записывается как найденный tier.
Свойство читается и изменяется напрямую: поиск по имени и отдельная зависимость `kotlin-reflect` не используются.
Вместо property доступны receiver-лямбды `getter`/`setter`.

## Собственный элемент

`customElement` принимает `check` и необязательные `hint`, `place`, `survival`, `couldBeValid`,
`blocksToPlace`, `description`, `channels`. Координаты передаются как `Int`; промежуточные position-объекты
на каждом посещаемом блоке не создаются.

```kotlin
val glass: IStructureElement<ExampleController> = block(block = Blocks.glass)
val window: IStructureElement<ExampleController> = customElement(
    check = { world, x, y, z ->
        glass.check(
            this,
            world,
            x,
            y,
            z,
        )
    },
    survival = { world, x, y, z, trigger, environment ->
        glass.survivalPlaceBlock(
            this,
            world,
            x,
            y,
            z,
            trigger,
            environment,
        )
    },
)
```

У этого сокращённого элемента нет проекции и creative-размещения: соответствующие callback не заданы.
В полном контроллере `windowElement()` реализованы все эти пути; расход предметов делегируется заранее созданному
стандартному элементу стекла. Не создавайте backing-элемент заново внутри каждого callback.

## Каналы ItemStack

```kotlin
trigger.setChannelValue(
    name = "casing",
    value = 2,
)
val selectedTier: Int = trigger.channelValue("casing")
val explicitTier: Int? = trigger.channelValueOrNull("casing")
val snapshot: Map<String, Int> = trigger.channels
```

`channelValue` сохраняет fallback Java API к размеру trigger-стека, `channelValueOrNull` возвращает только явно
заданный канал, `channels` создаёт независимый снимок. Для удаления доступны `removeChannel` и `clearChannels`.
Имена каналов задаются в нижнем регистре, значения — положительные.

В определении `channel(name = "casing", values = 1..2)` описывает диапазон,
а `channeled(element = tierElement, channel = "casing")` передаёт выбранное значение элементу.
Это две отдельные операции: объявление диапазона само по себе не переключает trigger элемента.

## Проверка, строительство и scopes

```kotlin
val valid: Boolean = withInstrumentation(identifier = controller) {
    definition.checkStructure(
        context = controller,
        world = world,
        worldX = controller.xCoord,
        worldY = controller.yCoord,
        worldZ = controller.zCoord,
        offsetA = 1,
        offsetB = 1,
    )
}

withHinting(world = world) {
    definition.showHints(
        context = controller,
        world = world,
        worldX = controller.xCoord,
        worldY = controller.yCoord,
        worldZ = controller.zCoord,
        trigger = trigger,
        offsetA = 1,
        offsetB = 1,
    )
}
```

Defaults: `piece = "main"`, `facing = ExtendedFacing.DEFAULT`, все offsets равны нулю,
`forceCheckAllBlocks = false`. Для формы с контроллером не в начале нужно явно передать offsets.
`showHints` делегирует Java API; один `withHinting` объединяет вызовы одной проекции в группу.
Оба scope закрываются через `finally`. Java instrumentation не допускает вложенного включения;
отклонённый внутренний scope не выключает внешний.

Для creative используется `buildCreative`. Survival вызывается на сервере с явными бюджетом и окружением:

```kotlin
val environment: ISurvivalBuildEnvironment = ISurvivalBuildEnvironment.create(
    player.itemSource(),
    player,
)
val built: Int = definition.buildSurvival(
    context = controller,
    world = world,
    worldX = controller.xCoord,
    worldY = controller.yCoord,
    worldZ = controller.zCoord,
    trigger = trigger,
    budget = 32,
    environment = environment,
    offsetA = 1,
    offsetB = 1,
)
```

Здесь `player` — `EntityPlayerMP`. Возвращаемое значение не нормализуется: отрицательный результат завершения
сохраняется, бюджет `<= 0` не вызывает размещение. Проверки стороны, чанков и ресурсов выполняет ядро.
`checkAfterPlacement` по умолчанию выключен; отдельный `checkStructure` после строительства остаётся доступен.

Для отдельных подсказок используйте `world.showHint(..., color = HintColor.WHITE)`,
`world.showMarker(..., kind = HintMarkerKind.AIR)`, `world.showTintedHint`.
`player.markHintError` добавляет служебную рамку и сохраняет цвет; `player.updateHintTint` меняет tint явно.

Для фрейма со своими текстурами передайте предмет для подписи отдельно:

```kotlin
world.showTintedHint(
    worldX = x,
    worldY = y,
    worldZ = z,
    icons = frameTextures,
    tint = frameTint,
    displayStack = frameStack,
)
```

Название берётся из `displayStack` с учётом metadata и NBT. Предмет копируется и не расходуется;
текстуры и tint остаются заданными явно. Без `displayStack` сохраняется общая подпись подсказки.
Для варианта без tint есть `world.showHint(..., icons = frameTextures, displayStack = frameStack)`.
Java API: `ImpactAPI.hintParticleTinted(world, x, y, z, icons, tint, displayStack)` и
`ImpactAPI.hintParticle(world, x, y, z, icons, displayStack)`.
Добавленная через `ImpactAPI.hintFrame` цветная рамка сохраняет это название на первой строке,
а название цвета — на второй; обе строки выравниваются по центру.

## Адаптация чужих контроллеров

Собственный TileEntity может вернуть себя из `IConstructableProvider.getConstructable()`, как полный пример.
Когда интерфейсы нельзя добавить чужому типу, используйте контейнер:

```kotlin
registerContainer<ExampleController>(
    container = multiblockInfoContainer<ExampleController>(
        survival = { trigger, budget, environment, facing ->
            survivalAtFacing(
                trigger = trigger,
                budget = budget,
                environment = environment,
                facing = facing,
            )
        },
        construct = { trigger, hintsOnly, facing ->
            constructAtFacing(
                trigger = trigger,
                hintsOnly = hintsOnly,
                facing = facing,
            )
        },
    ),
)
```

Компилируемый вариант — [ExampleContainer.kt](../kotlinx/src/test/kotlin/space/impact/api/kotlinx/examples/ExampleContainer.kt).
Для собственного `ExampleController` такая регистрация избыточна: он уже реализует provider.
Тип контейнера не ограничен TileEntity; мета-контекст GregTech также можно адаптировать на стороне GregTech.
Lookup `containerOrNull<Context>()` возвращает nullable-контейнер, `toConstructable(context, facing)` доступен напрямую.

## Nullability и кэширование

- `context: T` допускает nullable-контекст, если определение имеет тип `IStructureDefinition<Context?>`.
- `trigger: ItemStack?` обязателен в execution-вызовах, но может быть `null` для элементов, которым предмет не нужен.
  Trigger-aware фабрики вызываются с `null` при обычной проверке: callback должен это учитывать.
- `World` и survival environment передаются явно и не nullable. До прикрепления TileEntity к миру проверяйте `worldObj`.
- Кэшируйте definition и backing-элементы один раз, например в companion через `lazy`; состояние конкретной машины
  храните в контроллере. Callback получает этот контроллер через receiver, а не через глобально захваченную ссылку.
- Property references, reified-типы и обычные Kotlin-функции не требуют coroutine scope или отдельного reflection runtime.

Примеры проверяются компиляцией тестовых исходников; игровой запуск и визуальная проверка выполняются пользователем.
