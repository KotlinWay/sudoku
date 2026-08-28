# Spec: Sudoku release for Google Play

**Status:** Approved by the owner on 2026-08-27.

## Objective

Prepare the existing Android Sudoku game for its first Google Play release and submit it to the furthest track available for the developer account. Keep one common application codebase and two store release branches, `google_play` and `rustore`, whose intentional source difference is limited to store URLs.

The release is for Android phone and tablet users. It remains free, offline, ad-free, account-free, and contains no in-app purchases, analytics, or third-party runtime SDKs.

## Current evidence

- Common branch: `master` at `d3dff466`; the worktree was clean before the audit.
- Remote `rustore` is an ancestor of `master` by 13 commits, so it is safe to synchronize from the common branch but is currently stale.
- The live RuStore listing reports version `1.3` (August 20, 2026), while `app/build.gradle` and `origin/rustore` still report version `1.1`, code `2`.
- Forced unit-test execution passed: 178 tests, 0 failures, 0 errors, 0 skipped.
- Debug APK, release APK, and release AAB are buildable. The release APK and AAB are currently unsigned, as designed when `keystore.properties` is absent.
- `lintRelease` currently fails with 2 errors and 24 warnings:
  - the Android 16 back-navigation detector does not recognize the existing platform `OnBackInvokedDispatcher` plus pre-33 `onBackPressed` fallback;
  - `Theme.DeviceDefault.DayNight` is API 29 while the application supports API 23.
- `allowBackup="false"` alone may still permit device-to-device transfer on Android 12+; this conflicts with the published privacy statement that application data is not transferred.
- The public site already contains and serves:
  - `https://javaway.info/portfolio/sudoku`;
  - `https://javaway.info/sudoku-privacy-policy`.
- The site listing already points to `https://play.google.com/store/apps/details?id=info.javaway.sudoku`; it returns 404 until the Play app is created.
- Four current phone screenshots exist as 720x1280 WebP files. They meet the minimum dimensions but not the recommended 1080x1920 game-screenshot guidance and are not in an accepted upload format.
- A 512x512 Play icon exists. A required 1024x500 opaque feature graphic does not.
- A 2520-byte PKCS#12 file named `keystore` is tracked in the public GitHub repository. Its certificate and relationship to the RuStore signing certificate have not yet been verified.

## Release requirements

### Android application

1. Preserve package name `info.javaway.sudoku`, `minSdk 23`, and `targetSdk 36`.
2. Fix all Android Lint errors without adding AndroidX or another runtime dependency.
3. Keep predictive back behavior on API 33+ through platform APIs and retain correct fallback behavior on API 23-32.
4. Use an API-compatible base theme on Android 6-9 while retaining the existing light, dark, and system theme behavior on newer Android versions.
5. Explicitly exclude all application files and preferences from cloud backup and device-to-device transfer on supported Android versions, matching the privacy policy.
6. Review all remaining lint warnings. Fix correctness, accessibility, privacy, and compatibility warnings; document narrowly suppressed false positives. Unrelated visual refactors are out of scope.
7. Produce a signed release AAB for Google Play and a reproducible signed APK path for RuStore.
8. Set a release version higher than the live RuStore 1.3 release. Proposed common version: `1.4`, code `5`, subject to certificate/version confirmation in the consoles.

### Signing and package ownership

1. Never commit passwords, `keystore.properties`, private certificates, or new keystores.
2. Compare the tracked PKCS#12 certificate with the current RuStore app certificate before choosing a Play signing strategy.
3. Because the same package is distributed outside Play, preserve cross-store update compatibility when safely possible:
   - if the RuStore signing key is valid and not considered compromised, provide that app-signing key to Play App Signing and use a separate new upload key;
   - if the key is weak, exposed, unavailable, or rejected, stop before choosing a different Play signing key and obtain owner approval for the resulting rotation or cross-store incompatibility.
4. Complete Android developer verification/package registration. If Play Console requests proof of ownership, build the verification APK with the console-provided asset snippet and the existing RuStore signing key.
5. Remove the tracked private-keystore file from the release branches and add its exact filename to ignore rules. Rewriting public Git history or rotating the RuStore key is destructive/cross-system work and requires separate explicit approval.

### Store branches

1. `master` remains the common source branch.
2. `google_play` and `rustore` are release branches synchronized from the same verified common commit.
3. Store-specific link behavior must be deterministic:
   - rating, developer page, and promoted companion-app links resolve to Google Play in `google_play`;
   - the corresponding links resolve to RuStore in `rustore`.
