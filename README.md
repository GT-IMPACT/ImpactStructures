# Impact Structures

[![Latest Release](https://img.shields.io/github/v/tag/GT-IMPACT/ImpactStructures?label=Latest&sort=semver)](https://github.com/GT-IMPACT/ImpactStructures/releases/latest)

Библиотека мультиблочных структур для Minecraft 1.7.10 и Forge 10.13.4.1614.
Идентификатор мода — `impactapi`.

Поддерживает описание и проверку структур, creative- и survival-строительство,
каналы настройки, ориентации и цветные голограммы. Все 16 цветов подсказок доступны
независимо от служебных маркеров.

Java API и KotlinX входят в один артефакт: `space.impact:ImpactStructures:<version>`.

Основной класс Java API — `space.impact.structures.ImpactStructures`.

KotlinX добавляет Kotlin DSL и extensions для структур, рендера и анимаций поверх Java API.
Kotlin runtime предоставляет зависимость Forgelin 2.1.3. [Подключение и примеры](docs/kotlinx.md).

Сборка: JDK 25, Gradle 9.7.1 через wrapper. Для Minecraft и тестов нужен JDK 8.
Целевой байткод — Java 8.

```sh
git submodule update --init --recursive
./gradlew clean build
```

Корневые `build` и `check` охватывают библиотеку вместе с KotlinX и example-мод.
Артефакты сборки: `build/libs/` и `example/build/libs/`.
`verifyKotlinXConsumer` проверяет Java, KotlinX и анимации из опубликованного в локальный
тестовый Maven-репозиторий единого JAR, включая dev/reobf-варианты.

Команды: `/istruct_channel` и `/istruct_registry_debug`.

`runClient` по умолчанию подключает Angelica 2.2.43 и GTNHLib 0.11.52.
Для сравнения без Angelica: `./gradlew runClient -Pangelica=false`.
Эти моды не включаются в JAR, опубликованные зависимости или classpath сервера.
[Рендер голограмм и проверка производительности](docs/rendering.md).

[Анимируемый 3D-рендер структур](docs/structure-rendering.md): отдельные API геометрии,
анимации и управления временем, привязка к каналам либо самостоятельные модели.
Example-мод с космическим лифтом: `./gradlew :ImpactStructures-Example:runClient`.

Опциональные источники материалов для survival-строительства: AE2, WCT,
AE2FluidCraft, Baubles, Forestry, Backpack и Golden Bag из Extra Utilities. Каждый адаптер включается только при
наличии своего мода и необходимых API. [Версии, поведение и проверки](docs/integrations.md).

Forestry, Backpack и Extra Utilities для компиляции находятся в `libs/`; остальные зависимости
загружаются из Maven. В итоговый JAR эти моды не включаются.

Лицензии: [LICENSE](LICENSE), [LICENSE.LESSER](LICENSE.LESSER). [NOTICE](NOTICE).
