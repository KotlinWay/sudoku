# Sudoku Google Play release checklist

## Task 1: Verify signing provenance and version history

**Description:** Establish which certificate signed the live RuStore 1.3 APK, the highest published version code, and whether Play requires proof of ownership for `info.javaway.sudoku`.

**Acceptance criteria:**
- [ ] RuStore signing SHA-256 is recorded without private material.
- [ ] Highest RuStore version code/name are read from the console.
- [ ] The tracked PKCS#12 certificate is confirmed as matching or not matching RuStore.

**Verification:**
- [ ] Compare certificate fingerprints character-for-character.
- [ ] Save only public fingerprints and console facts in release notes.

**Dependencies:** None

**Files likely touched:** `docs/releases/1.4.md`

**Estimated scope:** Small
## Task 2: Remove the tracked private keystore safely

**Description:** Preserve one local recovery copy outside Git, then remove `keystore` from the tracked branch tip and ignore the exact extensionless filename as well as standard key extensions.

**Acceptance criteria:**
- [ ] A local recovery copy exists outside the repository and is not overwritten.
- [ ] `keystore` is absent from the worktree and tracked tip.
- [ ] `.gitignore` excludes `keystore`, `*.p12`, and `*.pfx` in addition to existing patterns.

**Verification:**
- [ ] `git status --short` shows only the intended deletion/ignore change.
- [ ] `git check-ignore -v keystore` identifies the new rule.
- [ ] No new secret appears in the staged diff.

**Dependencies:** Task 1

**Files likely touched:** `.gitignore`, `keystore`

**Estimated scope:** Small

## Task 3: Record signing and version decisions

**Description:** Document the approved Play App Signing key, separate upload key, package-registration path, and next version after console evidence is available.

**Acceptance criteria:**
- [ ] Cross-store compatibility consequences are explicit.
- [ ] Approved version code exceeds both stores' histories.
- [ ] No password, private key, or 2FA material is documented.

**Verification:**
- [ ] Owner confirms any decision that changes signing identity.
- [ ] Release record contains only public certificate fingerprints.

**Dependencies:** Tasks 1-2

**Files likely touched:** `docs/releases/1.4.md`

**Estimated scope:** Small

## Task 4: Make the platform theme API-23-compatible

**Description:** Route the application theme through a qualified platform parent so API 23-28 never resolve the API-29-only DayNight style while existing app colors and theme selection remain intact.

**Acceptance criteria:**
- [ ] Base resources reference only styles available at API 23.
- [ ] API 29+ uses `Theme.DeviceDefault.DayNight` through a qualified resource.
- [ ] Existing light/dark/system resources remain single-source where practical.

**Verification:**
- [ ] Add a resource regression test for qualified parents.
- [ ] `lintRelease` no longer reports `NewApi` for `AppTheme`.
- [ ] Smoke-test all three theme choices.

**Dependencies:** Task 3

**Files likely touched:** `app/src/main/res/values/themes.xml`, `app/src/main/res/values-v29/themes.xml`, `app/src/test/java/info/javaway/sudoku/ui/ThemeResourcesTest.java`

**Estimated scope:** Medium

## Task 5: Make back navigation Lint-clean

**Description:** Preserve the platform callback for API 33+ and legacy fallback for API 23-32, then narrowly suppress the detector path it cannot model.

**Acceptance criteria:**
- [ ] Open pause/win panels consume Back and return to the board.
- [ ] With no panel open, system Back exits normally and predictive animation remains available.
- [ ] No AndroidX dependency is introduced.

**Verification:**
- [ ] Add a source/resource regression test proving callback registration and guarded legacy fallback.
- [ ] `lintRelease` has no `GestureBackNavigation` error.
- [ ] Smoke-test panel Back and app-exit Back on API 33/36.

**Dependencies:** Task 4

**Files likely touched:** `app/src/main/java/info/javaway/sudoku/ui/game/GameActivity.java`, `app/src/test/java/info/javaway/sudoku/ui/game/BackNavigationTest.java`

**Estimated scope:** Small

## Task 6: Disable backup and device transfer explicitly

**Description:** Make manifest and XML rules match the published claim that game state and statistics never leave the device through Android backup/transfer.

