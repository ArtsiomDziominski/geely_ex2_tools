# Документация Geely EX2 Tools

| Документ | Описание |
|----------|----------|
| [android-car-apk.md](./android-car-apk.md) | Разбор APK `com.android.car` (Car service): Vehicle HAL, Car API, `VehicleProperty`, eCarX |
| [flyme-settings-apk.md](./flyme-settings-apk.md) | Разбор APK `com.flyme.auto.settings`: Flyme API, VHAL id, классы и паттерны из dex |
| [flyme-hvac-apk.md](./flyme-hvac-apk.md) | Разбор APK `com.flyme.auto.hvac` (Climate): UI, HvacService AIDL, VHAL/Adapt property id |
| [flyme-auto-service-apk.md](./flyme-auto-service-apk.md) | Разбор APK `com.flyme.auto` (FlymeAutoService): CoreService, driving restrictions, SDK |
| [flyme-customize-apk.md](./flyme-customize-apk.md) | Разбор APK `com.flyme.auto.customize` (Themes): темы/обои, ThemeManager, WallpaperProvider, AI |
| [flyme-energy-apk.md](./flyme-energy-apk.md) | Разбор APK `com.flyme.auto.energy` (Energy/Мощность): заряд/разряд, trip, super endurance, AIDL |
| [ecarx-parking-apk.md](./ecarx-parking-apk.md) | Разбор APK `com.ecarx.parking` (AVM): VHAL-команды, EAS API, точки входа, ArcSoft |
| [managedprovisioning-apk.md](./managedprovisioning-apk.md) | Разбор APK `com.android.managedprovisioning`: work profile / device owner provisioning (AOSP) |
| [mediatek-thermalmanager-apk.md](./mediatek-thermalmanager-apk.md) | Разбор APK `com.mediatek.thermalmanager`: thermal policy, sysfs, warning/shutdown |
| [flyme-scenedirector-apk.md](./flyme-scenedirector-apk.md) | Разбор APK `com.flyme.auto.scenedirector` (Scene Mode): Rest/Camping, HAL, VR, SceneProvider |
| [android-shell-apk.md](./android-shell-apk.md) | Разбор APK `com.android.shell` (Оболочка): UID shell, bugreport UX, dumpstate |
| [flyme-wallpaperlauncher-apk.md](./flyme-wallpaperlauncher-apk.md) | Разбор APK `com.flyme.auto.wallpaperlauncher`: live wallpaper, Customize Provider, MIPC/Car3D |

## Локальные артефакты

| Путь | Содержимое |
|------|------------|
| `.tmp/android-car-apk/` | Распакованный `com.android.car` |
| `.tmp/android-car-jadx/` | JADX-декомпилят Car service |
| `.tmp/android-car.apk` | Оригинальный APK (опционально) |
| `.tmp/android-shell.apk` | Оригинальный APK (опционально) |
| `.tmp/android-shell-apk/` | Распакованный `com.android.shell` |
| `.tmp/android-shell-jadx/` | JADX-декомпилят Shell APK |
| `.tmp/flyme-settings-apk/` | Распакованный `com.flyme.auto.settings` |
| `.tmp/flyme-settings.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-hvac-apk/` | Распакованный `com.flyme.auto.hvac` |
| `.tmp/flyme-hvac-jadx/` | JADX-декомпилят Climate APK |
| `.tmp/flyme-hvac.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-auto-service-apk/` | Распакованный `com.flyme.auto` |
| `.tmp/flyme-auto-service-jadx/` | JADX-декомпилят FlymeAutoService |
| `.tmp/flyme-auto-service.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-customize.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-customize-apk/` | Распакованный `com.flyme.auto.customize` |
| `.tmp/flyme-customize-jadx/` | JADX-декомпилят Themes / CustomizeCenter |
| `.tmp/flyme-customize-dexdump.txt` | dexdump `classes.dex` |
| `.tmp/flyme-energy-apk/` | Распакованный `com.flyme.auto.energy` |
| `.tmp/flyme-energy-dexdump.txt` | dexdump Energy APK |
| `.tmp/flyme-energy-strings.txt` | Отфильтрованные строки dex (CHARGE/HYBRID/…) |
| `.tmp/flyme-energy.apk` | Оригинальный APK (опционально) |
| `.tmp/ecarx-parking-apk/` | Распакованный `com.ecarx.parking` |
| `.tmp/ecarx-parking-src/` | JADX-декомпилят AVM APK |
| `.tmp/managedprovisioning.apk` | Оригинальный APK (опционально) |
| `.tmp/managedprovisioning-apk/` | Распакованный `com.android.managedprovisioning` |
| `.tmp/mediatek-thermalmanager.apk` | Оригинальный APK (опционально) |
| `.tmp/mediatek-thermalmanager-apk/` | Распакованный `com.mediatek.thermalmanager` |
| `.tmp/mediatek-thermalmanager-jadx/` | JADX-декомпилят MTK Thermal Manager |
| `.tmp/flyme-scenedirector.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-scenedirector-apk/` | Распакованный `com.flyme.auto.scenedirector` |
| `.tmp/flyme-scenedirector-jadx/` | JADX-декомпилят Scene Mode APK |
| `.tmp/flyme-wallpaperlauncher.apk` | Оригинальный APK (опционально) |
| `.tmp/flyme-wallpaperlauncher-apk/` | Распакованный `com.flyme.auto.wallpaperlauncher` |
| `.tmp/flyme-wallpaperlauncher-jadx/` | JADX-декомпилят AutoWallpaperLauncher |

Папка `.tmp/` в `.gitignore`.
