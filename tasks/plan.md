# Implementation Plan: Sudoku Google Play release

**Status:** Approved by the owner on 2026-08-27.

## Overview

Release Sudoku 1.4 from the current common Android source to Google Play while preserving RuStore compatibility, eliminating Android 16/API 23 release blockers, producing compliant store assets and declarations, and isolating all website work from the active feedback-system changes in `mysite`.

The plan deliberately separates four kinds of state:

1. common application source on `master`;
2. one-line store selection on `google_play` and `rustore`;
3. public website changes in a separate `mysite` worktree;
4. private signing material and uploaded artifacts outside Git.

## Dependency graph

```text
Signing provenance + console version history
    ├── approved app-signing strategy
    │       └── signed AAB/APK
    │               └── Play Internal test
    │                       └── closed test or production review
    └── approved version 1.4 / code 5
            └── common release commit
                    ├── google_play branch
                    └── rustore branch

Android compatibility fixes
    └── green unit/build/lint gate
            ├── runtime smoke tests
            └── release screenshots
                    └── Play listing assets and copy
                            ├── isolated website update
                            └── Play Console listing/declarations
```

## Architecture decisions

- Keep `master` store-neutral except for a documented default `BuildConfig.STORE_FLAVOR` value. Release branches change only that build-time value and unavoidable store metadata.
- Keep platform Android Views and platform back-navigation APIs; do not add AndroidX to silence Lint.
- Treat the existing API 33+ `OnBackInvokedDispatcher` path as the primary back implementation and the legacy override as an API 23-32 fallback. Suppress only the detector's false positive, with a regression test that proves both paths remain present.
- Introduce a qualified platform-theme parent so API 23-28 never reference the API-29-only `Theme.DeviceDefault.DayNight` resource.
- Add explicit legacy and Android 12+ backup rules excluding all local game/preferences data from cloud and device transfer.
- Prefer the RuStore signing identity for Play App Signing to preserve cross-store updates, but only after certificate comparison and an explicit security decision. Use a separate fresh upload key.
- Store release-ready public assets and listing copy under `content/play-store/1.4/`; never store signed binaries, credentials, or private keys there.
- Capture screenshots from a verified release-equivalent build. Generate only the required feature graphic; do not fabricate UI screenshots.
- Use Play Internal testing before any public track and keep managed publishing enabled when available.
- Create `C:\Users\maxkaz\Developer\mysite-sudoku-play` from clean `mysite/master` for site work. The dirty primary `mysite` worktree remains untouched.

## Task list

### Phase 1: Release identity and security

- [ ] Task 1: Verify signing-key provenance and console version history.
- [ ] Task 2: Preserve the local key safely and remove the tracked PKCS#12 from branch tips.
- [ ] Task 3: Record the approved Play App Signing and version decision.

### Checkpoint: Identity

- [ ] RuStore certificate fingerprint and highest version code are evidence-backed.
- [ ] The owner has approved any signing choice with cross-store consequences.
- [ ] No private key remains tracked at the release branch tip.

### Phase 2: Android release blockers

- [ ] Task 4: Make the application theme API-23-compatible.
- [ ] Task 5: Make predictive-back handling Lint-clean without AndroidX.
- [ ] Task 6: Enforce the published no-backup/no-transfer behavior.
- [ ] Task 7: Resolve or document remaining release-relevant Lint warnings.
- [ ] Task 8: Set and document the verified 1.4 release version.

### Checkpoint: Android source

- [ ] Forced unit tests, debug/release APKs, release AAB, and `lintRelease` all succeed.
- [ ] The manifest still declares zero permissions and package `info.javaway.sudoku`.
- [ ] The unsigned build path remains safe when local signing configuration is absent.

### Phase 3: Runtime and store materials

- [ ] Task 9: Perform emulator/runtime smoke tests, including Android 16 behavior.
- [ ] Task 10: Capture four compliant phone screenshots from the verified build.
- [ ] Task 11: Prepare the Play icon and required feature graphic.
- [ ] Task 12: Write English/Russian listing copy and declaration workbook.

### Checkpoint: Submission package

- [ ] All graphics meet Play dimensions, format, opacity, and content rules.
- [ ] Listing text stays within Play limits and matches actual behavior.
- [ ] Release notes, support URL, privacy URL, and questionnaire answers are ready to paste.

### Phase 4: Store topology and website

- [ ] Task 13: Implement deterministic store-link selection in common code.
- [ ] Task 14: Synchronize and verify `google_play` and `rustore` release branches.
- [ ] Task 15: Update the Sudoku product page in an isolated website worktree.
- [ ] Task 16: Verify and integrate the isolated website change.

