# Geely EX2 Tools

Android-приложение на Kotlin с Jetpack Compose.

## Требования

- Android Studio Ladybug (2024.2+) или новее
- JDK 17–21 (Android Studio использует встроенный JBR)
- Android SDK 35

> **Примечание:** системный JDK 25 не поддерживается Kotlin Gradle Plugin. `gradlew.bat` автоматически подхватывает JBR из Android Studio на Windows.

## Сборка

```bash
./gradlew assembleDebug
```

Установка на подключённое устройство:

```bash
./gradlew installDebug
```

## Релиз (system APK)

System-сборка с platform-подписью (как для установки на ГУ):

```powershell
.\scripts\release-system-apk.ps1
```

Каждый запуск:

1. повышает сборку (`VERSION_CODE += 1` в `app\version.properties`)
2. собирает `systemRelease`
3. кладёт APK в `install\out\`

| Команда | Действие |
|---------|----------|
| `.\scripts\release-system-apk.ps1` | bump сборки + релиз |
| `.\scripts\release-system-apk.ps1 -NoBump` | релиз без повышения сборки |
| `.\scripts\release-system-apk.ps1 -VersionName 0.0.4` | новая версия + bump сборки |

Версия и сборка хранятся в `app\version.properties`.

Подробнее: [docs/system-install.md](docs/system-install.md)

## Структура проекта

```text
app/
└── src/main/
    ├── java/com/geely/ex2/tools/
    │   ├── GeelyEx2ToolsApp.kt    # Application
    │   ├── MainActivity.kt        # Точка входа, Compose UI
    │   └── ui/theme/              # Material 3 тема
    ├── res/                       # Ресурсы
    └── AndroidManifest.xml
```

## Параметры

| Параметр    | Значение              |
|-------------|-----------------------|
| Package     | `com.geely.ex2.tools` |
| minSdk      | 26                    |
| targetSdk   | 35                    |
| UI          | Jetpack Compose       |
