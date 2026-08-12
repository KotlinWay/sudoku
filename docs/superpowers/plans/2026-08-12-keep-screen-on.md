# Keep Screen On Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить включённую по умолчанию настройку, удерживающую экран включённым только во время активной партии судоку.

**Architecture:** Новый `boolean` проходит через существующие `Prefs` и MVI экрана настроек. Чистый `ScreenPolicy` решает, нужен ли оконный флаг для текущей фазы, а `GameActivity` устанавливает или снимает `FLAG_KEEP_SCREEN_ON` при возобновлении, отрисовке состояния и уходе с экрана.

**Tech Stack:** Java 11, Android platform API 23-36, XML layouts/resources, SharedPreferences, собственный MVI, JUnit 4.13.2.

## Global Constraints

- Только Java; Kotlin, AndroidX, Material и новые runtime-зависимости запрещены.
- В манифесте остаётся ноль разрешений; wake lock и фоновые сервисы не используются.
- Новые строки сразу добавляются в `values/strings.xml` и `values-ru/strings.xml`.
- В пользовательских строках запрещено длинное тире `—`.
- Настройка включена по умолчанию, включая существующие установки без нового ключа.
- Экран удерживается только в фазе `GameState.Phase.PLAYING` и только пока виден `GameActivity`.
- В фазах `GENERATING`, `PAUSED`, `WON`, `LOST` действует системный тайм-аут.
- Значение переключателя рисуется только из `SettingsState` через `SettingsActivity.render()`.
- JVM-тесты обязательны для редьюсера и чистой политики фаз.
- Перед handoff UI проверяется при `font_scale 1.8` и `864x2340`, после чего обязательно возвращаются `font_scale 1.0` и `wm size reset`.
- Полная проверка Gradle выполняется только с `--rerun-tasks`; `installDebug` запрещён.
- Все команды `adb` используют только явный serial `emulator-5554`.

---

## File Map

**Create:**

- `app/src/test/java/info/javaway/sudoku/ui/settings/SettingsReducerTest.java` - контракт нового MVI-перехода.
- `app/src/test/java/info/javaway/sudoku/ui/settings/SettingsResourcesTest.java` - наличие переключателя и точной копии в обеих локалях.
- `app/src/main/java/info/javaway/sudoku/ui/game/ScreenPolicy.java` - чистое правило удержания по настройке и фазе.
- `app/src/test/java/info/javaway/sudoku/ui/game/ScreenPolicyTest.java` - матрица фаз политики.

**Modify:**

- `app/src/main/java/info/javaway/sudoku/settings/Prefs.java` - ключ, `keepScreenOn()` и `setKeepScreenOn(boolean)`.
- `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsState.java` - четвёртая настройка в иммутабельном состоянии.
- `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsAction.java` - `KeepScreenOnToggled`.
- `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsEffect.java` - `SaveKeepScreenOn`.
- `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsReducer.java` - идемпотентный переход и эффект сохранения.
- `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsActivity.java` - чтение, binding, render и немедленная запись настройки.
- `app/src/main/res/layout/activity_settings.xml` - третий переключатель раздела «Игра».
- `app/src/main/res/values/strings.xml` - английские название и пояснение.
- `app/src/main/res/values-ru/strings.xml` - русские название и пояснение.
- `app/src/main/java/info/javaway/sudoku/ui/game/GameActivity.java` - применение оконного флага по жизненному циклу и фазе.

---

### Task 1: Модель, хранение и редьюсер настройки

**Files:**

- Create: `app/src/test/java/info/javaway/sudoku/ui/settings/SettingsReducerTest.java`
- Modify: `app/src/main/java/info/javaway/sudoku/settings/Prefs.java:8-51`
- Modify: `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsState.java:5-38`
- Modify: `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsAction.java:5-29`
- Modify: `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsEffect.java:13-45`
- Modify: `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsReducer.java:10-32`
- Modify: `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsActivity.java:53-55`

**Interfaces:**

