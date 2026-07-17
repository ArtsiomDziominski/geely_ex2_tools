# AVAS / system install

Обычный `userDebug` / `adb install` **не может** отключить AVAS: `CarAudioManager.setAVASMode` требует `CAR_CONTROL_AUDIO_VOLUME`, а в CentralEXAuto это работает только потому что APK:

- `android:sharedUserId="android.uid.system"`
- подписан **AOSP platform testkey** (`android@android.com`)

## Сборка system APK (как CentralEXAuto)

```powershell
.\scripts\release-system-apk.ps1
```

Каждый запуск: `VERSION_CODE += 1` в `app\version.properties`, затем сборка и platform-подпись.

Результат:
- `install\out\geely-ex2-tools-v{VERSION_NAME}({VERSION_CODE}).apk` — например `geely-ex2-tools-v0.0.3(2).apk`
- `install\out\geely-ex2-tools-system-platform-signed.apk` — копия с стабильным именем

Без повышения сборки: `.\scripts\release-system-apk.ps1 -NoBump`  
Сменить marketing-версию: `.\scripts\release-system-apk.ps1 -VersionName 0.0.4`

## Установка на ГУ

Сначала снимите обычную user-сборку (иначе конфликт UID/подписи):

```text
adb uninstall com.geely.ex2.tools
```

На этой ГУ надёжнее push + `pm install` (относительный `adb install install\out\...` на Windows часто даёт `filename doesn't end .apk`):

```text
adb push install/out/geely-ex2-tools-system-platform-signed.apk /data/local/tmp/geely-ex2-tools-system.apk
adb shell pm install -r -g /data/local/tmp/geely-ex2-tools-system.apk
```

Или абсолютный путь:

```text
adb install -r "C:\Users\hitma\AndroidStudioProjects\geely_ex2_tools\install\out\geely-ex2-tools-system-platform-signed.apk"
```

При установке в `/system/priv-app/` дополнительно положите:

`install/privapp-permissions-com.geely.ex2.tools.xml` → `/system/etc/permissions/`

## Flavors

| Flavor | sharedUserId | Подпись | AVAS mute |
|--------|--------------|---------|-----------|
| `user` (default) | нет | debug | обычно нет |
| `system` | `android.uid.system` | platform testkey (скрипт) | да, как CentralEXAuto |