4. The intentional branch diff is limited to those URLs and release metadata that a store strictly requires. Gameplay, resources, tests, package name, and privacy behavior remain identical.
5. Release commits and tags identify the exact source of every uploaded artifact. No artifact is built from an uncommitted version change.

### Google Play listing and declarations

1. Create a free **Game** named **Sudoku** with default language English (United States), category **Puzzle**, package `info.javaway.sudoku`, and no monetization.
2. Add hand-written English and Russian listings:
   - name no longer than 30 characters;
   - short description no longer than 80 characters;
   - full description no longer than 4000 characters;
   - no ranking, promotional, time-sensitive, or repetitive keyword claims.
3. Upload:
   - opaque 512x512 PNG app icon;
   - opaque 1024x500 PNG/JPEG feature graphic;
   - at least four current 1080x1920 phone PNG/JPEG screenshots, including actual gameplay and both themes;
   - accurate alt text for uploaded graphics where Play Console offers it.
4. Use `https://javaway.info/sudoku-privacy-policy` as the privacy-policy URL and `https://javaway.info/portfolio/sudoku` as the support website.
5. Declare the observed application behavior accurately:
   - no ads;
   - all functionality available without sign-in or special access;
   - no data collected or shared by the app or embedded SDKs;
   - no advertising ID, sensitive permissions, account system, purchases, financial, health, government, or news functionality;
   - content-rating answers describe a conventional Sudoku puzzle with no objectionable or user-generated content.
6. Target-age answers must match the actual audience. The proposed starting selection is ages 13-15, 16-17, and 18+, not primarily directed at children. If Play Console applies Families requirements, verify every external link and declaration instead of changing answers merely to bypass review.
7. Make the app available in all countries/regions permitted by the account and Google Play policies unless the owner identifies a legal restriction.

### Testing and rollout

1. Upload the signed AAB to Internal testing first and install the Play-generated APK on a test device through the tester flow.
2. Verify cold start, new game, both game modes, pause/back behavior, rotation, theme switching, save/restore, settings links, TalkBack semantics, and Android 16 edge-to-edge/back behavior.
3. Inspect pre-launch report results and resolve release-blocking crashes, ANRs, accessibility failures, or policy warnings.
4. If the account is a personal developer account created after November 13, 2023, start a closed test with at least 12 continuously opted-in testers for at least 14 days, then apply for production access.
5. Otherwise, submit the verified build to production review with managed publishing enabled when available.
6. Do not claim public availability until Play Console reports the production release as available.

### Website

1. Perform any website work in a separate Git worktree and branch based on clean `mysite/master`; do not touch the dirty primary site worktree used by the feedback agent.
2. Keep the existing product and privacy URLs stable.
3. Reconcile the product page with release 1.4 behavior, especially the relaxed-game mode, current screenshots, and Play availability wording.
4. Do not merge or deploy website changes until the isolated worktree passes tests, type checking, build, and browser verification.

## Tech stack

### Android repository

- Android Gradle Plugin 9.0.1
- Gradle 9.1.0 wrapper
- Java 11 source/target, running on JDK 21
- compile/target SDK 36, minimum SDK 23
- Platform Android Views; no AndroidX, Material, Kotlin, or runtime dependencies
- JUnit 4.13.2 for local unit tests

### Website repository

- Next.js 15.5.22 App Router
- React 19
- TypeScript 5.x
- Tailwind CSS 3.4
- Node test runner through `tsx`

## Commands

Run Android commands from `C:\Users\maxkaz\Developer\sudoku` after making the SDK visible to the process:

```powershell
$env:ANDROID_HOME = 'C:\Users\maxkaz\AppData\Local\Android\Sdk'
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease bundleRelease lintRelease --rerun-tasks
```

Do not run `installDebug`. Device commands, when needed, must name the emulator explicitly:

```powershell
adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk
```

Run website verification only from the isolated website worktree:

```powershell
npm ci
npm test
npm run typecheck
npm run lint
npm run build
```

## Project structure

```text
sudoku/
  app/src/main/java/       Android application source
  app/src/main/res/        Android layouts, strings, themes, and launcher assets
  app/src/test/java/       Local unit and resource regression tests
  content/                 Source artwork and store-ready release assets
  docs/                    Release specification and durable project documentation

mysite/                    Existing dirty primary worktree; read-only for this task
mysite-sudoku-play/        Planned isolated worktree for website release changes
  app/portfolio/sudoku/    Product/support page
  app/sudoku-privacy-policy/ Privacy policy
  public/                  Product screenshots and public graphics
```

## Code style

Follow the existing Java/MVI style: explicit immutable updates, early returns, platform APIs, and no speculative abstraction.

