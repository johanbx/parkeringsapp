# Parkeringsapp

Parkeringsapp är en app vars huvudsyfte är att få användaren att komma ihåg vart bilen är parkerad. Om du någonsin varit vid ett stort köpcentrum innan en stressigt innan julen helg förstår du vad jag pratar om.

## Några av huvudpunkterna
 - Sätt ut en knapp på kartan där bilen är parkerad
 - Sparar automatiskt platser (koordinater, adresser, tid, träffsäkerhet)
 - Integererat med Google Maps API
 - Inloggningar från Google Plus
 - Databas som ligger i molnet
 - Analytiska verktyg för att tracka användarevanor

### Några tekniker använda i appen
* [Firebase] - Firebase för analys, statistik, användareinloggningar och databashantering
* [Google Maps API] - Google Maps API som grund för kartan

   [Firebase]: <https://firebase.google.com/>
   [Google Maps API]: <https://developers.google.com/maps/>

## För utveckling

### Saker som finns att göra
 - Navigationsmenyn ska markera items när användare klickar på bakåtknappen
 - Parkeringskoordinator hämtas från närmaste adress, borde hämtas från nuvarande punkt
 - Analytiska verktyg för flera knapptryck i appen

### Funktioner som kan implementeras
 - Alarm på när perkerinstiden börjar ta slut
 - Auto-parkering
 - Förhindra att många dubletter sparas om användaren råkar komma åt parkeringsknappen

### Debugga firebase analytics i realtid
I Windows, Gå in i powershell och kör detta
```sh
PS C:\Users\wirle\AppData\Local\Android\Sdk\platform-tools> ./adb shell setprop debug.firebase.analytics.app com.example.wirle.parkeringsapp
```

För att avsluta
```sh
PS C:\Users\wirle\AppData\Local\Android\Sdk\platform-tools> ./adb shell setprop debug.firebase.analytics.app .none.
```

### För att få firebase att fungera i Android Studio (ett måste för nya utvecklingsmiljöer)
  1. Gå in på FireBase konsolen i webbläsaren
  2. Gå till project settings
  3. Lägg till nytt sha1 fingerprint
##### Vart hittar jag sha1 fingerprint?
  1. Öppna Android studio
  2. Klicka upp gradle fliken längst till vänster i Android Studio
  3. Gå till "Tasks->Android", högerklicka på "singingReport" och kör denna
  4. Klicka på gradle console längst ner i högerhörnet, där står nu sha1 nyckeln