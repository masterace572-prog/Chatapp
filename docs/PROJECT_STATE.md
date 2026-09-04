# Pulse — Project State

_Companion to `docs/PRD.md`. Kept current at every milestone hand-off; update this file when you change architecture, tokens, routes, or persistence._

Last updated: 2026-09-04 (M4a). Head: `e8abc9c` + M4a commits.

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
│   ├── icons/AppIcons.kt    # the ONLY icon source: Lucide-derived ImageVectors
│   ├── components/          # design-system components + previews (see §3)
│   ├── util/                # MessagePresentation: TimeFormat, MessageLabels, ConversationFormat
│   ├── navigation/          # Routes.kt (route consts) + PulseApp.kt (NavHost, auth router)
│   └── screens/             # one feature package per screen family
│       ├── splash/ auth/ welcome/ login/ signup/ otp/ forgot/ profile/
│       ├── permissions/ friends/ success/          # M1-M2
│       ├── main/            # S18 shell + NewChat stub
│       ├── chats/           # S19 list (+ ChatRowsPreviews)
│       ├── search/          # S20 global search
│       ├── archived/        # S21 archived chats
│       ├── folders/         # S22 chat folders editor
│       └── conversation/    # S23 chat screen (M4a)
├── domain/
│   ├── model/               # ChatMessage.kt (model v2: MessageContent sealed hierarchy,
│   │                        # GroupMember/ChatRole, ChatPermissions), ChatSummary.kt,
│   │                        # ChatFolder.kt, User.kt, Session.kt, Auth.kt
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
  hot StateFlows of domain models; `ChatSummary` (row aggregates incl. typing, drafts, verified,
  media labels) is computed inside the repository. The conversation screen receives resolved
  `Chat`, `Message`, typing-set and summary flows; the ViewModel joins them with the contacts
  directory (same pattern the search VM uses) but never touches data-layer code.
- Messages live on `ChatRepository` (not a separate MessageRepository): a conversation and its
  messages are one aggregate — summaries derive from the last message, unread counts track the
  message stream, and drafts edit what the list previews. The Phase-1 mock keeps both in one
  singleton, so a split contract would fork a single source of truth.

## 2. Design system summary

All tokens live in `ui/theme/`, applied via `PulseTheme`; screens must never hardcode
colors/typography/spacing/radius. Tokens are **internal**; consume through the objects below.

| Token file | What it holds | How to consume |
|---|---|---|
| `Color.kt` | `PulseColors` light+dark palette (+ `isDark` flag), `AccentPresets` (6 ids), `AvatarTones` (8 pairs), `senderToneTextColor()` (group sender names) | `PulseTheme.colors.textSecondary` |
| `Spacing.kt` | `PulseSpacing` (tight=2 … huge=40, screen=20), `PulseIconSizes` (tiny=14, small=16, inline=20, default=24, feature=28), `PulseSizes` (buttonHeight 52, avatarChat 52, avatarHeader 40, avatarRun 24, minTouchTarget 48, topBar/inputHeight 56, dividerHairline 1, chatRowDividerInset 84, fabClearance 96, bubbleMetaIcon 14, unreadOverlayBadge 14, selectionCheck 22/CheckMark 13/stroke 2, sendCircle 40) | `Modifier.padding(PulseSpacing.screen)` |
| `Shape.kt` | `PulseShapes` (xs…xl, full, sheetTop) + conversation chrome: bubbleRadius 18, bubbleTightRadius 4, composerRadius 24, `PulseBubbleShape(outgoing, firstInRun, lastInRun)` | `Modifier.clip(PulseShapes.full)` |
| `Type.kt` | `PulseTypography` over bundled Inter; `PulseFontWeights` (Regular/Medium/SemiBold); `avatarInitialsFontSize(avatarSizeDp)` | `MaterialTheme.typography.bodyMedium` |
| `Theme.kt` | `PulseTheme` composable + `PulseColors` accessor; **never** the M3 `MaterialTheme.colorScheme` defaults | wrap app in `PulseTheme(darkTheme, accentPreset)` |

