# Impact Structures

[![Latest Release](https://img.shields.io/github/v/tag/GT-IMPACT/ImpactAPI?label=Latest&sort=semver)](https://github.com/GT-IMPACT/ImpactAPI/releases/latest)

Библиотека структур, survival-строительства, каналов и цветных голограмм для Forge 1.7.10.
Идентификатор мода — `impactapi`, Maven artifact — `space.impact:ImpactAPI`.

Сборка: `./gradlew clean build` на JDK 25. Для Minecraft и тестов нужен установленный JDK 8;
байткод мода остаётся Java 8. UniMixins подключается общими Gradle-плагинами.

NEI доступен при компиляции и в среде разработки. `./gradlew runClient -PdevNei=false`
запускает среду без NEI; `./gradlew runClient` — с NEI. NEI не публикуется обязательной
зависимостью библиотеки. GregTech для компиляции и запуска библиотеки не нужен.

Все 16 цветов HINT сохраняют прежние метаданные. Новый API принимает
`HintColor`, например `StructureUtility.ofHint(HintColor.BLUE)`.
Служебные подсказки рисуют отдельную нейтральную рамку и подпись при наведении.

Команды: `/istruct_channel` и `/istruct_registry_debug`.

Для Kotlin доступен отдельный артефакт `space.impact:ImpactAPI-KotlinX` той же версии:
типизированный DSL, extensions, receiver callbacks и ссылки на свойства. Kotlin runtime
для KotlinX предоставляет мод Forgelin `2.1.3`; отдельные kotlin-stdlib/kotlin-reflect не подключаются.
Основное Java-ядро не зависит от KotlinX или Forgelin.

Проверка отдельных опубликованных артефактов: `./gradlew verifyKotlinXConsumer`.
Задача использует временный Maven-репозиторий внутри `build/` и не публикует во внешний Maven.

- [KotlinX: подключение и примеры](docs/kotlinx.md)

Основан на закреплённом исходном коде StructureLib; происхождение и изменения описаны в
[NOTICE](NOTICE), тексты лицензий — [LICENSE](LICENSE) и [LICENSE.LESSER](LICENSE.LESSER).
