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
