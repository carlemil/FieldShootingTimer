# Release: Bump Minor Version and Deploy to Google Play Test Track

**Optional argument:** `<liveVersion>` — the version currently live on Google Play (e.g., `1.10.0`). If provided, release notes will be based on changes since git tag `v<liveVersion>`.

Perform the following steps in order. Stop and report if any step fails or a prerequisite is missing.

---

## Step 1 — Check prerequisites

### 1a. keystore.properties
Check if `keystore.properties` exists in the project root. If it does **not** exist, stop and tell the user to create it with the following format (the `keystore` file already exists at the project root):

```
storeFile=keystore
storePassword=<your-keystore-password>
keyAlias=<your-key-alias>
keyPassword=<your-key-password>
```

Tell the user this file is already gitignored (`.gitignore` excludes `*.keystore` and `*.jks`, but they should also add `keystore.properties` to `.gitignore` if not already there).

### 1b. play-account.json
Check if `play-account.json` exists in the project root. If it does **not** exist, stop and tell the user to:
1. Go to Google Play Console → Setup → API access
2. Link a Google Cloud project and create a service account with "Release manager" role
3. Download the JSON key and save it as `play-account.json` in the project root
4. Add `play-account.json` to `.gitignore`

### 1c. Gradle Play Publisher plugin
Read `app/build.gradle.kts` and check if `com.github.triplet.play` is in the `plugins {}` block.

If it is **not** present, add it now:
- In the `plugins {}` block, add: `id("com.github.triplet.play") version "4.0.0"` (4.0.0 is the first release that supports AGP 9.x; 3.x is AGP 8-only)
- After the closing `}` of the `android {}` block (before `dependencies {}`), add:

```kotlin
play {
    serviceAccountCredentials.set(rootProject.file("play-account.json"))
    track.set("internal")
    defaultToAppBundles.set(true)
}
```

### 1d. Signing config
Read `app/build.gradle.kts` and check if a `signingConfigs` block exists inside `android {}`.

If it is **not** present, add the following:

After the line `val appVersionName = "..."`, add:
```kotlin
val keystoreProperties = java.util.Properties().also { props ->
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { props.load(it) }
}
```

Inside the `android {}` block, before `defaultConfig`, add:
```kotlin
    signingConfigs {
        create("release") {
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile", "keystore"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }
```

Inside the `buildTypes { release { ... } }` block, add:
```kotlin
            signingConfig = signingConfigs.getByName("release")
```

---

## Step 2 — Bump the version

Read `app/build.gradle.kts` and find:
```
val appVersionCode = <N>
val appVersionName = "<X>.<Y>.<Z>"
```

Compute new values:
- `newVersionCode` = N + 1
- Determine the new version name based on the current value:
  - **If the current name has a `-candidate-NNN` suffix** (e.g., `"1.10.0-candidate-001"`):
    - Strip the suffix and use the base version as-is (e.g., `"1.10.0"`)
  - **If the current name does NOT have a candidate suffix** (e.g., `"1.9.0"`):
    - Parse X, Y, Z from the version. Set new version = `"<X>.<Y+1>.0"`

Update the file:
- Replace `val appVersionCode = <N>` with `val appVersionCode = <newVersionCode>`
- Replace `val appVersionName = "<current>"` with `val appVersionName = "<newVersionName>"`

Also update the release notes comment block (lines starting with `// ---- Generate release notes ----`) to reflect the new version range. Change the comment to reference the previous version and the new version name.

Show the user the old and new version strings before continuing.

---

## Step 3 — Generate release notes

### 3a. Determine the last released version

**If `<liveVersion>` was provided as an argument:** Use `v<liveVersion>` as the baseline tag. Verify the tag exists with `git tag -l v<liveVersion>`. If the tag doesn't exist, warn the user and ask whether to create it on a specific commit or fall back to other methods.

**Otherwise, use git tags.** Each release is tagged `v<version>` (e.g., `v1.10.0`). Find the most recent release tag:
```
git describe --tags --abbrev=0 --match "v*" 2>/dev/null
```

**Verification (when no argument was given): cross-check with Google Play.** Fetch the Play Store listing and extract the live version to verify the tag matches what's actually published:
```
curl -s "https://play.google.com/store/apps/details?id=se.kjellstrand.fieldshootingtimer&hl=en-US" | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | sort -u
```
Look through the extracted versions for one that matches the app's version pattern (starts with `1.`). If the Play Store version differs from the latest git tag, tell the user and ask which to use as the baseline.

**Fallback:** If no tags exist, fall back to finding the previous "Bump version" commit:
```
git log --grep="^Bump version" --format="%H" -1
```

### 3b. Get commits since the last release
Using the tag or commit found in 3a, get the commit log:
```
git log --oneline v<lastVersion>..HEAD
```

If using the fallback commit hash:
```
git log --oneline <commitHash>..HEAD
```

If nothing works, use the last 20 commits:
```
git log --oneline -20
```

### 3c. Write release notes file
Based on the commits, write user-friendly release notes to:
```
app/src/main/play/release-notes/sv-SE/default.txt
```

Create parent directories if they don't exist.

Rules for the release notes:
- **Write in Swedish**
- **Maximum 500 characters** (Google Play hard limit — count carefully)
- Focus on what the **user experiences**, not what the dev team did
- Short, friendly language — no technical jargon, no commit hashes, no branch names
- Use a bullet list with short lines if there are multiple changes
- If there's nothing user-visible in the commits, write something like "Felkorrigeringar och prestandaförbättringar."
- Show the generated text to the user before continuing

---

## Step 4 — Build the signed AAB

Run:
```
./gradlew :app:bundleProdRelease --no-daemon
```

Wait for it to complete. If it fails, show the error output and stop.

The built AAB will be at: `app/build/outputs/bundle/prodRelease/app-prod-release.aab`

---

## Step 5 — Upload to Google Play internal test track

Run:
```
./gradlew :app:publishProdReleaseBundle --no-daemon
```

Wait for it to complete. If it fails, show the error output and stop.

---

## Step 6 — Commit and tag the release

Stage and commit the version and release notes:
```
git add app/build.gradle.kts
git add app/src/main/play/release-notes/sv-SE/default.txt
git commit -m "Bump version to <newVersionName> (build <newVersionCode>)"
```

Then create a git tag for this release:
```
git tag v<newVersionName>
```

This tag will be used by future releases to determine what changed since this version.

---

## Step 7 — Summary

Report:
- Previous version → New version (e.g., `1.7.9 (16) → 1.8.0 (17)`)
- Git tag created (e.g., `v1.8.0`)
- Upload status (success/failure)
- The release notes that were published
- Reminder to promote the build from the internal test track in Google Play Console when ready
- Reminder to push the tag with `git push origin v<newVersionName>`
