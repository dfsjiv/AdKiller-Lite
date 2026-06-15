# AdKiller Lite MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an installable Android 8.0+ MVP that automatically clicks recognized ad-close accessibility nodes only inside user-selected apps.

**Architecture:** A single Android application module separates pure rule logic from Android adapters. Compose screens read repositories backed by Preferences DataStore, while `AdKillerAccessibilityService` delegates matching, cooldown decisions, delayed scheduling, and logging to focused components.

**Tech Stack:** Kotlin, Android SDK 35, Jetpack Compose Material 3, Navigation Compose, Preferences DataStore, Kotlin coroutines, JUnit 4, AndroidX Test, Gradle Kotlin DSL, GitHub Actions.

---

## File Structure

- `settings.gradle.kts`: plugin and repository configuration.
- `build.gradle.kts`: root plugin versions.
- `gradle/libs.versions.toml`: dependency versions and aliases.
- `app/build.gradle.kts`: Android application configuration and dependencies.
- `app/src/main/AndroidManifest.xml`: activity and accessibility service declarations.
- `app/src/main/res/xml/ad_killer_accessibility_service.xml`: accessibility event configuration.
- `app/src/main/java/io/github/adkillerlite/AdKillerApplication.kt`: application entry point and dependency container.
- `app/src/main/java/io/github/adkillerlite/MainActivity.kt`: Compose host and accessibility settings launcher.
- `app/src/main/java/io/github/adkillerlite/rules/KeywordMatcher.kt`: exact normalized keyword matching.
- `app/src/main/java/io/github/adkillerlite/rules/ClickSafetyPolicy.kt`: duplicate and cooldown decisions.
- `app/src/main/java/io/github/adkillerlite/accessibility/NodeFinder.kt`: bounded accessibility-tree traversal.
- `app/src/main/java/io/github/adkillerlite/accessibility/ClickScheduler.kt`: cancelable delayed clicks.
- `app/src/main/java/io/github/adkillerlite/accessibility/AdKillerAccessibilityService.kt`: service orchestration.
- `app/src/main/java/io/github/adkillerlite/data/Models.kt`: app rule, log, and dashboard models.
- `app/src/main/java/io/github/adkillerlite/data/SettingsRepository.kt`: allowlist and delay persistence contract.
- `app/src/main/java/io/github/adkillerlite/data/DataStoreSettingsRepository.kt`: Preferences DataStore implementation.
- `app/src/main/java/io/github/adkillerlite/data/StatsRepository.kt`: daily count and bounded-log contract.
- `app/src/main/java/io/github/adkillerlite/data/DataStoreStatsRepository.kt`: Preferences DataStore implementation.
- `app/src/main/java/io/github/adkillerlite/apps/InstalledAppsRepository.kt`: installed-app discovery.
- `app/src/main/java/io/github/adkillerlite/ui/AdKillerApp.kt`: navigation and shared Compose shell.
- `app/src/main/java/io/github/adkillerlite/ui/HomeScreen.kt`: status and summary.
- `app/src/main/java/io/github/adkillerlite/ui/AppPickerScreen.kt`: app allowlist selection.
- `app/src/main/java/io/github/adkillerlite/ui/AppRuleScreen.kt`: per-app delay configuration.
- `app/src/main/java/io/github/adkillerlite/ui/LogsScreen.kt`: recent event display.
- `app/src/test/java/io/github/adkillerlite/rules/KeywordMatcherTest.kt`: keyword tests.
- `app/src/test/java/io/github/adkillerlite/rules/ClickSafetyPolicyTest.kt`: safety tests.
- `app/src/test/java/io/github/adkillerlite/data/DailyStatsTest.kt`: date rollover tests.
- `.github/workflows/android-build.yml`: cloud test and debug APK build.
- `README.md`: setup, privacy, limitations, and APK instructions.

### Task 1: Bootstrap The Android Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/java/io/github/adkillerlite/MainActivity.kt`

- [ ] **Step 1: Create the Gradle wrapper and version catalog**

Use Gradle `8.9`, Android Gradle Plugin `8.7.3`, Kotlin `2.0.21`, compile/target SDK `35`, and minimum SDK `26`. Configure Java/Kotlin target `17`, Compose, unit tests, and Android resources.

- [ ] **Step 2: Add the minimal manifest and Compose activity**

Create `MainActivity` with a Material 3 surface that displays `广告杀手 Lite`. Do not add service behavior yet.