- Produces: `boolean Prefs.keepScreenOn()` with default `true`.
- Produces: `void Prefs.setKeepScreenOn(boolean value)`.
- Produces: `SettingsState.of(boolean candidates, boolean sound, boolean keepScreenOn, Theme theme)`.
- Produces: `SettingsState.keepScreenOn(boolean value)`.
- Produces: `SettingsAction.KeepScreenOnToggled(boolean value)`.
- Produces: `SettingsEffect.SaveKeepScreenOn(boolean value)`.
- Produces: a compiling initial `SettingsState` construction in `SettingsActivity`.

- [ ] **Step 1: Write the failing reducer tests**

Create `SettingsReducerTest.java`:

```java
package info.javaway.sudoku.ui.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import info.javaway.sudoku.settings.Theme;
import info.javaway.sudoku.ui.mvi.Update;

public class SettingsReducerTest {

    private static final SettingsReducer REDUCER = new SettingsReducer();

    private static SettingsState state(boolean keepScreenOn) {
        return SettingsState.of(false, true, keepScreenOn, Theme.DARK);
    }

    @Test public void удержаниеЭкранаВключаетсяИНеМеняетОстальныеНастройки() {
        Update<SettingsState, SettingsEffect> update = REDUCER.reduce(
                state(false), new SettingsAction.KeepScreenOnToggled(true));

        assertTrue(update.state.keepScreenOn);
        assertFalse(update.state.candidates);
        assertTrue(update.state.sound);
        assertEquals(Theme.DARK, update.state.theme);
        assertEquals(1, update.effects.size());
        assertTrue(update.effects.get(0) instanceof SettingsEffect.SaveKeepScreenOn);
        assertTrue(((SettingsEffect.SaveKeepScreenOn) update.effects.get(0)).value);
    }

    @Test public void удержаниеЭкранаВыключаетсяИПроситСохранитьFalse() {
        Update<SettingsState, SettingsEffect> update = REDUCER.reduce(
                state(true), new SettingsAction.KeepScreenOnToggled(false));

        assertFalse(update.state.keepScreenOn);
        assertEquals(1, update.effects.size());
        assertFalse(((SettingsEffect.SaveKeepScreenOn) update.effects.get(0)).value);
    }

    @Test public void повторноеЗначениеНичегоНеМеняетИНеПишет() {
        SettingsState before = state(true);

        Update<SettingsState, SettingsEffect> update = REDUCER.reduce(
                before, new SettingsAction.KeepScreenOnToggled(true));

        assertSame(before, update.state);
        assertTrue(update.effects.isEmpty());
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\gradlew testDebugUnitTest --tests "info.javaway.sudoku.ui.settings.SettingsReducerTest" --rerun-tasks
```

Expected: compilation fails because `keepScreenOn`, `KeepScreenOnToggled` and
`SaveKeepScreenOn` do not exist yet.

- [ ] **Step 3: Add the preference API with an enabled default**

Add to `Prefs`:

```java
private static final String KEEP_SCREEN_ON = "keep_screen_on";

public boolean keepScreenOn() {
    return preferences.getBoolean(KEEP_SCREEN_ON, true);
}

public void setKeepScreenOn(boolean value) {
    preferences.edit().putBoolean(KEEP_SCREEN_ON, value).apply();
}
```

Update its class comment to describe three game settings and the enabled default.

- [ ] **Step 4: Carry the value through SettingsState**

Use this state shape and copy method:

```java
public final boolean candidates;
public final boolean sound;
public final boolean keepScreenOn;
public final Theme theme;

private SettingsState(boolean candidates, boolean sound, boolean keepScreenOn, Theme theme) {
    this.candidates = candidates;
    this.sound = sound;
    this.keepScreenOn = keepScreenOn;
    this.theme = theme;
}

public static SettingsState of(boolean candidates, boolean sound,
                               boolean keepScreenOn, Theme theme) {
    return new SettingsState(candidates, sound, keepScreenOn, theme);
}

public SettingsState candidates(boolean value) {
    return new SettingsState(value, sound, keepScreenOn, theme);
}

public SettingsState sound(boolean value) {
    return new SettingsState(candidates, value, keepScreenOn, theme);
}

public SettingsState keepScreenOn(boolean value) {
    return new SettingsState(candidates, sound, value, theme);
}

public SettingsState theme(Theme value) {
    return new SettingsState(candidates, sound, keepScreenOn, value);
}
```

