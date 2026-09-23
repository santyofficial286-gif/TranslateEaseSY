# Hindi → English Translator (Android)

A complete, ready-to-build Android Studio project. Text **and** voice input, works offline after
the first run, targets Android 10+ (API 29).

## How to get the .apk

1. Unzip this project.
2. Open the folder in **Android Studio** (Arctic Fox or newer). It will sync Gradle automatically
   — make sure you're online the first time so it can download dependencies.
3. Connect a device/emulator (or just build without running), then:
   **Build → Build App Bundle(s) / APK(s) → Build APK(s)**
4. The finished file lands in `app/build/outputs/apk/debug/app-debug.apk`. Copy it to your phone
   and install (enable "install from unknown sources" if prompted).

For a signed release APK: **Build → Generate Signed Bundle / APK**, choose APK, and follow the
wizard to create/select a keystore.

## Option B: build it in the cloud with GitHub Actions (no local install needed)

This project includes `.github/workflows/build.yml`, which builds the debug APK automatically
whenever you push to GitHub — the whole build runs on GitHub's servers.

1. Create a free account at github.com if you don't have one.
2. Create a new repository (public or private, either works).
3. Upload this entire unzipped `HindiToEnglish` folder into that repo — easiest way: on the repo
   page, use **Add file → Upload files**, drag the whole folder in, and commit. (Make sure the
   hidden `.github` folder comes along — if your upload UI hides dot-folders, use GitHub Desktop
   or `git push` from a terminal instead, see below.)
4. Go to the **Actions** tab of your repo. A workflow run should start automatically (or click
   **Run workflow** to trigger it manually).
5. Wait ~3–5 minutes for it to finish (green checkmark).
6. Click into the finished run → scroll to **Artifacts** → download **HindiToEnglish-debug-apk**.
   That's a zip containing `app-debug.apk` — install it on your phone.

### If drag-and-drop upload won't include the `.github` folder
Use git from a terminal instead:
```
cd HindiToEnglish
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```
Then check the **Actions** tab as above.

## What's inside
- `MainActivity.kt` — mic button (Android SpeechRecognizer, Hindi locale) + text field, both feed
  into Google **ML Kit Translate**, which runs the Hindi↔English model **on-device** once
  downloaded, so translation keeps working without internet afterward.
- A speaker icon on the output card reads the English translation aloud (TextToSpeech).
- `activity_main.xml` — gradient background, rounded "card" input/output panels, pill-shaped
  Translate button.

## Notes
- The very first translation needs a Wi-Fi connection so ML Kit can download its language model
  (a few MB); after that it works offline.
- Voice input needs the device's Google app / speech services (present on virtually all Android
  10+ phones with Play Services).
- Want app icon/branding changed, a language-swap toggle, or history of past translations? Easy
  to add — just ask.
