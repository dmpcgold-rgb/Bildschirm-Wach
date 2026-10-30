# Bildschirm Wach — Android App

Schützt das Display vor dem automatischen Abschalten beim Serienschauen und anderen Tätigkeiten, auch beim Wechsel in andere Apps. Schaltet den systemweiten Timeout temporär auf den maximalen Wert und stellt den vorherigen Wert beim Ausschalten wieder her.

## Funktionen

- Ein-/Aus-Schalter; läuft weiter, wenn die App im Hintergrund ist
- 2 Stunden, 4 Stunden oder manuell
- Live-Countdown (HH:MM:SS), Laufzeit seit Aktivierung und geplante Ausschaltuhrzeit
- Zeitmessung bleibt beim App-Wechsel erhalten; Anzeige aktualisiert sich, sobald die App geöffnet ist
- Android-Schnelleinstellungen-Kachel
- Wiederherstellung nach Neustart oder Update, soweit die Systemberechtigung verfügbar bleibt
- Keine Werbung, kein Netzwerkzugriff, keine gefährlichen Hintergrunddienste

## APK

**Actions → Android APK → letzter erfolgreicher Lauf → Artifacts → Bildschirm-Wach-Debug-APK**. ZIP entpacken, `app-debug.apk` installieren.

Bei der ersten Verwendung **Systemeinstellungen ändern** für Bildschirm Wach zulassen. Im Anschluss auf **Bildschirm wach halten** drücken und zur Serie wechseln.

**Einschränkungen:** Android/Samsung kann Systemwerte begrenzen; Energiesparfunktionen, automatische Helligkeit, Gerätesicherheitsrichtlinien und der manuelle Sperrknopf haben Vorrang. Bei den 2- und 4-Stunden-Alarmen kann Android durch Doze verzögert beenden. Der maximale Timeoutwert beträgt ~24,85 Tage, also kein tatsächlich unendlicher Zeitraum. Test auf dem Gerät erforderlich.

Die GitHub-Action baut eine installierbare **Debug-APK** mit einem ephemeren Android-Debug-Schlüssel. Für dauerhaft updatefähige Release-APKs muss ein stabiler Signaturschlüssel sicher eingerichtet werden; Debug-APKs verschiedener Läufe sind nicht zuverlässig übereinander installierbar.
