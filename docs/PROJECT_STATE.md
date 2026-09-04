# Pulse — Project State

_Companion to `docs/PRD.md`. Kept current at every milestone hand-off; update this file when you change architecture, tokens, routes, or persistence._

Last updated: 2026-09-04 (pre-M4 audit). Head: `575a11b` + audit commit(s).

---

## 1. Architecture overview & package map

Phase 1 = UI-first with offline mock data. MVVM + Unidirectional Data Flow throughout:
`screens (Compose, UiState) → ViewModels (StateFlow) → repository interfaces (domain) → mock/local implementations (data) → Hilt`.

```
com.pulse.messenger/
├── MainActivity.kt          # host: PulseTheme(darkTheme, accent) from SettingsRepository
├── PulseApplication.kt      # Hilt app
├── ui/
│   ├── theme/               # DESIGN TOKENS (see §2) - Color, Spacing, Shape, Type, Theme
│   ├── icons/AppIcons.kt    # the ONLY icon source: 117 Lucide-derived ImageVectors
│   ├── components/          # design-system components + previews (see §3)
│   ├── util/                # MessagePresentation: TimeFormat, MessageLabels
│   ├── navigation/          # Routes.kt (route consts) + PulseApp.kt (NavHost, auth router)
│   └── screens/             # one feature package per screen family
│       ├── splash/ auth/ welcome/ login/ signup/ otp/ forgot/ profile/
│       ├── permissions/ friends/ success/          # M1–M2
│       ├── main/            # S18 shell + stub screens (NewChat, Conversation)
│       ├── chats/           # S19 list (+ ChatRowsPreviews)
│       ├── search/          # S20 global search
│       ├── archived/        # S21 archived chats
│       └── folders/         # S22 chat folders editor
├── domain/
│   ├── model/               # ChatMessage.kt (Chat/ChatKind/Message/MessageType/MessageStatus),
│   │                        # ChatSummary.kt, ChatFolder.kt, User.kt, Session.kt, Auth.kt
│   └── repository/          # interfaces only: Auth, Chat, Contacts, Folders,
│                            # SearchHistory, Settings, UserRepository
├── data/
│   ├── mock/                # MockAuth/Chat/Contacts/UserRepository, SeedData, Simulator
│   ├── local/               # Folders/SearchHistory/SettingsRepositoryImpl (DataStore+org.json)
│   └── remote/              # reserved for Phase 2 Supabase (empty by design)
└── di/                      # RepositoryModule (@Binds), Qualifiers (@ApplicationScope), CoroutineModule
```

### Rules that hold
- Screens never import `data/*`; ViewModels depend on `domain/repository` interfaces.
- Repository interfaces are the swap point for Phase 2: `ui` consumes interfaces only.
- **Swapping `data/mock` → Supabase requires zero UI changes.** All repository streams are
  cold flows or StateFlow of domain models; `ChatSummary` (row aggregates incl. typing, drafts,
  verified, media labels) is computed inside the repository. UI never joins data itself.
  Only nuance: auth screens display repository-supplied error strings (`exception.message`);
  a remote implementation must map failures to human text the same way (keep messages in the
  data layer or return sealed errors later - existing call sites only consume the string).

## 2. Design system summary

All tokens live in `ui/theme/`, applied via `PulseTheme`; screens must never hardcode
colors/typography/spacing/radius. Tokens are **internal**; consume through the objects below.

| Token file | What it holds | How to consume |
|---|---|---|
| `Color.kt` | `PulseColors` light+dark palette (background, surface, surfaceVariant, border, textPrimary/Secondary/Tertiary, accent, accentContainer, onAccent*, success, warning, error, info, …), `AccentPresets` (6 ids), `AvatarTones` | `PulseTheme.colors.textSecondary` |
| `Spacing.kt` | `PulseSpacing` (4/8 dp grid: xs=4…screen=20, xxxl=32…), `PulseIconSizes` (inline=20, default=24, feature=28), `PulseSizes` (minTouchTarget=48, buttonHeight=52, avatarChat=52, topBarHeight=56, inputHeight=56, dividerHairline=1, chatRowDividerInset=84, fabClearance=96) | `Modifier.padding(PulseSpacing.screen)` |
| `Shape.kt` | `PulseShapes` (sm/md/lg/full radii, sheetTop) | `Modifier.clip(PulseShapes.full)` |
| `Type.kt` | `PulseTypography` over bundled Inter (regular/medium/semibold `.otf` in `res/font`, license `licenses/Inter-LICENSE.txt`) | `MaterialTheme.typography.bodyMedium` |
| `Theme.kt` | `PulseTheme` composable + `PulseColors` accessor; **never** the M3 `MaterialTheme.colorScheme` defaults | wrap app in `PulseTheme(darkTheme, accentPreset)` |