- [ ] **Step 5: Update the existing SettingsActivity constructor call**

The changed `SettingsState.of(...)` signature must compile within this task. Replace the
initial state construction with:

```java
store = new Store<>(
        SettingsState.of(prefs.candidates(), prefs.sound(),
                prefs.keepScreenOn(), prefs.theme()),
        new SettingsReducer());
```

- [ ] **Step 6: Add the action and effect**

Add to `SettingsAction`:

```java
public static final class KeepScreenOnToggled extends SettingsAction {
    public final boolean value;

    public KeepScreenOnToggled(boolean value) {
        this.value = value;
    }
}
```

Add to `SettingsEffect`:

```java
public static final class SaveKeepScreenOn extends SettingsEffect {
    public final boolean value;

    public SaveKeepScreenOn(boolean value) {
        this.value = value;
    }
}
```

- [ ] **Step 7: Add the idempotent reducer branch**

Place it after `SoundToggled`:

```java
if (action instanceof SettingsAction.KeepScreenOnToggled) {
    boolean value = ((SettingsAction.KeepScreenOnToggled) action).value;
    if (state.keepScreenOn == value) return Update.state(state);
    return Update.of(state.keepScreenOn(value),
            new SettingsEffect.SaveKeepScreenOn(value));
}
```

- [ ] **Step 8: Run the focused test and verify GREEN**

Run the same focused Gradle command. Expected: `SettingsReducerTest` passes.

- [ ] **Step 9: Commit the domain slice**

```powershell
git add -- app/src/main/java/info/javaway/sudoku/settings/Prefs.java app/src/main/java/info/javaway/sudoku/ui/settings/SettingsState.java app/src/main/java/info/javaway/sudoku/ui/settings/SettingsAction.java app/src/main/java/info/javaway/sudoku/ui/settings/SettingsEffect.java app/src/main/java/info/javaway/sudoku/ui/settings/SettingsReducer.java app/src/main/java/info/javaway/sudoku/ui/settings/SettingsActivity.java app/src/test/java/info/javaway/sudoku/ui/settings/SettingsReducerTest.java
git commit -m "Добавил настройку удержания экрана"
```

---

### Task 2: Переключатель и локализованный текст

**Files:**

- Create: `app/src/test/java/info/javaway/sudoku/ui/settings/SettingsResourcesTest.java`
- Modify: `app/src/main/res/layout/activity_settings.xml:21-54`
- Modify: `app/src/main/res/values/strings.xml:79-89`
- Modify: `app/src/main/res/values-ru/strings.xml:80-90`
- Modify: `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsActivity.java:20-60,105-169`

**Interfaces:**

- Consumes: `Prefs.keepScreenOn()`, `Prefs.setKeepScreenOn(boolean)`.
- Consumes: MVI types introduced in Task 1.
- Produces: view ID `R.id.keep_screen_on`.
- Produces: strings `R.string.keep_screen_on` and `R.string.keep_screen_on_hint`.

- [ ] **Step 1: Write the failing resource contract test**

Create `SettingsResourcesTest.java`:

```java
package info.javaway.sudoku.ui.settings;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsResourcesTest {

    @Test public void настройкаЭкранаЕстьВРазметкеИДвухЛокалях() throws Exception {
        String layout = read("src/main/res/layout/activity_settings.xml");
        String en = read("src/main/res/values/strings.xml");
        String ru = read("src/main/res/values-ru/strings.xml");

        assertTrue(layout.contains("android:id=\"@+id/keep_screen_on\""));
        assertTrue(layout.contains("android:text=\"@string/keep_screen_on\""));
        assertTrue(layout.contains("android:text=\"@string/keep_screen_on_hint\""));
        assertTrue(en.contains("<string name=\"keep_screen_on\">Keep screen on</string>"));
        assertTrue(en.contains("<string name=\"keep_screen_on_hint\">While a game is running, I will keep the screen on.</string>"));
        assertTrue(ru.contains("<string name=\"keep_screen_on\">Не выключать экран</string>"));
        assertTrue(ru.contains("<string name=\"keep_screen_on_hint\">Пока идёт партия, я не дам экрану погаснуть.</string>"));
    }

    private static String read(String relative) throws Exception {
        Path fromModule = Paths.get(relative);
        Path file = Files.exists(fromModule) ? fromModule : Paths.get("app").resolve(relative);
        return Files.readString(file);
    }
}
```