**Acceptance criteria:**
- [ ] Legacy Auto Backup excludes all app-owned domains.
- [ ] Android 12+ cloud backup and device transfer exclude all app-owned domains.
- [ ] The manifest references both rule sets and retains `allowBackup=false`.

**Verification:**
- [ ] Add a resource test that parses the manifest and both XML files.
- [ ] `lintRelease` no longer reports missing `dataExtractionRules`.
- [ ] Inspect the merged release manifest.

**Dependencies:** Task 5

**Files likely touched:** `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/backup_rules.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/test/java/info/javaway/sudoku/ui/BackupResourcesTest.java`

**Estimated scope:** Medium

## Task 7: Triage remaining Lint warnings

**Description:** Review all warnings after Tasks 4-6; fix compatibility/accessibility correctness issues and document intentional layout/performance warnings without broad baselines.

**Acceptance criteria:**
- [ ] `Integer.BYTES` does not create an API-23 compatibility warning.
- [ ] Board touch accessibility is either correctly implemented or narrowly justified.
- [ ] Every remaining warning has an evidence-backed disposition.

**Verification:**
- [ ] Run `lintRelease --rerun-tasks` and review the full text report.
- [ ] Run focused tests for any touched behavior.

**Dependencies:** Task 6

**Files likely touched:** `app/src/main/java/info/javaway/sudoku/ui/game/GameSave.java`, `app/src/main/java/info/javaway/sudoku/ui/game/BoardView.java`, `app/src/test/java/info/javaway/sudoku/ui/game/GameSaveTest.java`, `docs/releases/1.4.md`

**Estimated scope:** Medium

## Task 8: Set release version 1.4

**Description:** Commit the owner-approved version name/code and human-readable release notes from the already-live 1.3 baseline.

**Acceptance criteria:**
- [ ] Version code exceeds both store histories.
- [ ] Version name is `1.4` unless console evidence required a revised approved value.
- [ ] Release notes describe user-visible impact, not internal mechanics.

**Verification:**
- [ ] Inspect packaged manifest metadata with Android build tools.
- [ ] Unit/build/lint checkpoint is fully green.

**Dependencies:** Tasks 3, 7

**Files likely touched:** `app/build.gradle`, `docs/releases/1.4.md`

**Estimated scope:** Small

## Task 9: Runtime smoke-test release candidate

**Description:** Install only to explicitly named emulators and verify critical flows on API 33 and API 36 or the closest available Play pre-launch equivalent.

**Acceptance criteria:**
- [ ] Cold start, both game modes, pause/back, rotation, save/restore, and theme changes work.
- [ ] Settings links open the expected store for the tested build.
- [ ] TalkBack semantics remain usable.

**Verification:**
- [ ] Use `adb -s emulator-5554` only after that emulator exists.
- [ ] Record device/API and smoke results in release notes.

**Dependencies:** Task 8

**Files likely touched:** `docs/releases/1.4.md`

**Estimated scope:** Small

## Task 10: Capture Play phone screenshots

**Description:** Capture four actual 9:16 phone screens at 1080x1920 or higher from the verified build: levels, light gameplay, dark candidates, and solved/relaxed state.

**Acceptance criteria:**
- [ ] Four PNG/JPEG files show current UI without notifications or personal data.
- [ ] Each file is at least 1080x1920 and no dimension exceeds twice the other.
- [ ] Screens accurately represent release 1.4.

**Verification:**
- [ ] Inspect dimensions/pixel format with `ffprobe`.
- [ ] Visually inspect all four images.

**Dependencies:** Task 9

**Files likely touched:** `content/play-store/1.4/graphics/phone-01-levels.png`, `content/play-store/1.4/graphics/phone-02-light.png`, `content/play-store/1.4/graphics/phone-03-dark.png`, `content/play-store/1.4/graphics/phone-04-relaxed.png`

**Estimated scope:** Medium

## Task 11: Prepare icon and feature graphic

**Description:** Reuse the existing 512 icon and create one compliant, text-light 1024x500 feature graphic consistent with the app's gold/charcoal visual language.

**Acceptance criteria:**
- [ ] Icon is opaque 512x512 PNG and under 1 MB.
- [ ] Feature graphic is opaque 1024x500 PNG/JPEG.
- [ ] No store badge, ranking, price, testimonial, or download call-to-action appears.

