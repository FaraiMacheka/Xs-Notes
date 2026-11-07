# Noughts & Crosses (Android)

A free, ad-free noughts and crosses game for Android: two players on one phone, or vs the computer (Easy, Medium, Hard).
Spec: https://claude.ai/artifact/Ncq5FsdpDEzrg8YyDRSMjF

## Project layout

| Path | What |
| --- | --- |
| `game/` | Pure Kotlin game logic: board, rules, AI (minimax). No Android dependencies. |
| `app/` | Android app: Jetpack Compose UI, ViewModels, settings (DataStore). |
| `docs/privacy-policy.html` | Privacy policy, served by GitHub Pages from `/docs` on `main`. |

## Build and test

Open the folder in Android Studio, or from a terminal:

```
gradlew :game:test :app:testDebugUnitTest   # unit tests
gradlew :app:assembleDebug                   # debug APK -> app/build/outputs/apk/debug/
gradlew :app:installDebug                    # install on a connected phone or emulator
```

## Release build for Google Play

1. Create an upload key once (keep the file and passwords backed up somewhere safe; losing it means a support request to Google):

   ```
   keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```

2. Copy `keystore.properties.example` to `keystore.properties` and fill in the passwords. Both files are git-ignored.
3. Build the bundle: `gradlew :app:bundleRelease` -> `app/build/outputs/bundle/release/app-release.aab`
4. Upload the `.aab` in Play Console and enrol in Play App Signing.
5. Bump `versionCode` (and `versionName`) in `app/build.gradle.kts` for every upload.

## Before the first release

- [x] Privacy policy hosted on GitHub Pages: https://faraimacheka.github.io/Xs-Notes/privacy-policy.html (used in the app's Settings screen)
- [ ] Paste that privacy policy URL into Play Console (App content > Privacy policy).
- [ ] Confirm the package ID `za.co.stripedape.noughtscrosses`; it cannot change after publishing.
- [ ] Store assets: 512x512 icon, 1024x500 feature graphic, 2+ phone screenshots.
