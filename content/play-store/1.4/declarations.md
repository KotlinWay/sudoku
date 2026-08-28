# Google Play declarations — Sudoku 1.4

This workbook maps the verified application behavior to the expected Play Console setup and App content forms. Console wording can change; preserve the meaning below rather than guessing around a new question.

## Verified evidence

- Package: `info.javaway.sudoku`; version 1.4 (code 5); target SDK 36.
- The release manifest declares no Android permissions, including `INTERNET`, `AD_ID`, sensitive permissions, or high-risk permissions.
- The app declares no runtime libraries or SDKs; JUnit is test-only. AGP injects `kotlin-stdlib` into the intermediate release classpath, but R8 removes all 969 Kotlin entries: no Kotlin or other third-party classes remain in the release mapping or packaged DEX.
- Puzzles, unfinished games, settings, and statistics are generated or stored only on the device.
- Android cloud backup and device-to-device transfer are disabled and explicitly excluded by backup rules.
- The app opens specific external links through Android `ACTION_VIEW`; it does not contain a WebView or transmit data itself.
- Privacy policy: `https://javaway.info/sudoku-privacy-policy` (HTTP 200 verified 2026-08-28).
- Support website: `https://javaway.info/portfolio/sudoku` (HTTP 200 verified 2026-08-28).
- Support email: `max.simple.apps@gmail.com`.

## App setup and store settings

| Console field | Answer |
| --- | --- |
| App name | Sudoku |
| Default language | English (United States), `en-US` |
| App or game | Game |
| Free or paid | Free |
| Category | Puzzle |
| Tags | Sudoku; Puzzle; Offline (use only tags offered by Console) |
| Monetization | None |
| In-app products/subscriptions | None |
| Countries/regions | All locations permitted by the account and Play policies |
| Contact website | `https://javaway.info/portfolio/sudoku` |
| Privacy policy | `https://javaway.info/sudoku-privacy-policy` |

Do not change a published app from free to paid later; Play does not allow that transition. No price template, products, subscriptions, or merchant setup is needed for this app.

## Ads

- **Does the app contain ads? — No.**
- There is no ad SDK, banner, interstitial, native ad, sponsored placement, or paid product placement.
- A non-interruptive card for another app by the same developer appears only at the bottom of Settings and opens the relevant store. It does not interrupt gameplay. This matches Play's documented “More Apps” exception to the ads label; revisit the answer if the implementation becomes an ad banner, interstitial, ad wall, or gameplay interruption.

## App access

- **Are all features available without special access? — Yes.**
- Select the equivalent of **All functionality is available without special access**.
- No account, login, membership, location restriction, one-time password, or reviewer instructions are needed.

## Target audience and content

- Select ages **13–15**, **16–17**, and **18 and over**.
- Do not select any age group below 13.
- The app is not primarily directed at children and should not be enrolled in Designed for Families.
- Store listing, screenshots, and feature graphic contain no child-directed characters, school framing, or child-specific claims.
- If Console asks whether the app intentionally appeals to children: **No**.

The target audience selection is a product declaration, not an attempt to influence the IARC result. Accept the rating generated from truthful content answers.

## Content rating (IARC)

- Category: **Game**.
- Enter the developer contact email requested by IARC.
- Violence or violent references: **No**.
- Fear/horror content: **No**.
- Sexual content or nudity: **No**.
- Profanity or crude humor: **No**.
- Alcohol, tobacco, or drugs: **No**.
- Gambling, simulated gambling, or contests: **No**.
- User-generated content or user-to-user communication: **No**.
- Sharing precise location or personal information: **No**.
- Digital purchases or paid random items: **No**.
- Ads: **No**.
- Unrestricted web access: **No**. The app has no browser or WebView; fixed links open a separate browser/store/email app after a user action.
- Online gameplay or network interaction: **No**.

Expected outcome is a low/all-ages regional rating, but record and use the rating returned by IARC rather than pre-declaring a specific result.

## Data safety

| Console question | Answer |
| --- | --- |
| Does the app collect any required user data types? | No |
| Does the app share user data with other companies or organizations? | No |
| Is data sent off the user's device by the app or an embedded SDK? | No |
| Privacy policy URL | `https://javaway.info/sudoku-privacy-policy` |

On-device game state, preferences, and statistics are outside Play's definition of collected data because they are never transmitted off device. The external browser/store handles a user-requested fixed URL independently; the app passes no user data in that URL. Encryption-in-transit and server-side retention questions are not applicable because the app transmits and retains no user data off device.

Account creation and account deletion are not applicable: the app cannot create an account and the developer holds no user data. Users can erase local statistics inside the app, clear app storage, or uninstall the app.

## Advertising ID and permissions

- Advertising ID use: **No**.
- `com.google.android.gms.permission.AD_ID`: **not declared**.
- Sensitive/high-risk permissions: **none**.
- Permissions Declaration Form: **not required** unless Console reports a permission not present in the inspected bundle.
- Photos/videos, all-files access, SMS/call log, background location, VPN, exact alarms, accessibility service, and foreground-service declarations: **not applicable**.

## Financial features

- Select **My app doesn't provide any financial features**.
- The app has no loans, banking, payments, wallets, money transfers, purchase agreements, rewards, cryptocurrency, trading, crowdfunding, insurance, credit reporting, or financial advice.

## Health apps

- Select **My app doesn't provide any health features**.
- The app does not access health data and makes no medical, fitness, wellness, diagnosis, treatment, or research claim.

## Government apps

- The app is not developed by or on behalf of a government entity and does not communicate government information: **No**.

## News and Magazine apps

- The app is not a News or Magazine app and contains no news or periodical content: **No**.

## COVID-19 declaration

- The app provides neither contact tracing nor COVID-19 status functionality: select the **none/not a COVID-19 app** answer.

## Monetization and commerce

- Paid app: **No**.
- Ads: **No**.
- In-app purchases: **No**.
- Subscriptions: **No**.
- External purchase links or payment flows: **No**.
- Play Games Services: **No**.

## Ownership and legal attestations

The owner must personally review and accept any Console legal attestations about:

- ownership or sufficient rights to the application, icon, feature graphic, screenshots, and listing text;
- compliance with Play policies and export/sanctions requirements;
- accuracy of App content and Data safety declarations;
- Play App Signing terms and any package-name ownership proof;
- country/region availability and developer contact details.

Do not accept a newly presented legal agreement or a different app-signing identity without owner review. If Console shows a declaration not covered above, save it as a draft and inspect the exact wording before answering.

## Official references checked 2026-08-28

- Store listing and preview assets: https://support.google.com/googleplay/android-developer/answer/9866151
- App review declarations: https://support.google.com/googleplay/android-developer/answer/9859455
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Target audience: https://support.google.com/googleplay/android-developer/answer/9867159
- Content ratings: https://support.google.com/googleplay/android-developer/answer/9859655
- Financial features: https://support.google.com/googleplay/android-developer/answer/13849271
- Health apps: https://support.google.com/googleplay/android-developer/answer/14738291
- Government apps: https://support.google.com/googleplay/android-developer/answer/9514050
