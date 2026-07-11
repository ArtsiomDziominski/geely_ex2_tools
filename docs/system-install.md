# AVAS / system install

Обычный `userDebug` / `adb install` **не может** отключить AVAS: `CarAudioManager.setAVASMode` требует `CAR_CONTROL_AUDIO_VOLUME`, а в CentralEXAuto это работает только потому что APK:

- `android:sharedUserId="android.uid.system"`
- подписан **AOSP platform testkey** (`android@android.com`)

## Сборка system APK (как CentralEXAuto)

```powershell
.\scripts\build-system-apk.ps1
```

Результат: `install\out\geely-ex2-tools-system-platform-signed.apk`

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