### Checkpoint: Public surfaces

- [ ] The two application branches differ only in approved store values/metadata.
- [ ] The site page and privacy policy are live, consistent, and browser-verified.
- [ ] The feedback agent's uncommitted website files are unchanged.

### Phase 5: Signing and Play Console

- [ ] Task 17: Produce and verify signed release artifacts.
- [ ] Task 18: Create the Play app and complete package registration/signing setup.
- [ ] Task 19: Complete store listing and policy declarations.
- [ ] Task 20: Upload and verify an Internal testing release.
- [ ] Task 21: Resolve pre-launch report findings and promote to the available public path.
- [ ] Task 22: Record release checksums, tags, console state, and rollback instructions.

### Checkpoint: Release

- [ ] The Play-generated build passes the critical user-flow smoke test.
- [ ] Production review is submitted, or every prerequisite is complete and the mandatory 12-tester/14-day closed test is active.
- [ ] Exact source commits, tags, certificates, and artifact checksums are recorded without secrets.

## Verification checkpoints

### After Tasks 1-3

```powershell
git status --short
git ls-files | rg -i '(^|/)(keystore|.*\.(jks|keystore|p12|pfx))$'
```

Review certificate fingerprints and console version codes manually; do not print passwords.

### After Tasks 4-8

```powershell
$env:ANDROID_HOME = 'C:\Users\maxkaz\AppData\Local\Android\Sdk'
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease bundleRelease lintRelease --rerun-tasks
```

Expected: all tasks complete successfully, 178 or more tests pass, Lint has zero errors.

### After Tasks 9-12

```powershell
ffprobe -v error -select_streams v:0 -show_entries stream=width,height,pix_fmt -of default=noprint_wrappers=1 content\play-store\1.4\graphics\*.png
```

Expected: icon 512x512, feature graphic 1024x500, screenshots 1080x1920 or better; required assets have no alpha channel.

### After Tasks 13-16

```powershell
git diff master..google_play --stat
git diff master..rustore --stat
npm test
npm run typecheck
npm run lint
npm run build
```

Run npm commands from the isolated site worktree. Verify the product and privacy routes in a real browser.

### After Tasks 17-22

```powershell
jarsigner -verify -verbose -certs <signed-aab>
apksigner verify --verbose --print-certs <signed-apk>
```

Compare the printed certificate fingerprints with the approved certificate records, then verify the uploaded bundle in Play's App Bundle Explorer and on the Internal testing track.

## Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Public PKCS#12 is the active RuStore key | High: offline password attack or release impersonation | Compare fingerprints first; assess password exposure; use Play App Signing; seek store-supported rotation before destructive action |
| Play uses a different signing identity | High: users cannot update across stores without uninstalling and lose local data | Prefer the existing app-signing identity; require owner approval before accepting incompatibility |
| RuStore live version history differs from Git | High: duplicate/rollback version code or irreproducible release | Read highest code from console and set one evidence-backed higher code in committed source |
| API 23 theme fix changes appearance | Medium | Qualified parent only; screenshots and runtime checks in light/dark/system modes |
| Predictive back regression on API 36 | Medium | Platform callback test, API 36 emulator/pre-launch report, panel-open and app-exit smoke cases |
| Backup declarations contradict policy | High | Explicit legacy and Android 12+ exclusion rules plus merged-manifest/resource tests |
| Store screenshots do not match release | Medium | Capture from the verified build after source freeze |
| Website work collides with feedback agent | High | Dedicated worktree from clean HEAD; focused commit; merge only after primary work is reconciled |
| New-account testing requirement delays production | Schedule only | Detect account eligibility early; start Internal/Closed test immediately when required |
| Play questionnaire wording changes | Medium | Use current console prompts and official help; stop for new legal attestations |

## Parallelization opportunities

- After the Android source checkpoint, listing copy and feature-graphic preparation are independent.
- Website work is independent after final behavior and listing copy are known because it runs in a separate worktree.
- Play Console listing fields can be filled while Internal testing processes the uploaded bundle.
- Signing decisions, version code, branch synchronization, and final promotion remain sequential.

No sub-agents are planned; parallel agents were not requested. Independent work will be interleaved only where it does not mutate shared state.

## Open decision gates

- Task 1 may require the owner to sign in to RuStore Console and Play Console and complete 2FA.
- Task 3 requires explicit owner approval if certificate evidence makes same-key Play signing unsafe.
- Task 18 may require identity verification and a package-ownership APK generated from a console-provided snippet.
- Task 21 may pause for 14 days if the personal-account closed-test rule applies.
- Legal agreements, developer identity changes, payments, and final attestations not already covered by the approved spec remain owner actions.