**Icons:** `AppIcons.*` only (117 stroke icons, single source, full multi-primitive geometry).
No material-icons imports, no vector drawables, no emoji-as-icons anywhere in UI text.
Zero gradients (`Brush.*`), zero default-Material palette colors, zero hardcoded `Color(0x…)`
outside `Color.kt`/`Theme.kt` scrim/inverse values.

**Example**
```kotlin
@Composable
fun DemoRow() {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Pin, null, tint = c.textTertiary, modifier = Modifier.size(PulseIconSizes.inline))
        Spacer(Modifier.width(PulseSpacing.md))
        Text("Title", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
    }
}
```

**Documented exceptions (deliberate metrics, not grid spacing):** 2 dp text-line gaps inside
list rows (typography leading), 84 dp avatar-column inset (now token `PulseSizes.chatRowDividerInset`),
avatar 44 dp in search rows, skeleton bar heights 12–14 dp, badge offsets in `MainScreen`, divider
inset/micro paddings in `ChatListItem` trailing glyphs (14/13/16/20/22 dp are glyph metrics with no
token yet - M4 may add `PulseIconSizes.tiny`). Keep this list honest when you touch these files.

## 3. Component catalog (`ui/components/`)

| Component (file) | Purpose | Key params |
|---|---|---|
| `AppTopBar` + `AppBackButton` (AppTopBar.kt) | Large/Medium/Compact top bar, hairline option | title, style, navigationIcon, actions |
| `AppButton`, `AppIconButton` (AppButton.kt) | 52 dp CTA (Primary/Secondary/Tertiary/Destructive, loading, disabled); 48 dp icon button | text/icon, variant, loading, enabled, fillMaxWidth |
| `AppTextField` (AppTextField.kt) | Themed input: label, placeholder, error, leading/trailing icons, password toggle | value, onValueChange, label, isError, isPassword |
| `OtpInput` (OtpInput.kt) | 6-box code entry, auto-advance | onComplete, length, isError, resetSignal |
| `AuthStepScaffold` (AuthStepScaffold.kt) | Auth screens: back + step progress + title + content + pinned CTA | steps, stepIndex, title, cta |
| `Avatar` (+ group composite) (Avatar.kt) | Initials/photo circle 24–120 dp, online dot, group stack | name, avatarTone, size, isOnline, isGroup |
| `ChatListItem` (ChatListItem.kt) | S19/S21 chat row: 52 dp avatar, preview, badges, ticks, selection | summary, onClick, onLongClick, selectionMode, selected |
| `SwipeableRow` (SwipeableRow.kt) | Swipe actions container (snap-back, M3 SwipeToDismissBox) | startAction, endAction, enabled, content |
| `SelectionCheck`, `DeliveryTicks` (ChatListItem.kt) | Multi-select circle; single/double ticks | selected/status |
| `AppChip`, `Badge` (Badges.kt) | Filter chip w/ leading icon; count pill (muted variant) | label, selected; count, muted |
| `SearchBar` (SearchBar.kt) | Collapsed pill → expanded field with back | value, active, onActiveChange, placeholder |
| `AppBottomSheet` (AppBottomSheet.kt) | Modal sheet, drag handle, lg radius | onDismissRequest, sheetState, content |
| `CountryPickerSheet` (CountryPickerSheet.kt) | Searchable ISO countries (text badges, no flags) | onDismiss, onSelect |
| `AppCard`, `AppDivider` (AppCard.kt) | Surface card; hairline divider w/ inset | content; insetStart |
| `AppDialog`, `ConfirmDialog` (AppDialog.kt) | Info dialog; destructive confirm | title/text/buttons |
| `Controls`: `SegmentedControl`, `AppSwitch` (Controls.kt) | M3 segmented; themed switch | options/selected; checked |
| `SettingsItems` (SettingsItems.kt) | Row/switch/radio settings rows | icon, title, checked… |
| `States` (States.kt) | `EmptyState`, `ErrorState`, `SkeletonBox/Circle`, skeleton list rows | icon, title, subtitle, action; onRetry |
| `SuccessScreen` (SuccessScreen.kt) | Full success w/ CTA (account/reset) | title, subtitle, buttonLabel, onButton |
| `LogoMark` (LogoMark.kt) | Brand mark | size |
| `PasswordStrength` (PasswordStrength.kt) | Meter + label | score |
| `PermissionCard` (PermissionCard.kt) | S16 permission row | icon, title, reason, granted, onAllow |

**Duplication findings:** no true duplicates (one chip = `AppChip`, one top bar = `AppTopBar`
variants, one list row = `ChatListItem`). Watch items:
1. `ChatsScreen` header row (title+search+chips) vs `AppTopBar` Large — kept separate because it hosts
   collapsible search + chip row + more menu (a future `ChatTopBar` variant could unify; defer to M4b).