**Icons:** `AppIcons.*` only (117 stroke icons, single source, full multi-primitive geometry).
No material-icons imports, no vector drawables, no emoji-as-icons anywhere in UI text (emoji
inside message *content* is user data and fine). Zero gradients, zero default-Material palette
colors, zero hardcoded `Color(0x…)` outside `Color.kt`/`Theme.kt`.

**Example**
```kotlin
@Composable
fun DemoBubble() {
    val c = PulseTheme.colors
    MessageBubble(message = myMessage)   // components own their chrome
}
```

**Documented exceptions (deliberate metrics, not grid spacing):** avatar letter fraction
(tokenised via `avatarInitialsFontSize`), 2 dp run gaps (token `PulseSpacing.tight`), bubble max
width 78% of the row (code-level constraint), structural metrics like country-flag badge
40×28 and meter segment widths. Keep this list honest when you touch these files.

## 3. Component catalog (`ui/components/`)

| Component (file) | Purpose | Key params |
|---|---|---|
| `AppTopBar` + `AppBackButton` (AppTopBar.kt) | Large/Medium/Compact top bar, hairline option | title, style, navigationIcon, actions |
| `ChatHeader` (ChatHeader.kt) | S23 conversation top bar: back + unread overlay badge, 40dp avatar, name/status line, voice/video/more, dropdown menu | title, statusText, status(Online/Typing/Group/Neutral), avatarSeed, otherUnread, onMenuAction |
| `AppButton`, `AppIconButton` (AppButton.kt) | 52dp CTA (Primary/Secondary/Tertiary/Destructive, loading, disabled); 48dp icon button | text/icon, variant, loading, enabled, fillMaxWidth |
| `AppTextField` (AppTextField.kt) | Themed input: label, placeholder, error, leading/trailing icons, password toggle | value, onValueChange, label, isError, isPassword |
| `OtpInput` (OtpInput.kt) | 6-box code entry, auto-advance | onComplete, length, isError, resetSignal |
| `AuthStepScaffold` (AuthStepScaffold.kt) | Auth screens: back + step progress + title + content + pinned CTA | steps, stepIndex, title, cta |
| `Avatar` (+ group composite) (Avatar.kt) | Initials/photo circle 24–120dp, online dot, group stack | name, avatarTone, size, isOnline, isGroup |
| `ChatListItem` + `SelectionCheck`, `DeliveryTicks` (ChatListItem.kt) | S19/S21 row incl. all badges/ticks; ticks now cover Sending(clock)/Failed(alert) | summary, onClick, onLongClick, selectionMode, selected |
| `SwipeableRow` (SwipeableRow.kt) | Swipe actions container (snap-back) | startAction, endAction, enabled, content |
| `MessageBubble` (MessageBubble.kt) | S23 bubble: text + tappable links, placeholders for media types, edited/deleted/failed/retry, run geometry, group names + run avatar | message, isFirstInRun/isLastInRun, senderName, senderNameColor, avatarSeed, onRetry |
| `DateSeparator`, `UnreadDivider`, `SystemMessageRow`, `TypingIndicator` (ChatExtras.kt) | Conversation list chrome (day pill / "N unread" pill / centered system row / 3-dot pulse bubble) | label; senderName for typing |
| `MessageComposer` (MessageComposer.kt) | Basic S23 composer: attachment + growing pill field (6 lines) + camera/mic when empty, crossfade to send circle | value, onValueChange, onSend, onAttachment/onCamera/onMic |
| `AppChip`, `Badge`, `Tag` (Badges.kt) | Filter chip w/ leading icon; count pill; neutral micro-label | label, selected; count, muted |
| `SearchBar` (SearchBar.kt) | Collapsed pill → expanded field with back | value, active, onActiveChange, placeholder |
| `AppBottomSheet`, `CountryPickerSheet` (AppBottomSheet.kt) | Modal sheet, drag handle; searchable ISO picker | onDismissRequest, sheetState |
| `AppCard`, `AppDivider` (AppCard.kt) | Surface card; hairline divider w/ inset | content; insetStart |
| `AppDialog`, `ConfirmDialog` (AppDialog.kt) | Info dialog; destructive confirm | title/text/buttons |
| `Controls`: `SegmentedControl`, `AppSwitch` (Controls.kt) | M3 segmented; themed switch | options/selected; checked |
| `SettingsItems` (SettingsItems.kt) | Row/switch/radio settings rows | icon, title, checked… |
| `States` (States.kt) | `EmptyState`, `ErrorState`, `SkeletonBox/Circle` | icon, title, subtitle, action; onRetry |
| `SuccessScreen`, `LogoMark`, `PasswordStrength`, `PermissionCard` | Misc shared | — |

