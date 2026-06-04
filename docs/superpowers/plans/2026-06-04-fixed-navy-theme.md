# Fixed Navy Theme (Remove Dynamic Color) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax. This is a removal across a dependency chain (preference → VM → Settings UI → MainActivity → theme); make all edits, compile once, then verify on the emulator.

**Goal:** Remove Android-12 dynamic color and commit to the fixed navy brand; keep Light/Dark theme.

**Architecture:** Delete the `dynamicColor` preference, its VM/Settings wiring, and the Material-You branch in `BudgetTrackerTheme`. The fixed navy `DarkColors`/`LightColors` become the only schemes. No data migration (old stored value ignored).

**Tech Stack:** Compose Material 3, DataStore.

Spec: `docs/superpowers/specs/2026-06-04-fixed-navy-theme-design.md`.

---

### Task 1: Remove the `dynamicColor` preference

**Files:** Modify `app/src/main/java/com/example/budgettracker/data/repository/PreferencesRepository.kt`

- [ ] **Step 1:** Delete the `import androidx.datastore.preferences.core.booleanPreferencesKey` line (no other boolean prefs remain).
- [ ] **Step 2:** Delete the `dynamicColor` flow line: `val dynamicColor: Flow<Boolean> = dataStore.data.map { it[DYNAMIC_COLOR] ?: false }`.
- [ ] **Step 3:** Delete the `setDynamicColor` function:
```kotlin
    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }
```
- [ ] **Step 4:** Delete the key line `val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")` from the `companion object`. Update the class KDoc that lists prefs to drop "dynamic color".

### Task 2: Remove `dynamicColor` from SettingsViewModel

**Files:** Modify `app/src/main/java/com/example/budgettracker/ui/screens/settings/SettingsViewModel.kt`

- [ ] **Step 1:** Delete the `dynamicColor` StateFlow:
```kotlin
    val dynamicColor: StateFlow<Boolean> = preferences.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
```
- [ ] **Step 2:** Delete the setter line `fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { preferences.setDynamicColor(enabled) }`.

### Task 3: Remove the Dynamic-color row from Settings UI

**Files:** Modify `app/src/main/java/com/example/budgettracker/ui/screens/settings/SettingsScreen.kt`

- [ ] **Step 1:** Delete `val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()`.
- [ ] **Step 2:** In the `SettingsSection("Appearance")` block, delete the line:
```kotlin
            SwitchTile("Dynamic color", "Use the system palette (Android 12+)", dynamicColor) { viewModel.setDynamicColor(it) }
```
leaving the Theme and Density `SettingTile`s.
- [ ] **Step 3:** Delete the now-unused `SwitchTile` composable function and the `import androidx.compose.material3.Switch` line.

### Task 4: Remove the Material-You branch in the theme

**Files:** Modify `app/src/main/java/com/example/budgettracker/ui/theme/Theme.kt`

- [ ] **Step 1:** Remove the `dynamicColor: Boolean = false,` parameter from `BudgetTrackerTheme`.
- [ ] **Step 2:** Replace the `when { … }` color-scheme selection with:
```kotlin
    val colorScheme = if (darkTheme) DarkColors else LightColors
```
- [ ] **Step 3:** Delete the now-unused imports: `androidx.compose.material3.dynamicDarkColorScheme`, `androidx.compose.material3.dynamicLightColorScheme`, `android.os.Build`, `androidx.compose.ui.platform.LocalContext`. Update the param KDoc comment (drop the dynamic-color mention).

### Task 5: Stop passing `dynamicColor` from MainActivity

**Files:** Modify `app/src/main/java/com/example/budgettracker/MainActivity.kt`

- [ ] **Step 1:** Delete the line `val dynamicColor by preferences.dynamicColor.collectAsStateWithLifecycle(initialValue = false)`.
- [ ] **Step 2:** Remove the `dynamicColor = dynamicColor,` argument from the `BudgetTrackerTheme(...)` call (keep `darkTheme` and `densityMode`).

### Task 6: Compile, verify, docs, commit

**Files:** Modify `CLAUDE.md`

- [ ] **Step 1: Compile.** Run: `./gradlew :app:compileDebugKotlin` — expect BUILD SUCCESSFUL, with no unresolved references to `dynamicColor` / `setDynamicColor` / `DYNAMIC_COLOR`.
- [ ] **Step 2: Confirm no stragglers.** Run: `grep -rn "dynamicColor\|DYNAMIC_COLOR\|dynamic.ColorScheme" app/src` — expect no matches.
- [ ] **Step 3: Update `CLAUDE.md`** — in the design-essentials, replace the "hybrid … Settings toggle for Android-12+ dynamic color" wording with a note that the app commits to the fixed navy brand (dynamic color removed: the gradient chrome can't follow Material You).
- [ ] **Step 4: Build + emulator verify.** `./gradlew :app:assembleDebug`, install, open Settings → Appearance shows only **Theme** + **Density** (no Dynamic color); switch Theme Light↔Dark and confirm the navy brand renders consistently with no wallpaper tint anywhere.
- [ ] **Step 5: Tests + commit.** `./gradlew :app:testDebugUnitTest` (green); commit the spec + plan + change on `feat/fixed-navy-theme`; push; open the PR.

---

## Self-review

- **Spec coverage:** §changes 1→Task 1 (preference), 2→Task 5 (MainActivity), 3→Task 3 (SettingsScreen), 4→Task 2 (SettingsViewModel), 5→Task 1 (preference), 6→Task 6 (CLAUDE.md); theme branch → Task 4. "No migration" honored (key removed, old value ignored). Testing section → Task 6 Steps 1/4/5.
- **Placeholder scan:** none — exact code/commands throughout.
- **Type/symbol consistency:** removed symbols (`dynamicColor`, `setDynamicColor`, `DYNAMIC_COLOR`, `SwitchTile`) are deleted at every reference site found in the audit; the grep in Task 6 Step 2 is the consistency backstop.
