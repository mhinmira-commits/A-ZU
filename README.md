# A-ZU — Custom Android Launcher

Kotlin + Jetpack Compose launcher, API 26–34, tuned for low-end
hardware (Redmi A2+ / Android Go). Home screen + app drawer only —
no vault, no extra features in this core build.

## Structure

```
A-ZU/
  settings.gradle.kts, build.gradle.kts, gradle.properties
  app/build.gradle.kts                 # module-level config
  app/src/main/AndroidManifest.xml
  app/src/main/java/com/mahin/azu/
    MainActivity.kt                    # HOME launcher, NavHost home -> drawer
    AzuApp.kt                          # Application + Coil RGB_565 ImageLoader
    data/model/AppModel.kt
    data/repository/AppRepository.kt   # PackageManager query, icon LRU cache
    data/receiver/PackageChangeReceiver.kt
    ui/viewmodel/LauncherViewModel.kt  # combine(apps, query) on Default
    ui/screens/HomeScreen.kt
    ui/screens/AppDrawerScreen.kt      # LazyVerticalGrid, search, long-press
    ui/theme/Theme.kt
    ui/theme/Color.kt
  app/src/main/res/values/strings.xml, themes.xml
```

Package: `com.mahin.azu` — applicationId `com.mahin.azu`, display name `A-ZU`.

## Use it

**Android Studio (recommended):** Open this folder, wait for Gradle sync
(JDK 17, SDK API 34), Run on a device, press Home → A-ZU → Always.

**Phone-only / browser-based:** GitHub Actions is included — `.github/workflows/build-apk.yml`
is in this folder. Create a repo, upload this whole folder (including the hidden
`.github` folder), push, and download the APK from Actions → Artifacts
(`A-ZU-core-debug-apk` / `A-ZU-core-release-apk`). No `gradlew` file
is included, so the workflow uses `gradle assembleDebug assembleRelease`
(with `gradle/actions/setup-gradle`), not `./gradlew`.

**Local Gradle (if you have a terminal):**
```
gradle assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Performance notes (Redmi A2+)

- `LazyVerticalGrid`, debounced search (250 ms), filtering on
  `Dispatchers.Default`.
- Coil `RGB_565`, 15% memory cache, 32 MB disk cache.
- Icon LRU capped at 120, no background services.
- Release: minify/shrink OFF, debug-signed so the APK installs.
  Turn R8 on only after a debug build works.