- [ ] **Step 3: Run the first build**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: `BUILD SUCCESSFUL` and `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 4: Commit**

```powershell
git add settings.gradle.kts build.gradle.kts gradle app
git commit -m "build: bootstrap Android application"
```

### Task 2: Implement Exact Keyword Matching

**Files:**
- Create: `app/src/test/java/io/github/adkillerlite/rules/KeywordMatcherTest.kt`
- Create: `app/src/main/java/io/github/adkillerlite/rules/KeywordMatcher.kt`

- [ ] **Step 1: Write the failing keyword tests**

```kotlin
class KeywordMatcherTest {
    private val matcher = KeywordMatcher()

    @Test fun matchesInitialKeywordsAfterNormalization() {
        assertEquals("跳过", matcher.match(" 跳过 "))
        assertEquals("关闭广告", matcher.match("关闭广告"))
        assertEquals("×", matcher.match(" × "))
    }

    @Test fun rejectsSubstringsAndUnrelatedText() {
        assertNull(matcher.match("关闭页面"))
        assertNull(matcher.match("跳过登录"))
        assertNull(matcher.match(null))
    }
}
```

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*KeywordMatcherTest"`

Expected: FAIL because `KeywordMatcher` does not exist.

- [ ] **Step 3: Implement the matcher**

```kotlin
class KeywordMatcher(
    private val keywords: Set<String> = setOf("跳过", "关闭", "关闭广告", "×"),
) {
    fun match(value: CharSequence?): String? {
        val normalized = value?.toString()?.trim()?.replace("\\s+".toRegex(), " ")
        return normalized?.takeIf(keywords::contains)
    }
}
```

- [ ] **Step 4: Verify GREEN and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*KeywordMatcherTest"`

Expected: PASS.

```powershell
git add app/src/main/java/io/github/adkillerlite/rules app/src/test/java/io/github/adkillerlite/rules
git commit -m "feat: add exact ad-close keyword matching"
```

### Task 3: Implement Click Safety Policy

**Files:**
- Create: `app/src/test/java/io/github/adkillerlite/rules/ClickSafetyPolicyTest.kt`
- Create: `app/src/main/java/io/github/adkillerlite/rules/ClickSafetyPolicy.kt`

- [ ] **Step 1: Write failing cooldown and duplicate tests**

```kotlin
class ClickSafetyPolicyTest {
    @Test fun rejectsDuplicateCandidateInsideCooldown() {
        val policy = ClickSafetyPolicy(cooldownMs = 2_000)
        val candidate = CandidateKey("app.pkg", 7, "跳过")
        assertTrue(policy.canSchedule(candidate, nowMs = 1_000))
        policy.recordClick(candidate, nowMs = 1_500)
        assertFalse(policy.canSchedule(candidate, nowMs = 2_000))
        assertTrue(policy.canSchedule(candidate, nowMs = 3_501))
    }

    @Test fun allowsDifferentPackage() {
        val policy = ClickSafetyPolicy(cooldownMs = 2_000)
        policy.recordClick(CandidateKey("a", 1, "关闭"), 1_000)
        assertTrue(policy.canSchedule(CandidateKey("b", 1, "关闭"), 1_100))
    }
}
```

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*ClickSafetyPolicyTest"`

Expected: FAIL because the safety types do not exist.

- [ ] **Step 3: Implement minimal policy**

```kotlin
data class CandidateKey(val packageName: String, val windowId: Int, val keyword: String)

class ClickSafetyPolicy(private val cooldownMs: Long = 2_000) {
    private val clickedAt = mutableMapOf<CandidateKey, Long>()

    fun canSchedule(key: CandidateKey, nowMs: Long): Boolean =
        nowMs - (clickedAt[key] ?: Long.MIN_VALUE) > cooldownMs

    fun recordClick(key: CandidateKey, nowMs: Long) {
        clickedAt[key] = nowMs
    }
}
```

- [ ] **Step 4: Verify GREEN and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*ClickSafetyPolicyTest"`

Expected: PASS.

```powershell
git add app/src/main/java/io/github/adkillerlite/rules app/src/test/java/io/github/adkillerlite/rules
git commit -m "feat: add automatic click safety policy"
```

### Task 4: Add Settings And Daily Statistics Persistence

**Files:**
- Create: `app/src/main/java/io/github/adkillerlite/data/Models.kt`
- Create: `app/src/main/java/io/github/adkillerlite/data/SettingsRepository.kt`
- Create: `app/src/main/java/io/github/adkillerlite/data/DataStoreSettingsRepository.kt`
- Create: `app/src/main/java/io/github/adkillerlite/data/StatsRepository.kt`
- Create: `app/src/main/java/io/github/adkillerlite/data/DataStoreStatsRepository.kt`
- Create: `app/src/test/java/io/github/adkillerlite/data/DailyStatsTest.kt`

