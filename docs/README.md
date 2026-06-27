# Документация Geely EX2 Tools

| Документ | Описание |
|----------|----------|
| [android-car-apk.md](./android-car-apk.md) | Разбор APK `com.android.car` (Car service): Vehicle HAL, Car API, `VehicleProperty`, eCarX |
| [flyme-settings-apk.md](./flyme-settings-apk.md) | Разбор APK `com.flyme.auto.settings`: Flyme API, VHAL id, классы и паттерны из dex |
| [flyme-hvac-apk.md](./flyme-hvac-apk.md) | Разбор APK `com.flyme.auto.hvac` (Climate): UI, HvacService AIDL, VHAL/Adapt property id |
| [flyme-auto-service-apk.md](./flyme-auto-service-apk.md) | Разбор APK `com.flyme.auto` (FlymeAutoService): CoreService, driving restrictions, SDK |
| [ecarx-parking-apk.md](./ecarx-parking-apk.md) | Разбор APK `com.ecarx.parking` (AVM): VHAL-команды, EAS API, точки входа, ArcSoft |

## Локальные артефакты

| Путь | Содержимое |
|------|------------|
| `.tmp/android-car-apk/` | Распакованный `com.android.car` |
| `.tmp/android-car-jadx/` | JADX-декомпилят Car service |
| `.tmp/android-car.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-settings-apk/` | Распакованный `com.flyme.auto.settings` |
| `.tmp/flyme-settings.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-hvac-apk/` | Распакованный `com.flyme.auto.hvac` |
| `.tmp/flyme-hvac-jadx/` | JADX-декомпилят Climate APK |
| `.tmp/flyme-hvac.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-auto-service-apk/` | Распакованный `com.flyme.auto` |
| `.tmp/flyme-auto-service-jadx/` | JADX-декомпилят FlymeAutoService |
| `.tmp/flyme-auto-service.apk` | Оригинальный APK (опционально) |
| `.tmp/ecarx-parking-apk/` | Распакованный `com.ecarx.parking` |
| `.tmp/ecarx-parking-src/` | JADX-декомпилят AVM APK |

Папка `.tmp/` в `.gitignore`.