2. `GlobalSearchScreen` header replicates an *expanded* `SearchBar` with clear button; acceptable
   (different chrome: back/field/clear only), but M4 may refactor SearchBar to expose this mode.
3. `SuccessScreen` vs `EmptyState` share layout DNA; both stay (different semantics).

**Previews:** every component has light+dark `@Preview` pairs — M1 catalog in
`ComponentsPreviews.kt`, M3 rows in `screens/chats/ChatRowsPreviews.kt`, audit gap-fill in
`ui/components/ComponentShowcases.kt` (SwipeableRow, OtpInput, PasswordStrength, PermissionCard,
SuccessScreen). Not previewed (documented reasons): `CountryPickerSheet` and `AuthStepScaffold`
(interactive modals/IME scaffolding).

## 4. Navigation route table

`ui/navigation/Routes.kt` consts; destinations registered in `PulseApp.kt`.

| Route | Screen | Status |
|---|---|---|
| `splash` | S01 Splash (routes by persisted session) | ✅ IMPLEMENTED |
| `welcome` | S02 Welcome | ✅ |
| `login/email` | S03 | ✅ |
| `login/phone` | S03b | ✅ |
| `login/password` | S04 | ✅ |
| `otp` | S06/S11b OTP | ✅ |
| `forgot/email` `forgot/inbox` `forgot/reset` `success/reset` | S07–S10 | ✅ |
| `signup/email` `signup/password` | S11a/c | ✅ |
| `profile/name` `profile/username` `profile/photo` `profile/bio` | S12–S15 | ✅ |
| `permissions` | S16 | ✅ |
| `find-friends` | S17 | ✅ |
| `success/account` | S17 completion | ✅ |
| `main` | S18 Main shell | ✅ tabs: Chats REAL; Calls/People/Settings = PLACEHOLDER |
| `search` | S20 | ✅ |
| `archived` | S21 | ✅ |
| `folders` | S22 | ✅ |
| `new-chat` | New chat | ⏳ PLACEHOLDER stub (M4) |
| `chat/{chatId}` | Conversation | ⏳ PLACEHOLDER stub (M4 will replace) |

**Back behavior:** system back inside `Main` is intercepted: non-Chats tab → Chats;
multi-select active → clears selection; Chats idle → exits app. All other pushed screens pop to the
tab shell.

## 5. Domain model summary

| Model | Fields (current) |
|---|---|
| `User` | id, firstName, lastName, username, phone?, bio?, avatarSeed (displayName derived) |
| `Chat` | id, kind(Direct/Group), title?, participantIds, avatarSeed, isArchived/Muted/Pinned, updatedAtMillis |
| `ChatSummary` | row aggregate: chatId, kind, displayName, avatarSeed, peerFirstName, memberNames, participantCount, lastMessage?, lastSenderFirstName?, unreadCount, isArchived/Muted/Pinned/Typing, draft?, isVerified |
| `Message` | id, chatId, senderId, type, text, sentAtMillis, status(Sending/Sent/Delivered/Read/Failed), isOutgoing |
| `ChatFolder` | id, name, includeKinds(Set<ChatKind>), onlyUnread |
| `SessionState` | Unknown / LoggedOut / LoggedIn(user); `ThemeMode` System/Light/Dark |
| `GoogleAccount`, `SignInResult`, `CountryCode`, `OtpChannel` | auth support models |

Gaps vs PRD §6.5/S23 (Message attachments/replies/reactions; Chat admins/pinned/disappearing/
wallpaper/blocked/permissions; User online/lastSeen/verified/blocked/email): see
**docs/M4_READINESS.md** — they are M4a work, deliberately not implemented here.

## 6. Repository interfaces & mocks

| Interface | Mock / impl | Backing |
|---|---|---|
| `AuthRepository` | `MockAuthRepository` | in-memory + **DataStore `pulse_session`** (session survives restart) |
| `ChatRepository` | `MockChatRepository` (@Singleton) | in-memory `ChatState` store; typing pulses; seeded messages |
| `ContactsRepository` | `MockContactsRepository` | SeedData contacts flow |
| `FoldersRepository` | `FoldersRepositoryImpl` | DataStore `pulse_folders` (JSON) |
| `SearchHistoryRepository` | `SearchHistoryRepositoryImpl` | DataStore `pulse_search_history` (JSON) |
| `SettingsRepository` | `SettingsRepositoryImpl` | DataStore `pulse_settings` (theme_mode, accent_preset_id) |
| `UserRepository` | `MockUserRepository` | returns `User.Me`; **not consumed by UI yet** (reserved for M5/S54; wire to session user when the first consumer lands) |