- [ ] **Step 1: Write the failing daily rollover test**

```kotlin
class DailyStatsTest {
    @Test fun resetsCountWhenDateChanges() {
        val old = DailyStats("2026-06-14", 12)
        assertEquals(DailyStats("2026-06-15", 0), old.forDate("2026-06-15"))
    }

    @Test fun preservesCountForSameDate() {
        val stats = DailyStats("2026-06-15", 12)
        assertEquals(stats, stats.forDate("2026-06-15"))
    }
}
```

- [ ] **Step 2: Verify RED**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DailyStatsTest"`

Expected: FAIL because `DailyStats` does not exist.

- [ ] **Step 3: Implement models and repository contracts**

```kotlin
data class AppRule(val packageName: String, val enabled: Boolean = true, val delayMs: Long = 1_000)
data class DailyStats(val date: String, val count: Int) {
    fun forDate(today: String) = if (date == today) this else DailyStats(today, 0)
}
data class CloseLog(val timestampMs: Long, val packageName: String, val keyword: String, val success: Boolean)
```

Repositories expose `Flow<List<AppRule>>`, `ruleFor(packageName)`, `setRule`, `removeRule`, `incrementSuccess`, and a bounded recent-log flow. Persist app rules and logs as JSON strings in Preferences DataStore; clamp delays to `0..10_000`.

- [ ] **Step 4: Verify GREEN, run all unit tests, and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest`

Expected: PASS.

```powershell
git add app/src/main/java/io/github/adkillerlite/data app/src/test/java/io/github/adkillerlite/data
git commit -m "feat: persist app rules and daily statistics"
```

### Task 5: Add Installed App Discovery

**Files:**
- Create: `app/src/main/java/io/github/adkillerlite/apps/InstalledAppsRepository.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Define the app-list model and repository**

```kotlin
data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
)

interface InstalledAppsRepository {
    suspend fun load(includeSystem: Boolean): List<InstalledApp>
}
```

Implement with `PackageManager.queryIntentActivities` for `ACTION_MAIN` plus `CATEGORY_LAUNCHER`, exclude this app, sort by localized label, and filter system apps unless requested.

- [ ] **Step 2: Add package visibility query**

Declare a `<queries>` launcher intent in the manifest so Android 11+ exposes launchable apps.

- [ ] **Step 3: Build and commit**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: `BUILD SUCCESSFUL`.

```powershell
git add app/src/main/java/io/github/adkillerlite/apps app/src/main/AndroidManifest.xml
git commit -m "feat: discover selectable installed apps"
```

### Task 6: Implement Accessibility Node Discovery And Scheduling

**Files:**
- Create: `app/src/main/java/io/github/adkillerlite/accessibility/NodeFinder.kt`
- Create: `app/src/main/java/io/github/adkillerlite/accessibility/ClickScheduler.kt`
- Create: `app/src/main/java/io/github/adkillerlite/accessibility/AdKillerAccessibilityService.kt`
- Create: `app/src/main/res/xml/ad_killer_accessibility_service.xml`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Implement bounded node traversal**

`NodeFinder` traverses at most 500 nodes, matches both `text` and `contentDescription`, and resolves a clickable target by checking the matched node followed by at most 8 ancestors. It returns a candidate containing package name, window ID, keyword, and target node.

- [ ] **Step 2: Implement cancelable delayed click scheduling**

`ClickScheduler` holds one pending coroutine job. A new active package/window cancels the previous job. Before clicking, verify the active package and root window ID still match, then call `performAction(ACTION_CLICK)`.

- [ ] **Step 3: Add service orchestration**

The service:

- Reads the package rule and returns immediately when missing or disabled.
- Finds the first candidate.
- Checks `ClickSafetyPolicy`.
- Schedules the automatic click using the configured delay.
- Records successful clicks and failed/stale attempts.
- Cancels pending work in `onInterrupt` and `onDestroy`.

- [ ] **Step 4: Declare the service**

Configure `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOW_CONTENT_CHANGED`, `canRetrieveWindowContent=true`, a short notification timeout, and `android.permission.BIND_ACCESSIBILITY_SERVICE`.

