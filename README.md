# Impact Structures

[![Latest Release](https://img.shields.io/github/v/tag/GT-IMPACT/ImpactAPI?label=Latest&sort=semver)](https://github.com/GT-IMPACT/ImpactAPI/releases/latest)

Библиотека мультиблочных структур для Minecraft 1.7.10 и Forge 10.13.4.1614.
Идентификатор мода — `impactapi`.

Поддерживает описание и проверку структур, creative- и survival-строительство,
каналы настройки, ориентации и цветные голограммы. Все 16 цветов подсказок доступны
независимо от служебных маркеров.

| Модуль | Maven-координаты |
| --- | --- |
| Java API | `space.impact:ImpactStructures:<version>` |
| KotlinX | `space.impact:ImpactStructures-Ktx:<version>` |

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

Лицензии: [LICENSE](LICENSE), [LICENSE.LESSER](LICENSE.LESSER). [NOTICE](NOTICE).