- [ ] **Step 2: Run the resource test and verify RED**

```powershell
.\gradlew testDebugUnitTest --tests "info.javaway.sudoku.ui.settings.SettingsResourcesTest" --rerun-tasks
```

Expected: the test fails because the IDs and strings are absent.

- [ ] **Step 3: Add both localized strings**

Add to `values/strings.xml` in the game settings block:

```xml
<string name="keep_screen_on">Keep screen on</string>
<string name="keep_screen_on_hint">While a game is running, I will keep the screen on.</string>
```

Add to `values-ru/strings.xml`:

```xml
<string name="keep_screen_on">Не выключать экран</string>
<string name="keep_screen_on_hint">Пока идёт партия, я не дам экрану погаснуть.</string>
```

- [ ] **Step 4: Add the switch below Sound**

Insert before the Appearance divider:

```xml
<Switch
    android:id="@+id/keep_screen_on"
    style="@style/Toggle"
    android:layout_marginTop="@dimen/space_l"
    android:text="@string/keep_screen_on" />

<TextView
    style="@style/Text.Caption"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/keep_screen_on_hint" />
```

Do not add `maxLines` or ellipsis.

- [ ] **Step 5: Bind the switch through SettingsActivity MVI**

Add the field and lookup:

```java
private Switch keepScreenOn;

keepScreenOn = findViewById(R.id.keep_screen_on);
```

Add to `render()`:

```java
bind(keepScreenOn, state.keepScreenOn,
        value -> store.dispatch(new SettingsAction.KeepScreenOnToggled(value)));
```

Add to `handle()` before theme handling:

```java
} else if (effect instanceof SettingsEffect.SaveKeepScreenOn) {
    boolean value = ((SettingsEffect.SaveKeepScreenOn) effect).value;
    prefs.setKeepScreenOn(value);
```

This call intentionally stays on the main thread. `apply()` updates the shared in-memory
value immediately, so `GameActivity.onResume()` cannot observe the previous setting when
the user presses Back before the common worker finishes another task.

- [ ] **Step 6: Run tests and compile the debug APK**

```powershell
.\gradlew testDebugUnitTest --tests "info.javaway.sudoku.ui.settings.*" assembleDebug --rerun-tasks
```

Expected: settings tests pass and `assembleDebug` succeeds.

- [ ] **Step 7: Commit the UI slice**

```powershell
git add -- app/src/main/res/layout/activity_settings.xml app/src/main/res/values/strings.xml app/src/main/res/values-ru/strings.xml app/src/main/java/info/javaway/sudoku/ui/settings/SettingsActivity.java app/src/test/java/info/javaway/sudoku/ui/settings/SettingsResourcesTest.java
git commit -m "Добавил переключатель удержания экрана"
```

---

### Task 3: Политика фаз и оконный флаг

**Files:**

- Create: `app/src/main/java/info/javaway/sudoku/ui/game/ScreenPolicy.java`
- Create: `app/src/test/java/info/javaway/sudoku/ui/game/ScreenPolicyTest.java`
- Modify: `app/src/main/java/info/javaway/sudoku/ui/game/GameActivity.java:53-72,134-151,281-298`

**Interfaces:**

- Consumes: `Prefs.keepScreenOn()` from Task 1.
- Consumes: `GameState.Phase`.
- Produces: package-private `ScreenPolicy.keepOn(boolean enabled, GameState.Phase phase)`.

- [ ] **Step 1: Write the failing phase matrix test**

Create `ScreenPolicyTest.java`:

