# Parkeringsapp

Parkeringsapp är en app vars huvudsyfte är att få användaren att komma ihåg vart bilen är parkerad. Om du någonsin varit vid ett stort köpcentrum innan en stressigt innan julen helg förstår du vad jag pratar om.

## Några av huvudpunkterna
   - Spara gps koordinater vart bilen är parkerad
   - Alarm när parkerinstiden börjar ta slut
   - Spara parkeringsplatser och parkeringstider

### Några tekniker använda i appen
* [Firebase] - Firebase för analys och statistik

   [Firebase]: <https://firebase.google.com/>

### För utveckling
För att debugga firebase analytics i realtid, gå in i powershell och kör denna
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