**Previews:** every component has light+dark `@Preview` pairs. Hubs:
`ComponentsPreviews.kt` (M1), `screens/chats/ChatRowsPreviews.kt` (M3 rows),
`ComponentShowcases.kt` (audit gap-fill), **`ConversationPreviews.kt` (M4a: ChatHeader
online/typing/group, MessageBubble outgoing run/incoming run + system row/placeholders,
DateSeparator, UnreadDivider, TypingIndicator, Composer empty/typing/multiline, full
`ConversationContent` with seeded history)**. Not previewed: `CountryPickerSheet`,
`AuthStepScaffold` (interactive modals/IME scaffolding).

## 4. Navigation route table

`ui/navigation/Routes.kt` consts; destinations registered in `PulseApp.kt`.

| Route | Screen | Status |
|---|---|---|
| `splash` | S01 Splash (routes by persisted session) | ✅ |
| `welcome` `login/*` `otp` `forgot/*` `signup/*` `profile/*` `permissions` `find-friends` `success/*` | S02–S17 | ✅ |
| `main` | S18 shell (Chats tab REAL; Calls/People/Settings placeholders) | ✅ |
| `search` | S20 | ✅ |
| `archived` | S21 | ✅ |
| `folders` | S22 | ✅ |
| **`chat/{chatId}`** | **S23 conversation core (M4a)** | ✅ REAL — stub replaced |
| `new-chat` | New chat | ⏳ stub (M4c/M4d flow) |

**Back behavior:** inside `Main`, back on a non-Chats tab → Chats; multi-select active → clears
selection; Chats idle → exits app. The conversation screen pops back to the tab shell and its
ViewModel release marks the chat closed (mock stops counting unread for it).

## 5. Domain model summary (v2, M4a)

| Model | Fields (current) |
|---|---|
| `User` | id, firstName/lastName, username, phone?, bio?, avatarSeed, **isOnline, lastSeenAtMillis?, isVerified, isBlocked** |
| `Chat` | id, kind(Direct/Group), title?, participantIds, **members: List<GroupMember(userId, role Owner/Admin/Member, joinedAtMillis)>**, description?, createdBy?, createdAtMillis?, avatarSeed, isArchived/Muted/Pinned, updatedAtMillis, **pinnedMessageIds, disappearingMessagesDurationSeconds?, wallpaperId?, isBlocked, permissions: ChatPermissions** (data only until M4d) |
| `ChatSummary` | row aggregate (unchanged shape): chatId, displayName, avatarSeed, peerFirstName, memberNames, participantCount, lastMessage?, lastSenderFirstName?, unreadCount, flags, isTyping, draft?, isVerified |
| `Message` | id, chatId, senderId, **content: MessageContent (sealed)**, sentAtMillis, status(Sending/Sent/Delivered/Read/Failed), isOutgoing, **replyToMessageId?, forwardedFromUserId?, isEdited, isDeleted, isStarred, isPinned, reactions: List<MessageReaction(emoji, userIds)>, linkPreview?** — convenience getters `type`/`text` |
| `MessageContent` | sealed: Text(text), System(text), Image(uris, caption, w/h), Video(uri, duration, w/h, caption), Voice(durationSeconds, waveformSamples), File(name, sizeBytes, mime), Location(lat/lng, address), Contact(userId, displayName, phone?, username?), Poll(question, options, votes map, flags), Sticker(assetKey) — every type exists with payload data in M4a; only Text/System render fully |
| `MessageType` | enum discriminant (Text…Sticker, System) used by lists/search/previews |
| `ChatFolder` | id, name, includeKinds, onlyUnread (unchanged) |
| `SessionState`, `GoogleAccount`, `SignInResult`, `CountryCode`, `OtpChannel` | auth support models (unchanged) |