```java
package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class ScreenPolicyTest {

    @Test public void включённаяНастройкаДержитЭкранТолькоВАктивнойИгре() {
        for (GameState.Phase phase : GameState.Phase.values()) {
            assertEquals(phase.name(), phase == GameState.Phase.PLAYING,
                    ScreenPolicy.keepOn(true, phase));
        }
    }

    @Test public void выключеннаяНастройкаНикогдаНеДержитЭкран() {
        for (GameState.Phase phase : GameState.Phase.values()) {
            assertFalse(phase.name(), ScreenPolicy.keepOn(false, phase));
        }
    }
}
```

- [ ] **Step 2: Run the policy test and verify RED**

```powershell
.\gradlew testDebugUnitTest --tests "info.javaway.sudoku.ui.game.ScreenPolicyTest" --rerun-tasks
```

Expected: compilation fails because `ScreenPolicy` does not exist.

- [ ] **Step 3: Implement the minimal pure policy**

Create `ScreenPolicy.java`:

```java
package info.javaway.sudoku.ui.game;

/** Решает только системное поведение экрана; состояние партии не меняет. */
final class ScreenPolicy {

    static boolean keepOn(boolean enabled, GameState.Phase phase) {
        return enabled && phase == GameState.Phase.PLAYING;
    }

    private ScreenPolicy() {
    }
}
```

- [ ] **Step 4: Apply the policy in GameActivity**

Import `android.view.WindowManager` and add:

```java
private boolean keepScreenOn;
```

In `onResume()`, read the potentially changed preference before scheduling ticks:

```java
keepScreenOn = prefs.keepScreenOn();
renderScreenPolicy(store.state());
```

At the start of `onPause()`, release the flag explicitly:

```java
getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
```

At the end of `render(GameState state)`, recalculate after every phase transition:

```java
renderScreenPolicy(state);
```

Add the method near the rendering helpers:

```java
private void renderScreenPolicy(GameState state) {
    int flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    if (ScreenPolicy.keepOn(keepScreenOn, state.phase)) {
        getWindow().addFlags(flag);
    } else {
        getWindow().clearFlags(flag);
    }
}
```

This follows the official Android contract: the flag belongs to an Activity, can be
cleared programmatically, needs no wake lock, and does not keep the screen awake after the
app leaves the foreground.

- [ ] **Step 5: Run the policy test and debug compilation**

```powershell
.\gradlew testDebugUnitTest --tests "info.javaway.sudoku.ui.game.ScreenPolicyTest" assembleDebug --rerun-tasks
```

Expected: `ScreenPolicyTest` passes and the debug APK builds.

- [ ] **Step 6: Commit the game slice**

```powershell
git add -- app/src/main/java/info/javaway/sudoku/ui/game/ScreenPolicy.java app/src/main/java/info/javaway/sudoku/ui/game/GameActivity.java app/src/test/java/info/javaway/sudoku/ui/game/ScreenPolicyTest.java
git commit -m "Удержал экран во время активной партии"
```

---

### Task 4: Полная и устройственная проверка

**Files:**

- Verify only: `app/src/main/AndroidManifest.xml`
- Verify only: all files modified in Tasks 1-3.

**Interfaces:**

- Consumes: complete setting and screen policy.
- Produces: verified debug/release APKs and restored emulator display settings.

- [ ] **Step 1: Run the mandatory clean Gradle verification**

```powershell
.\gradlew testDebugUnitTest assembleDebug assembleRelease --rerun-tasks
```

Expected with the current missing `keystore.properties`:

```text
BUILD SUCCESSFUL
81 actionable tasks: 81 executed
```

Also run:

```powershell
git diff --check
```

Expected: no output.

- [ ] **Step 2: Verify copy and the zero-permission invariant**

```powershell
rg -n "—" app/src/main/res/values/strings.xml app/src/main/res/values-ru/strings.xml
rg -n "<uses-permission" app/src/main/AndroidManifest.xml
```

Expected: neither command finds a match. Verify the new entries exist in both locales:

```powershell
rg -n "keep_screen_on" app/src/main/res/values/strings.xml app/src/main/res/values-ru/strings.xml
```

Expected: two entries in each locale.