```java
private boolean handleBack() {
    GameState state = store.state();
    if (!state.panel) return false;
    store.dispatch(state.phase == GameState.Phase.PAUSED
            ? new GameAction.PauseToggled() : new GameAction.PanelDismissed());
    return true;
}
```

Website changes follow existing server-component and Tailwind patterns with two-space indentation.

## Testing strategy

- Add focused unit/resource regression tests for each compatibility or privacy change that can be verified without a device.
- Execute all 178+ unit tests and all relevant Gradle build/lint tasks from scratch after each Android increment.
- Verify signed artifacts with `jarsigner`/`apksigner`; verify the final AAB again after Play Console ingestion.
- Perform explicit emulator smoke tests; never install to an unspecified connected device.
- Capture store screenshots from the verified release-equivalent build, not from mockups.
- Run the complete website test, type-check, lint, and production-build commands in its isolated worktree; verify changed pages in a real browser.
- Use Play Internal testing and the Play pre-launch report as the final Android integration gate.

## Boundaries

### Always

- Preserve user data and gameplay behavior unless a release blocker requires a change.
- Keep app, website, store declarations, and privacy claims consistent.
- Use official Android and Google Play documentation for platform and policy decisions.
- Inspect staged diffs for secrets before every commit.
- Keep the feedback agent's website worktree untouched.

### Ask first

- Use, upload, rotate, or replace the existing RuStore signing key.
- Accept different signing identities between Play and RuStore.
- Rewrite public Git history, force-push, or rotate the RuStore production key.
- Make a paid purchase, accept a legal agreement, change public developer identity/contact data, or exclude countries.
- Send the final production change for review if Play Console presents new legal or policy attestations not covered by this specification.

### Never

- Put credentials, 2FA codes, private keys, or passwords in chat, source control, logs, screenshots, or release notes.
- Build a release from a dirty worktree or an uncommitted version change.
- Hide real lint/policy failures behind a broad baseline or inaccurate Play Console answer.
- Install to the owner's physical device without an explicit device-specific instruction.
- Merge unrelated feedback-system work into the Sudoku website release.

## Success criteria

1. The common application commit passes all unit tests, debug/release builds, release bundle generation, and release lint with no errors.
2. Runtime smoke tests pass on the available emulator and on a Play-generated test install; Android 16 back navigation and API-23-compatible resources are demonstrably handled.
3. The final AAB is signed with the approved upload key, contains package `info.javaway.sudoku`, targets API 36, declares no permissions, and is accepted by Play Console.
4. The Play listing has valid English and Russian copy, required compliant graphics, working support/privacy URLs, and completed truthful declarations.
5. `google_play` and `rustore` point at the same verified application source and differ only in approved store URLs/metadata.
6. The public-key/package-ownership flow is completed without exposing new secrets.
7. The release reaches Internal testing, then either:
   - is sent to production review; or
   - enters the mandatory 12-tester/14-day closed test with every other production prerequisite complete.
8. Website changes, if needed, are independently committed, verified, and integrated without touching the feedback agent's uncommitted work.
9. Release source commits are tagged and the exact uploaded artifacts and checksums are recorded locally outside Git where appropriate.

## Open questions and decision gates

1. Is the tracked `keystore` the certificate that signed the current RuStore 1.3 APK, and is its password strong and known to the owner? Verify in RuStore Console; do not answer from memory.
2. Is the Play developer account personal and, if so, was it created after November 13, 2023? Play Console determines whether closed testing is mandatory.
3. Does Play Console auto-register `info.javaway.sudoku`, or require a signed verification APK because the package already exists in RuStore?
4. Does the owner approve the proposed release version `1.4` / code `5` after console version history is checked?

## Authoritative sources

- Target API 36 requirement from August 31, 2026: https://developer.android.com/google/play/requirements/target-sdk
- Android 16 predictive back behavior: https://developer.android.com/about/versions/16/behavior-changes-16
- Android backup and device-transfer rules: https://developer.android.com/identity/data/autobackup
- App signing and Play App Signing: https://developer.android.com/studio/publish/app-signing
- Package-name registration and verification: https://support.google.com/googleplay/android-developer/answer/16984799
- Create and configure an app: https://support.google.com/googleplay/android-developer/answer/9859152
- Store graphic requirements: https://support.google.com/googleplay/android-developer/answer/9866151
- App review declarations: https://support.google.com/googleplay/android-developer/answer/9859455
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Target audience: https://support.google.com/googleplay/android-developer/answer/9867159
- New personal-account testing requirements: https://support.google.com/googleplay/android-developer/answer/14151465