Remaining S23+ gaps deferred to M4b–M4e are rendering/behavioral only (reactions UI, reply
quotes, media bubbles, pinned banner, group management, message info); the **model supports all
of them already** — no further model migration is planned before Phase 2.

## 6. Repository interfaces & mocks

| Interface | Mock / impl | Backing |
|---|---|---|
| `AuthRepository` | `MockAuthRepository` | in-memory + DataStore `pulse_session` |
| `ChatRepository` | `MockChatRepository` (@Singleton) | in-memory singleton: chat flags/unread/drafts + **live message store + typing sets + send pipeline + auto-replies** |
| `ContactsRepository` | `MockContactsRepository` | SeedData contacts flow (17 incl. Pulse Assistant) |
| `FoldersRepository` | `FoldersRepositoryImpl` | DataStore `pulse_folders` (JSON) |
| `SearchHistoryRepository` | `SearchHistoryRepositoryImpl` | DataStore `pulse_search_history` (JSON) |
| `SettingsRepository` | `SettingsRepositoryImpl` | DataStore `pulse_settings` (theme_mode, accent_preset_id) |
| `UserRepository` | `MockUserRepository` | returns `User.Me`; no UI consumer yet (M5/S54) |

`ChatRepository` surface (interface only grows, never reshapes): list flows
(`observeChatSummaries/ArchivedSummaries`, `observeAllMessages`), conversation surface
(`observeChat(chatId)`, `observeMessages(chatId)` newest-last hot, `observeTyping(chatId):
Flow<Set<String>>`, `sendText(chatId, text, replyToMessageId?)`, `retryMessage(messageId)`,
`markChatRead(chatId)`, `setActiveConversation(chatId?)`, `setDraft(chatId, text)`), M3 actions
(archive/pin/mute/markRead/markAllRead/deleteChats/refresh). All bound via `@Binds` in
`di/RepositoryModule.kt`.

**Mock behaviors (PRD §9 + M4a D-decisions):**
- Send pipeline: Sending → Sent (300–800 ms) → Delivered (+0.5–1.5 s) → Read (+1–3 s, only when
  the chat is "online": peer online for directs, any member online for groups). ~5% of sends end
  Failed (retry UI); `retryMessage` always succeeds.
- Auto-reply (D2): ~40% of chats (`c-assistant`, `c-aria`, `c-noah`, `c-mira`, `c-design`,
  `c-fam`) reply 1.5–4 s after a successful send from pool sentences; the sender's typing
  indicator shows 1–2 s first. **Pulse Assistant (`u-pulse`, chat `c-assistant`) always
  replies** for deterministic demos.
- Unread accounting: incoming replies bump the chat's unread count only while the chat is not on
  screen (`setActiveConversation`); opening the conversation marks it read once the unread
  divider position was captured.
- Drafts: mutable per chat; the chat list shows the "Draft:" prefix (M3), unsent composer text
  survives leaving/re-entering a conversation (S23).
- Idle typing pulses on `c-aria` (M3) keep lists + conversation alive (D4).
- Session: persisted via DataStore `pulse_session` (splash routes LoggedIn → Main after restart).

## 7. Seed data (`data/mock/SeedData.kt`, M4a)

- **People:** 17 contacts (`u-aria`…`u-tara` + **`u-pulse` "Pulse Assistant"**, verified, always
  online) + `me` (Aarav Kapoor). Presence: `u-aria` and `u-pulse` online; everyone else carries a
  `lastSeenAtMillis`.
- **Chats:** 16 (12 direct + 4 groups: Design Guild me-owner; Roadtrip Crew dev-owned; Weekend
  Plans me-owner; Morning Runners iva-owned) with roles (`members`), descriptions, createdAt.
  Flags: archived `c-zara`/`c-tara`, muted `c-kabir`/`c-lea`/`c-roadtrip`, pinned `c-noah`/
  `c-iva`; unread: aria 3 / design 2 / roadtrip 5 / sam 1 / run 2 / kabir 4; draft on `c-noah`;
  typing sim on `c-aria`.
