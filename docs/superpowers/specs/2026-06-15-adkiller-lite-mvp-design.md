# AdKiller Lite MVP Design

## Goal

Build an open-source Android utility that automatically closes identifiable ads in user-selected apps through an `AccessibilityService`.

The MVP targets Android 8.0 and later (API 26+) and is distributed as a debug APK through GitHub Actions. It is not intended for Google Play release in this phase.

## MVP Scope

The first version will:

- Run an accessibility service after the user enables it in Android settings.
- Operate only in apps explicitly selected by the user.
- List user-installed apps by default, with an option to show system apps.
- Detect accessibility nodes whose visible text or content description matches an ad-close keyword.
- Automatically click detected targets after an app-specific delay.
- Allow a delay from 0 to 10 seconds in 0.1-second increments, defaulting to 1 second.
- Apply background safety checks without requiring user confirmation.
- Record today's successful close count and a small recent-event log.
- Build a debug APK in GitHub Actions.

The MVP will not include coordinate-based clicking, screenshots, OCR, VPN behavior, root access, ad SDK modification, Room, or a general-purpose rule editor.

## User Experience

### Home

The home screen shows:

- Product name: `广告杀手 Lite`
- Accessibility service status: protecting or not enabled
- A button that opens Android accessibility settings
- Today's successful close count
- Selected protected apps
- Navigation to app selection and recent logs

### App Selection

The app picker:

- Shows user-installed launchable apps by default.
- Can optionally show system apps.
- Displays each app's icon, label, package name, enabled state, and configured delay.
- Allows the user to add or remove an app from the protected allowlist.

### App Rule

Each selected app has:

- Protection enabled switch
- Automatic click delay from 0 to 10 seconds

### Logs

The recent log shows timestamp, app label, detected keyword, and whether the click succeeded or was skipped by a safety check.

## Detection And Automatic Click Flow

1. Android sends a relevant accessibility event.
2. The service checks whether the source package is enabled in the allowlist.
3. The service scans the active window node tree for normalized keyword matches.
4. Initial keywords are `跳过`, `关闭`, `关闭广告`, and `×`.
5. The service resolves the matched node to a clickable node, preferring the node itself and then its clickable ancestors.
6. The safety guard rejects duplicate or stale candidates.
7. The service waits for the app-specific delay.
8. The service verifies that the same package and window are still active and that the target remains valid.
9. The service performs `ACTION_CLICK` automatically, without prompting the user.
10. Successful clicks update the daily counter and recent log.

If an app has no ad or no matching accessibility node, the service performs no action.

## Safety Guard

The MVP remains fully automatic but limits accidental clicks through:

- Allowlist-only operation.
- Exact normalized keyword matching rather than broad substring matching.
- Clickable-node or clickable-ancestor requirement.
- Package and active-window revalidation after the delay.
- Candidate deduplication.
- A per-package cooldown after a click.
- Cancellation of pending clicks when the active package or window changes.
- Bounded tree traversal to avoid excessive background work.

The standalone `×` keyword is supported, but only when exposed as a clickable accessibility node or within a clickable ancestor. The MVP does not guess screen coordinates.

## Architecture

The app uses a single Android application module with clear package boundaries:

- `ui`: Compose screens and navigation.
- `accessibility`: service lifecycle, node scanning, candidate scheduling, and clicking.
- `rules`: keyword matching and safety policy.
- `data`: DataStore-backed app rules, counters, and recent logs.
- `apps`: installed-app discovery and label/icon loading.

Core components:

- `AdKillerAccessibilityService`: receives events and coordinates detection.
- `NodeMatcher`: performs bounded node-tree searches and keyword normalization.
- `SafetyGuard`: validates candidates, cooldowns, and active-window state.
- `ClickScheduler`: manages delayed, cancelable automatic clicks.
- `SettingsRepository`: stores protected app package names and per-app delays.
- `StatsRepository`: stores daily counts and a bounded recent log.

## Persistence

Preferences DataStore stores:

- Whether each package is protected.
- Per-package automatic click delay in milliseconds.
- Whether system apps are visible in the picker.
- Current daily count and its date.
- A bounded recent log suitable for the MVP.

Room is deferred until statistics and log querying require structured storage.

## Permissions And Privacy

- The app requests only permissions required for its local functionality.
- Accessibility configuration declares only the event types and capabilities needed for window inspection and clicks.
- All detection, rules, counters, and logs remain on-device.
- No screen content, app list, or usage data is uploaded.
- The README clearly explains the accessibility-service purpose and limitations.

## Error Handling

- If accessibility is disabled, the home screen shows the disabled state and offers a settings shortcut.
- If installed-app loading fails, the picker displays an error and retry action.
- If a node becomes stale or a click fails, the event is logged without incrementing the successful-close count.
- DataStore read failures fall back to an empty allowlist and safe defaults.
- The service avoids acting when package or window identity cannot be verified.

## Testing

Unit tests cover:

- Keyword normalization and exact matching.
- Clickable ancestor resolution.
- Delay bounds and defaults.
- Cooldown and duplicate-candidate behavior.
- Daily counter rollover.

Instrumentation or service-level tests cover:

- No action outside the allowlist.
- Automatic click scheduling for a valid allowlisted candidate.
- Cancellation after package or window change.
- Successful click statistics and log updates.

Manual device verification covers:

- Enabling and disabling the accessibility service.
- Selecting apps from the installed-app picker.
- Configuring per-app delays.
- Closing a test ad-like screen automatically.
- Confirming normal screens without matching nodes are untouched.

## Build And Delivery

- Kotlin and Jetpack Compose
- Gradle Kotlin DSL and Gradle Wrapper
- Minimum SDK 26
- Current stable compile and target SDK selected during implementation
- GitHub Actions workflow runs unit tests and `assembleDebug`
- Debug APK is uploaded as a workflow artifact

## Acceptance Criteria

The MVP is complete when:

1. A user can install the GitHub Actions debug APK on Android 8.0 or later.
2. The app reports whether its accessibility service is enabled.
3. The user can select an installed app and configure a 0-to-10-second delay.
4. In a selected app, a valid clickable node labeled with an initial keyword is automatically clicked after the configured delay.
5. The service does nothing in unselected apps and does nothing when no keyword matches.
6. Pending clicks are canceled when the active app or window changes.
7. Successful clicks update today's count and appear in recent logs.
8. Unit tests and the GitHub Actions build pass.
