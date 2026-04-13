# SleepTimer – Installationsanleitung
## Xiaomi 13T Pro mit HyperOS 3.0

---

## Methode 1: GitHub Actions (empfohlen)

### Voraussetzungen
- GitHub-Konto
- Android Studio (nur für ADB-Berechtigungen)

### Schritte

1. **Repository erstellen**
   - Neues GitHub-Repository anlegen (z.B. `SleepTimer`)
   - Alle Dateien aus dem ZIP hochladen (Ordnerstruktur beibehalten!)

2. **Build starten**
   - GitHub → Actions → „Build SleepTimer APK" → „Run workflow"
   - Nach ca. 3–5 Minuten: Artifacts → `SleepTimer-debug` → herunterladen

3. **APK auf das Gerät übertragen**
   - APK-ZIP entpacken → `app-debug.apk`
   - Per USB, AirDrop oder Google Drive auf das Xiaomi 13T Pro übertragen

4. **Installation auf dem Gerät**
   - Einstellungen → Datenschutz → Unbekannte Quellen → AN
   - APK-Datei öffnen → Installieren
   - Bei Sicherheitswarnung: „Trotzdem installieren"

---

## Methode 2: Android Studio (lokal)

1. **Projekt öffnen**
   - Android Studio starten
   - „Open an existing project" → ZIP entpacken → Ordner `SleepTimer` auswählen

2. **Gradle Sync abwarten**
   - Alle Abhängigkeiten werden automatisch heruntergeladen (~2–5 Min)

3. **APK erstellen**
   - Build → Build Bundle(s)/APK(s) → Build APK(s)
   - APK liegt unter: `app/build/outputs/apk/debug/app-debug.apk`

4. **Auf Gerät installieren**
   - USB-Debugging auf dem Xiaomi aktivieren:
     Einstellungen → Info → MIUI-Version 7x tippen → Entwickleroptionen → USB-Debugging AN
   - Gerät verbinden → In Android Studio: Run → Run 'app'

---

## ADB-Berechtigungen (für erweiterte Funktionen)

Folgende Features benötigen spezielle Berechtigungen, die per ADB vergeben werden müssen:

### Mobile Daten ausschalten
```bash
adb shell pm grant de.andre.sleeptimer android.permission.MODIFY_PHONE_STATE
```

### Flugmodus aktivieren
```bash
adb shell pm grant de.andre.sleeptimer android.permission.WRITE_SECURE_SETTINGS
```

### Ultra-Energiesparmodus aktivieren
(Wird automatisch mit WRITE_SECURE_SETTINGS freigeschaltet)

### Alle Berechtigungen auf einmal vergeben:
```bash
adb shell pm grant de.andre.sleeptimer android.permission.MODIFY_PHONE_STATE
adb shell pm grant de.andre.sleeptimer android.permission.WRITE_SECURE_SETTINGS
```

> **Hinweis:** ADB ist Teil der Android Platform Tools.
> Download: https://developer.android.com/studio/releases/platform-tools

---

## Xiaomi HyperOS 3.0 Besonderheiten

### Autostart erlauben
Einstellungen → Apps → SleepTimer → Autostart → AN

### Akkuverwaltung
Einstellungen → Apps → SleepTimer → Akku → Keine Einschränkungen

### Benachrichtigungen erlauben
Beim ersten Start der App → Berechtigung erlauben

---

## Funktionsübersicht

| Funktion | Berechtigung | Hinweis |
|---|---|---|
| Timer einstellen | – | Stunden + Minuten, Schnellauswahl |
| WLAN ausschalten | Automatisch | Ab Android 10: öffnet WiFi-Panel |
| Mobile Daten | ADB nötig | s.o. |
| Ton stummschalten | Automatisch | Klingel + Medien + Benachrichtigungen |
| Flugmodus | ADB nötig | s.o. |
| Energiesparmodus | ADB nötig | s.o. |
| Apps beenden | Automatisch | Hintergrund-Apps |
| Timer im Hintergrund | Automatisch | Läuft auch bei gesperrtem Bildschirm |

---

## Fehlerbehebung

**App startet nicht:**
→ Android Studio → Build → Clean Project → Rebuild

**Timer stoppt beim Sperren:**
→ Akkuverwaltung auf „Keine Einschränkungen" setzen (s.o.)

**Flugmodus/Mobile Daten funktionieren nicht:**
→ ADB-Berechtigungen vergeben (s.o.)