All bound in `di/RepositoryModule.kt` via `@Binds`; `@ApplicationScope` qualifier + coroutine
module in `di/`.

## 7. Mock behaviors & seed data

- Auth (PRD §9): valid email + password ≥ 8 chars succeeds; literal password `"wrong"` fails;
  OTP `123456` succeeds else fails; Google picker always succeeds (existing →
  `aarav.kapoor@gmail.com` → Main; others → profile setup). `Simulator.networkDelay()` = 300–800 ms,
  `shortDelay()` = 120–260 ms.
- Chats: 15 seeded chats (11 direct + 4 groups; archived `c-zara`, `c-tara`; muted `c-kabir`,
  `c-lea`, `c-roadtrip`; pinned `c-noah`, `c-iva`; unread counts aria 3 / design 2 / roadtrip 5 /
  sam 1 / run 2 / kabir 4; draft on `c-noah`; typing pulses on `c-aria` 12–24 s on / 7 s off).
  33 messages total (1–4 per chat, mixed text/media captions, timestamps anchored to
  `SeedData.BaseMillis` and offset to now at first use). 16 contacts (`u-aria`…`u-tara`), `me` =
  User("me","Aarav","Kapoor","aaravk","+91 98110 90000","Building Pulse.",3).
- Chat actions: archive/unarchive, pin, mute, mark read, delete, mark-all-read, pull-to-refresh —
  all mutate the singleton store and emit updated summaries.
- Session: persisted via DataStore so Splash routes LoggedIn → Main after restart.
- NOT yet simulated (M4): send → delivery status progression, auto-replies (PRD §9: ~40% chats,
  1.5–4 s, typing first), incoming message/call triggers (S74 debug menu).

## 8. Persistence summary

| Store | What | Survives restart? |
|---|---|---|
| DataStore `pulse_settings` | theme_mode, accent_preset_id | ✅ |
| DataStore `pulse_folders` | folders_json (user chat folders) | ✅ |
| DataStore `pulse_search_history` | recent_json | ✅ |
| DataStore `pulse_session` | session_json (logged-in user) | ✅ (added in audit) |
| Room | — (none; PRD §9 mandates Room for messages/chats → **M4a decision**, see readiness doc) | — |
| In-memory mocks | chat flags, unread counts, typing, contacts | ❌ reset each launch (Phase-1 mock store; Room or Supabase replaces in M4a/Phase 2) |

## 9. Milestone status

| Milestone | Scope | Status | Notes |
|---|---|---|---|
| M1 | Theme/tokens/icons/components foundation, splash | ✅ `334d292` | |
| M2 | Onboarding & auth S01–S17 | ✅ `d1b0be7` | icon-set regeneration fix `5bdc993` |
| M3 | Main shell & chats S18–S22 | ✅ `bae7bdc`→`575a11b` | |
| M4 | Conversation S23–S40 | ⏳ PENDING | plan in `docs/M4_READINESS.md` |

**Known limitations (accepted):** Calls/People/Settings tabs and several More-menu items are honest
placeholders (later milestones); logout exists in `AuthRepository`/`AuthViewModel` but has no UI
until Settings (S68, M6+); no Room; mock state resets between launches; chat list filters do not
persist; time labels & preview strings are English-only; Day/time labels ("Yesterday") and
`MessageLabels` are English util constants.

## 10. How to add a new screen (checklist)

1. Put it in `ui/screens/<feature>/`; one file per screen unless it grows past ~400 lines — then split
   into private composable sections or subcomponents (screen file guideline).
2. Create `UiState` data class + `@HiltViewModel` exposing `StateFlow<UiState>`; collect with
   `collectAsStateWithLifecycle()`. No business logic in composables.
3. Add repository interface method(s) in `domain/repository` first; implement in the matching mock/
   local impl; bind via `di/RepositoryModule` if new.
4. Add route consts in `ui/navigation/Routes.kt`; register `composable(...)` in `PulseApp.kt`;
   wire callbacks/back handling from the host screen.
5. Use ONLY tokens: `PulseTheme.colors`, `MaterialTheme.typography`, `PulseSpacing/IconSizes/Sizes`,
   `PulseShapes`, `AppIcons`. No gradients, no emoji icons, no `Color(0x…)` in screens.
6. Centralize every user-visible string in `res/values/strings.xml` (no literals; plurals allowed
   with %d; watch apostrophes).
7. Reuse components from §3 before writing new ones; if new, add light+dark `@Preview`.
8. Icons on tappables get `contentDescription`; decorative icons get `null`.
9. Add `@Preview` (light + dark) for the key states of reusable parts.
10. Keep `data/mock` and `data/local` as the only data sources; never call Supabase or network.

---

_Update this file whenever a milestone ships._