- **Messages: ~441 generated per chat (20–45 each)** spanning ≥2 days (night gaps) so date
  separators show Today / Yesterday / weekday / full date. Deterministic per chat (fixed seed):
  grouped sender runs (2 min window), system rows ("You created the group", "X joined" with real
  actor ids), links, edited + deleted examples, reactions data, and content-type placeholders
  (image/video/voice/file/location/contact/poll/sticker) with realistic payload fields. Unread
  tails are peer-sent by construction; recent chats end minutes before launch, archived chats
  end days ago.
- M3 list/search consumers read the same store: preview text derives from `content.text` with
  `MessageLabels.typeLabel` icons for non-text, search indexes Text content (system rows and
  empty payloads excluded), delivery ticks/labels unchanged.

## 8. Persistence summary

| Store | What | Survives restart? |
|---|---|---|
| DataStore `pulse_settings` | theme_mode, accent_preset_id | ✅ |
| DataStore `pulse_folders` | folders_json (user chat folders) | ✅ |
| DataStore `pulse_search_history` | recent_json | ✅ |
| DataStore `pulse_session` | session_json (logged-in user) | ✅ |
| Room | — (none; PRD §9 mandates Room for messages/chats → **decision D1 (resolved): stay in-memory through M4**; repository methods are all suspend/Flow so Room can be added additively) | — |
| In-memory mock store | chats, flags, unread, drafts, typing, message history + live sends | ❌ reset each launch (accepted; Phase-1 mock store) |

## 9. Milestone status

| Milestone | Scope | Status | Notes |
|---|---|---|---|
| M1 | Foundation: tokens/components/nav skeleton | ✅ | |
| M2 | Onboarding & auth S01–S17 | ✅ | |
| M3 | Main shell & chats S18–S22 | ✅ | |
| Pre-M4 audit | Consolidation + docs + readiness | ✅ | commits `895241f`, `e8abc9c` |
| **M4a** | **Conversation core: domain model v2, message repo + mock behaviors + seed history, S23 chat screen (header/list/bubbles/composer basic)** | ✅ | this update |
| M4b–M4e | Composer full states, reactions/replies, media bubbles, groups info, personalization | ⏳ | plan in `docs/M4_READINESS.md` |
| M5–M7 | Calls, People, Settings, Misc screens | ⏳ | |

**Known limitations (accepted):** M4a renders media/voice/location/contact/poll/sticker content
as labelled placeholder bubbles (M4b/M4c replace); long-press actions/reactions/reply/multi-
select/pinned banner are M4b; group role management UI is M4d; in-chat search/forward/etc are
later; mock state resets between launches; time labels & preview strings are English-only;
Calls/People/Settings tabs remain placeholders; composer Enter = newline (Enter-sends is an M6
setting).

## 10. How to add a new screen (checklist)

1. Put it in `ui/screens/<feature>/`; one file per screen unless it grows past ~400 lines — split
   into private composable sections or subcomponents (screen file guideline).
2. Create `UiState` data class + `@HiltViewModel` exposing `StateFlow<UiState>`; collect with
   `collectAsStateWithLifecycle()`. No business logic in composables.
3. Add repository interface method(s) in `domain/repository` first; implement in the matching
   mock/local impl; bind via `di/RepositoryModule` if new.
4. Add route consts in `ui/navigation/Routes.kt`; register `composable(...)` in `PulseApp.kt`;
   wire callbacks/back handling from the host screen. ViewModels that need route args take
   `SavedStateHandle`.
5. Use ONLY tokens: `PulseTheme.colors`, `MaterialTheme.typography`, `PulseSpacing/IconSizes/
   Sizes`, `PulseShapes`/`PulseBubbleShape`, `PulseFontWeights`, `AppIcons`. No gradients, no
   emoji icons, no `Color(0x…)` in screens.
6. Centralize every user-visible string in `res/values/strings.xml` (no literals; plurals with
   %d where quantity varies).
7. Reuse components from §3 before writing new ones; if new, add light+dark `@Preview`.
8. Icons on tappables get `contentDescription`; decorative icons get `null`.
9. Add `@Preview` (light + dark) for the key states of reusable parts.
10. Keep `data/mock` and `data/local` as the only data sources; never call Supabase or network.

---

_Update this file whenever a milestone ships._