**Verification:**
- [ ] Inspect dimensions, alpha, file sizes, and visual crop safety.
- [ ] Compare against current official Play asset requirements.

**Dependencies:** Task 9

**Files likely touched:** `content/play-store/1.4/graphics/icon-512.png`, `content/play-store/1.4/graphics/feature-1024x500.png`

**Estimated scope:** Small

## Task 12: Write listing copy and declaration workbook

**Description:** Produce paste-ready English/Russian metadata, release notes, asset alt text, and truthful answers for every expected App content declaration.

**Acceptance criteria:**
- [ ] Text meets 30/80/4000-character limits in both locales.
- [ ] Data safety states no collection/sharing and matches app behavior/privacy policy.
- [ ] Ads, access, audience, IARC, financial, health, government, news, and monetization answers are covered.

**Verification:**
- [ ] Run a small character-count check.
- [ ] Cross-check every claim against manifest, dependencies, and live privacy page.

**Dependencies:** Tasks 8, 10-11

**Files likely touched:** `content/play-store/1.4/en-US.md`, `content/play-store/1.4/ru-RU.md`, `content/play-store/1.4/declarations.md`

**Estimated scope:** Medium

## Task 13: Implement deterministic store-link selection

**Description:** Replace installer inference with the approved release-branch store value while hiding the irrelevant developer-store menu entry and preserving web fallbacks.

**Acceptance criteria:**
- [ ] Google Play builds use only Play rating/developer/companion links.
- [ ] RuStore builds use only RuStore equivalents.
- [ ] Branch-specific source difference is confined to the store build value.

**Verification:**
- [ ] Add pure/source regression coverage for both values.
- [ ] Build and manually exercise both store variants.

**Dependencies:** Task 8

**Files likely touched:** `app/build.gradle`, `app/src/main/java/info/javaway/sudoku/settings/Links.java`, `app/src/main/java/info/javaway/sudoku/ui/settings/SettingsActivity.java`, `app/src/test/java/info/javaway/sudoku/settings/StoreLinksTest.java`

**Estimated scope:** Medium

## Task 14: Synchronize release branches

**Description:** Create/update `google_play` and fast-forward the stale `rustore` branch from the same verified common commit, then apply only the store-value delta.

**Acceptance criteria:**
- [ ] Both branches contain all 1.4 fixes and tests.
- [ ] Branch comparison shows only approved store values/metadata.
- [ ] Both branches build from clean worktrees.

**Verification:**
- [ ] Review `git log --graph`, `git diff`, and branch-specific builds.
- [ ] Push without force; stop if remote movement prevents fast-forward.

**Dependencies:** Task 13 and Android source checkpoint

**Files likely touched:** `app/build.gradle`

**Estimated scope:** Small

## Task 15: Update site in an isolated worktree

**Description:** Create `mysite-sudoku-play` from clean site `master`, update the Sudoku page for release 1.4/relaxed mode and current Play assets, and keep privacy claims stable unless app behavior changed.

**Acceptance criteria:**
- [ ] Primary dirty `mysite` worktree is unchanged.
- [ ] Product copy matches release 1.4 and does not claim Play availability prematurely.
- [ ] Stable product/privacy URLs remain unchanged.

**Verification:**
- [ ] Review the isolated diff and screenshots at mobile/desktop sizes.
- [ ] Confirm privacy URL returns HTTP 200.

**Dependencies:** Tasks 10-12

**Files likely touched:** `app/portfolio/sudoku/page.tsx`, `public/images/sudoku-levels.webp`, `public/images/sudoku-light-start.webp`, `public/images/sudoku-dark-candidates.webp`, `public/images/sudoku-dark-solved.webp`

**Estimated scope:** Medium

## Task 16: Verify and integrate site change

**Description:** Run full site gates in the isolated worktree, browser-test both Sudoku pages, and integrate without mixing feedback-system work.

**Acceptance criteria:**
- [ ] Tests, typecheck, lint, and production build pass.
- [ ] Product and privacy pages render correctly in a real browser.
- [ ] Integration strategy preserves the other agent's uncommitted files.

**Verification:**
- [ ] `npm test`, `npm run typecheck`, `npm run lint`, `npm run build` pass.
- [ ] Browser console/network show no release-page errors.

