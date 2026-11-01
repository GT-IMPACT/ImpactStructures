# Impact Structures

[![Latest Release](https://img.shields.io/github/v/tag/GT-IMPACT/ImpactStructures?label=Latest&sort=semver)](https://github.com/GT-IMPACT/ImpactStructures/releases/latest)

Библиотека мультиблочных структур для Minecraft 1.7.10 и Forge 10.13.4.1614.
Идентификатор мода — `impactapi`.

Поддерживает описание и проверку структур, creative- и survival-строительство,
каналы настройки, ориентации и цветные голограммы. Все 16 цветов подсказок доступны
независимо от служебных маркеров.

| Модуль | Maven-координаты |
| --- | --- |
| Java API | `space.impact:ImpactStructures:<version>` |
| KotlinX | `space.impact:ImpactStructures-Ktx:<version>` |

Основной класс Java API — `space.impact.structures.ImpactStructures`.

KotlinX добавляет Kotlin DSL и extensions поверх Java API. Версии обоих артефактов
должны совпадать. [Подключение и примеры](docs/kotlinx.md).

Сборка: JDK 25, Gradle 9.7.1 через wrapper. Для Minecraft и тестов нужен JDK 8.
Целевой байткод — Java 8.

```sh
git submodule update --init --recursive
./gradlew clean build
```

Корневые `build` и `check` охватывают оба проекта. Артефакты сборки:
`build/libs/` и `kotlinx/build/libs/`.

Команды: `/istruct_channel` и `/istruct_registry_debug`.

Опциональные источники материалов для survival-строительства: AE2, WCT,
AE2FluidCraft, Baubles, Forestry, Backpack и Golden Bag из Extra Utilities. Каждый адаптер включается только при
наличии своего мода и необходимых API. [Версии, поведение и проверки](docs/integrations.md).

Forestry, Backpack и Extra Utilities для компиляции находятся в `libs/`; остальные зависимости
загружаются из Maven. В итоговый JAR эти моды не включаются.

Лицензии: [LICENSE](LICENSE), [LICENSE.LESSER](LICENSE.LESSER). [NOTICE](NOTICE).