- [ ] **Step 3: Re-read the machine manifest before any device command**

```powershell
Get-Content -Raw -Encoding UTF8 -LiteralPath "C:\Users\maxkaz\Development\obsidian_db\💻 Projects\_Общие_знания\Машины\DESKTOP-393VM2L.md"
```

Confirm from it that adb is at `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe` and the
only allowed target for this task is `emulator-5554`.

- [ ] **Step 4: Install only on emulator-5554**

```powershell
$sudokuAdb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
& $sudokuAdb -s emulator-5554 get-state
& $sudokuAdb -s emulator-5554 install -r "app\build\outputs\apk\debug\app-debug.apk"
& $sudokuAdb -s emulator-5554 shell pm clear info.javaway.sudoku
```

Expected: `device`, then `Success` for installation and data clearing. This intentionally
removes only the emulator's Sudoku state so the enabled default is tested. Do not run any
Gradle `installX` task.

- [ ] **Step 5: Verify the flag across game phases and navigation**

Launch the game:

```powershell
& $sudokuAdb -s emulator-5554 shell am force-stop info.javaway.sudoku
& $sudokuAdb -s emulator-5554 shell am start -n info.javaway.sudoku/.ui.game.GameActivity
```

After generation finishes, inspect WindowManager:

```powershell
& $sudokuAdb -s emulator-5554 shell dumpsys window displays | Select-String "mHoldScreenWindow"
```

Expected: `mHoldScreenWindow` names `GameActivity` while phase is `PLAYING`.

Open Settings before changing the new switch and confirm it is enabled after the clean
data state from Step 4.

Use the visible Pause action on the emulator, rerun the same command, and expect no held
screen window. Resume and expect `GameActivity` again. Open Settings and expect the held
window to disappear. Disable «Не выключать экран», return to the active board and expect
no held screen window. Re-enable it and expect `GameActivity` again.

- [ ] **Step 6: Verify persistence**

With the setting disabled, force-stop and relaunch the app using the two explicit commands
from Step 5. Open Settings and confirm the switch remains disabled. Enable it, force-stop,
relaunch and confirm it remains enabled.

- [ ] **Step 7: Verify the settings screen at large font and restore the emulator**

Run this restoration-safe PowerShell block:

```powershell
$sudokuAdb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
try {
    & $sudokuAdb -s emulator-5554 shell settings put system font_scale 1.8
    & $sudokuAdb -s emulator-5554 shell wm size 864x2340
    & $sudokuAdb -s emulator-5554 shell am start -n info.javaway.sudoku/.ui.settings.SettingsActivity
    & $sudokuAdb -s emulator-5554 shell screencap -p /sdcard/sudoku-settings-font-1.8.png
    New-Item -ItemType Directory -Force "build\verification" | Out-Null
    & $sudokuAdb -s emulator-5554 pull /sdcard/sudoku-settings-font-1.8.png "build\verification\settings-font-1.8.png"
} finally {
    & $sudokuAdb -s emulator-5554 shell settings put system font_scale 1.0
    & $sudokuAdb -s emulator-5554 shell wm size reset
}
```

Inspect `build/verification/settings-font-1.8.png`. Expected: the switch label and caption
wrap without clipping, remain above the Appearance section, and the page scrolls to all
content. Confirm restoration:

```powershell
& $sudokuAdb -s emulator-5554 shell settings get system font_scale
& $sudokuAdb -s emulator-5554 shell wm size
```

Expected: `1.0` and the emulator's physical size, not `864x2340`.

- [ ] **Step 8: Final repository check**

```powershell
git branch --show-current
git status --short
git log --oneline -6
```

Expected: branch `master`; only the pre-existing user-owned `?? AGENTS.md` remains; the
feature is represented by the planned small Russian past-tense commits.

## Reference

- Android Developers, [Keep the screen on](https://developer.android.com/develop/background-work/background-tasks/awake/screen-on): `FLAG_KEEP_SCREEN_ON` is Activity-scoped, equivalent to `android:keepScreenOn`, may be cleared programmatically, and does not keep a background app's screen awake.