**Dependencies:** Task 15

**Files likely touched:** site test files only if a regression guard is required

**Estimated scope:** Small

## Task 17: Produce signed artifacts

**Description:** Create a fresh private upload key, configure local ignored signing properties, produce the approved Play AAB and RuStore APK, and verify certificates/checksums.

**Acceptance criteria:**
- [ ] AAB is signed by the approved upload key.
- [ ] APK is signed by the approved RuStore/app-signing key.
- [ ] Private files remain outside Git and chat.

**Verification:**
- [ ] `jarsigner` verifies AAB; `apksigner` verifies APK.
- [ ] SHA-256 checksums and public certificate fingerprints are recorded.

**Dependencies:** Tasks 3, 8, 14

**Files likely touched:** local ignored `keystore.properties`; `docs/releases/1.4.md` receives public facts only

**Estimated scope:** Small

## Task 18: Create app and configure package/signing in Play Console

**Description:** Create the Game/Sudoku record, reserve `info.javaway.sudoku`, complete developer verification, configure Play App Signing, and provide a verification APK if requested.

**Acceptance criteria:**
- [ ] Play app exists with the permanent correct package name.
- [ ] Package ownership status is registered/verified.
- [ ] Play App Signing shows the approved app-signing and upload certificates.

**Verification:**
- [ ] Compare Play Console fingerprints with Task 3/17 records.
- [ ] Save no secrets in browser screenshots or repository files.

**Dependencies:** Task 17

**Files likely touched:** `docs/releases/1.4.md`; temporary verification asset only if supplied by Play Console

**Estimated scope:** Medium

## Task 19: Complete listing and declarations

**Description:** Enter both localized listings, assets, store settings, privacy/support contacts, Data safety, ads/access/audience/rating, and all applicable declarations.

**Acceptance criteria:**
- [ ] Play dashboard shows no incomplete required setup item.
- [ ] Uploaded copy/assets match Task 12 files exactly.
- [ ] Declarations are truthful and approved legal attestations are completed by the owner.

**Verification:**
- [ ] Review every summary screen before submission.
- [ ] Confirm public URLs return HTTP 200.

**Dependencies:** Tasks 12, 16, 18

**Files likely touched:** `docs/releases/1.4.md`

**Estimated scope:** Medium

## Task 20: Publish Internal testing release

**Description:** Upload the signed AAB, resolve ingestion warnings, add trusted testers, publish Internal testing, and install through Google Play.

**Acceptance criteria:**
- [ ] Bundle is accepted with correct package, version, target SDK, and signing certificate.
- [ ] Internal release is available to the tester account.
- [ ] Play-delivered install passes critical smoke tests.

**Verification:**
- [ ] Inspect App Bundle Explorer and tester opt-in/install flow.
- [ ] Repeat critical runtime checklist on the Play-generated build.

**Dependencies:** Task 19

**Files likely touched:** `docs/releases/1.4.md`

**Estimated scope:** Medium

## Task 21: Promote through the available release path

**Description:** Resolve pre-launch findings, then either start the mandatory closed test or submit production with managed publishing.

**Acceptance criteria:**
- [ ] No unresolved release-blocking pre-launch crash, ANR, policy, or compatibility issue remains.
- [ ] Required testing track is active with correct testers, or production review is submitted.
- [ ] Rollout state and next date/action are recorded.

**Verification:**
- [ ] Review pre-launch report and Publishing overview.
- [ ] If closed testing applies, verify at least 12 opted-in testers and record the first eligible production-access date.

**Dependencies:** Task 20

**Files likely touched:** `docs/releases/1.4.md`

**Estimated scope:** Medium

## Task 22: Finalize release records and tags

**Description:** Record exact source/artifact identity, rollback path, console status, and tag verified release commits without storing binaries or secrets.

**Acceptance criteria:**
- [ ] Android and website source commits are identified.
- [ ] Artifact hashes, public certificate fingerprints, console track, and rollback steps are documented.
- [ ] Verified release tags are pushed without rewriting history.

**Verification:**
- [ ] `git status` is clean in all owned worktrees.
- [ ] Tags resolve to the commits used for uploaded/deployed artifacts.

**Dependencies:** Task 21

**Files likely touched:** `docs/releases/1.4.md`

**Estimated scope:** Small