- [ ] **Step 5: Build and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`

Expected: PASS and `BUILD SUCCESSFUL`.

```powershell
git add app/src/main/java/io/github/adkillerlite/accessibility app/src/main/res/xml app/src/main/AndroidManifest.xml
git commit -m "feat: add automatic accessibility ad closing"
```

### Task 7: Build Compose Screens

**Files:**
- Create: `app/src/main/java/io/github/adkillerlite/AdKillerApplication.kt`
- Modify: `app/src/main/java/io/github/adkillerlite/MainActivity.kt`
- Create: `app/src/main/java/io/github/adkillerlite/ui/AdKillerApp.kt`
- Create: `app/src/main/java/io/github/adkillerlite/ui/HomeScreen.kt`
- Create: `app/src/main/java/io/github/adkillerlite/ui/AppPickerScreen.kt`
- Create: `app/src/main/java/io/github/adkillerlite/ui/AppRuleScreen.kt`
- Create: `app/src/main/java/io/github/adkillerlite/ui/LogsScreen.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add the application dependency container**

Create repositories once in `AdKillerApplication` and expose them to the service and Compose host without adding a dependency-injection framework.

- [ ] **Step 2: Implement navigation and home**

Home displays accessibility enabled state, settings button, today's count, protected apps, and navigation actions. The settings button launches `Settings.ACTION_ACCESSIBILITY_SETTINGS`.

- [ ] **Step 3: Implement app picker and per-app rule**

App picker defaults to user apps and supports a show-system-apps switch. Selecting an app creates/removes its rule. Rule screen uses a slider from `0f..10f` with 100 steps and displays the chosen value to one decimal place.

- [ ] **Step 4: Implement logs**

Display newest-first close events with local time, package label when available, keyword, and success/skipped state.

- [ ] **Step 5: Build and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`

Expected: PASS and `BUILD SUCCESSFUL`.

```powershell
git add app/src/main/java/io/github/adkillerlite app/src/main/AndroidManifest.xml
git commit -m "feat: add app selection dashboard and logs"
```

### Task 8: Add GitHub Actions Cloud Build

**Files:**
- Create: `.github/workflows/android-build.yml`
- Create: `.gitignore`

- [ ] **Step 1: Add cloud build workflow**

```yaml
name: Android Build

on:
  push:
  pull_request:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
          cache: gradle
      - uses: gradle/actions/setup-gradle@v4
      - run: chmod +x gradlew
      - run: ./gradlew testDebugUnitTest assembleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: AdKiller-Lite-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

- [ ] **Step 2: Ignore local build files**

Ignore `.gradle/`, `.idea/`, `local.properties`, `**/build/`, and `.superpowers/`.

- [ ] **Step 3: Validate workflow syntax and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`

Expected: PASS and `BUILD SUCCESSFUL`.

```powershell
git add .github .gitignore
git commit -m "ci: build debug APK with GitHub Actions"
```

### Task 9: Document Usage, Privacy, And Limitations

**Files:**
- Create: `README.md`
- Create: `LICENSE`

- [ ] **Step 1: Write README**

Document:

- Accessibility-service purpose and fully automatic behavior.
- Installation and service-enabling steps.
- How to choose protected apps and configure delay.
- Privacy statement: all processing and logs remain local.
- Current exact-keyword limitations and no OCR/coordinate clicking.
- GitHub Actions APK download steps.
- Development build commands.

- [ ] **Step 2: Add Apache-2.0 license**

Use the standard Apache License 2.0 text and copyright line `2026 AdKiller Lite contributors`.

- [ ] **Step 3: Verify and commit**

Run: `.\gradlew.bat clean testDebugUnitTest assembleDebug`

Expected: PASS and `BUILD SUCCESSFUL`.

```powershell
git add README.md LICENSE
git commit -m "docs: add setup privacy and limitations"
```

### Task 10: Publish To GitHub And Verify Cloud APK

**Files:**
- No source changes expected.

- [ ] **Step 1: Create or connect the GitHub repository**

Create a public GitHub repository named `AdKiller-Lite`, add it as `origin`, and push `main`.

- [ ] **Step 2: Verify GitHub Actions**

Wait for the `Android Build` workflow. Inspect logs if any step fails, reproduce the failure locally where possible, and fix through a failing test or build reproduction before pushing another commit.

- [ ] **Step 3: Verify artifact**

Confirm the workflow succeeds and exposes an `AdKiller-Lite-debug` artifact containing `app-debug.apk`.

- [ ] **Step 4: Record final evidence**

Run:

```powershell
git status --short
git log --oneline --decorate -10
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

Expected: clean worktree, intentional commits, passing tests, and successful debug APK build.
