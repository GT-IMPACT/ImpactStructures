# Impact Structures

[![Latest Release](https://img.shields.io/github/v/tag/GT-IMPACT/ImpactAPI?label=Latest&sort=semver)](https://github.com/GT-IMPACT/ImpactAPI/releases/latest)

Библиотека структур, survival-строительства, каналов и цветных голограмм для Minecraft 1.7.10
и Forge 10.13.4.1614. Название мода — **Impact Structures**, идентификатор — `impactapi`.

| Модуль | Maven-координаты | Зависимости для игры |
| --- | --- | --- |
| Java-ядро | `space.impact:ImpactAPI:<version>` | UniMixins |
| KotlinX, каталог `kotlinx/` | `space.impact:ImpactAPI-Ktx:<version>` | Impact Structures и Forgelin 2.1.3 |

KotlinX предоставляет DSL, extensions, receiver callbacks и ссылки на свойства поверх Java-ядра.
Версии обоих артефактов должны совпадать. Kotlin runtime предоставляет Forgelin;
ядро работает самостоятельно, без KotlinX и Forgelin. GregTech не требуется.

Для сборки нужны JDK 25 и установленный JDK 8 для Minecraft и тестов.
Gradle 9.7.1 запускается через wrapper; Java и Kotlin компилируются в байткод Java 8.

```sh
git submodule update --init --recursive
./gradlew clean build
```

В PowerShell используйте `.\gradlew.bat clean build`. Корневые `build` и `check` охватывают
оба проекта. Dev, reobf и sources JAR создаются отдельно для каждого артефакта
в `build/libs/` и `kotlinx/build/libs/`.

NEI подключён как `compileOnly` и `runtimeOnlyNonPublishable`: доступен при компиляции
и автоматически включён в среду разработки, но остаётся необязательной игровой интеграцией.
UniMixins для среды разработки подключают общие Gradle-плагины.

Все 16 цветов HINT используют прежние метаданные `0–15`.
Java API: `StructureUtility.ofHint(HintColor.BLUE)`.
Служебные маркеры `GENERIC`, `AIR`, `NOT_AIR`, `ERROR` используют отдельные рамки
и локализованную подпись при наведении, сохраняя доступность всех цветов.

Команды: `/istruct_channel` и `/istruct_registry_debug`.

[KotlinX: подключение, DSL и примеры контроллера](docs/kotlinx.md).

Основан на закреплённом исходном коде StructureLib; происхождение и изменения описаны в
[NOTICE](NOTICE), тексты лицензий — [LICENSE](LICENSE) и [LICENSE.LESSER](LICENSE.LESSER).
