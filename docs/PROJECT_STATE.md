# Pulse — Project State

_Companion to `docs/PRD.md`. Kept current at every milestone hand-off; update this file when you change architecture, tokens, routes, or persistence._

Last updated: 2026-09-04 (M4c complete). Head: `90e4a8a` (M4b head + M4c commits).

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
│   ├── util/                # MessagePresentation: TimeFormat, MessageLabels, ConversationFormat, SampleMedia (asset keys), CacheFiles (raw->FileProvider export)
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
├── media/                   # VoicePlaybackController.kt (M4c): Hilt-singleton MediaPlayer
│                           # over raw voice assets + @EntryPoint accessor for Compose
├── di/                      # RepositoryModule (@Binds), Qualifiers (@ApplicationScope), CoroutineModule
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
| `ChatHeader` (ChatHeader.kt) | S23 conversation top bar: back + unread overlay badge, 40dp avatar, name/status line, voice/video/more, dropdown menu (Block/Unblock label swaps on the `blocked` flag) | title, statusText, status(Online/Typing/Group/Neutral), avatarSeed, otherUnread, blocked, onMenuAction |
| `AppButton`, `AppIconButton` (AppButton.kt) | 52dp CTA (Primary/Secondary/Tertiary/Destructive, loading, disabled); 48dp icon button | text/icon, variant, loading, enabled, fillMaxWidth |
| `AppTextField` (AppTextField.kt) | Themed input: label, placeholder, error, leading/trailing icons, password toggle | value, onValueChange, label, isError, isPassword |
| `OtpInput` (OtpInput.kt) | 6-box code entry, auto-advance | onComplete, length, isError, resetSignal |
| `AuthStepScaffold` (AuthStepScaffold.kt) | Auth screens: back + step progress + title + content + pinned CTA | steps, stepIndex, title, cta |
| `Avatar` (+ group composite) (Avatar.kt) | Initials/photo circle 24–120dp, online dot, group stack | name, avatarTone, size, isOnline, isGroup |
| `ChatListItem` + `SelectionCheck`, `DeliveryTicks` (ChatListItem.kt) | S19/S21 row incl. all badges/ticks; ticks now cover Sending(clock)/Failed(alert) | summary, onClick, onLongClick, selectionMode, selected |
| `SwipeableRow` (SwipeableRow.kt) | Swipe actions container (snap-back) | startAction, endAction, enabled, content |
| `MessageBubble` (MessageBubble.kt) | S23 bubble (M4a core + M4b interactions + M4c media/voice): text + tappable links, reply-quote bar (tap scrolls to target), forwarded label, edited/star/status meta, deleted placeholder, failed+retry, run geometry, group names + run avatar, flash highlight after pin/quote jumps, reaction pill row; routes every rich payload to `RichUnsurfacedBubble` (image/video/sticker/location/poll) or the bubble body (text/voice/file/contact); M4c mirror of `VoicePlaybackController` (real duration/speed, seek callbacks) with defaults = simulated | message, run flags, senderName/Color, avatarSeed, onRetry, quote+onQuoteTap, flashSignal, onLongPress/onTap, onToggleReaction/onReactionLongPress, voicePlaying/onVoiceToggle (+ voiceControllerDriven/voicePlaySession/voiceDurationMs/voiceSpeedIndex/onVoiceSeek/onVoiceSpeedCycle), onImageTap/onVideoTap/onFileTap, onPollVote/onPollRetract | message, run flags, senderName/Color, avatarSeed, onRetry, quote(BubbleQuoteData?)+onQuoteTap, flashSignal, onLongPress/onTap, onToggleReaction/onReactionLongPress, voicePlaying/onVoiceToggle |
| `VoiceMessageBubble` (VoiceMessageBubble.kt) | Voice-note bubble content: play/pause, progress-tinted waveform bars, duration countdown + 1×/1.5×/2× speed chip; simulated timer by default, or mirrors the real controller (waveform tap = seek, chip = PlaybackParams rate) when controllerDriven | `VoiceNoteContent`(isOutgoing, duration, samples, playing, onToggle, controllerDriven, playSession, voiceDurationMs, voiceSpeedIndex, onSeekFraction, onSpeedCycle); `VoiceMessageBubble`(message…); `VoiceWaveform`(…, onSeekFraction) | `VoiceNoteContent`(isOutgoing, duration, samples, playing, onToggle); `VoiceMessageBubble`(message…) |
| `MessageActions` (MessageActions.kt) | Long-press surfaces: `LongPressScrim`, `QuickReactionBar` (6 quick emoji + "+"), `MessageActionSheet` (icon rows), `EmojiSheetContent` (24-emoji grid) | onReact/onMore; items: List<MessageActionItem(icon,labelRes,destructive)>; onEmoji |
| `MessageReactions` (MessageReactions.kt) | Reaction pills (`ReactionPillRow`, own-reaction accent border/bg) + `ReactorsSheetContent` (avatar+name list); `ReactionEmoji` sets; `reactionPills(message)` builder | pills, isOutgoing, onToggle, onLongPress; emoji, reactors: List<ReactorUi> |
| `ConversationBars` (ConversationBars.kt) | `PinnedBanner` (segment indicator, close-to-unpin, tap cycles/jumps) + `MultiSelectTopBar` (N selected + copy/star/forward/delete) | items+PinnedBannerData, displayIndex, onTap/onClose; count, canCopy, batch callbacks |
| `DateSeparator`, `UnreadDivider`, `SystemMessageRow`, `TypingIndicator` (ChatExtras.kt) | Conversation list chrome (day pill / "N unread" pill / centered system row / 3-dot pulse bubble) | label; senderName for typing |
| `ForwardSheet` (ForwardSheet.kt) | Forward chooser: forwarded-message preview bar (+N), comment field, search + Recent avatar row, selected-target chips, ChatListItem list, Send (N) pill | chats: List<ChatSummary>, messages, onSend(targetIds, comment) |
| `ConversationMedia` (ConversationMedia.kt) | M4c attach/pick/camera cluster: `AttachmentTile` enum (Camera/Photos/Document/Location/Poll/Contact/Audio), `AttachmentTray` (+ in-session recent strip), `MediaSendSheet` (caption editor over picked/drafted media), `CameraSimOverlay` (shutter + hold-to-record over bundled stills/samples), file/media bodies, `RecentMediaItem`/`ViewerMediaItem` builders | tile sets, recent, onTile, onSend… |
| `MediaViewer` (MediaViewer.kt) | M4c fullscreen viewer: horizontal pager over the chat's media, tap chrome (close/sender/date + share/forward/delete/info), pinch-zoom + double-tap, sample-video playback, caption overlay | items: List<ViewerMediaItem>, initialIndex, onClose/onForward/onDelete/onInfo |
| `PollComposer` (PollComposer.kt) | M4c create-poll sheet: question, options list w/ add/remove, multi-select/anonymous/quiz toggles (correct-answer picker) | onSend(question, options, multipleAnswers, isAnonymous, isQuiz, correctOptionIndex) |
| `LocationPicker` (LocationPicker.kt) | M4c location sheet: mock current position + searchable places, live-location duration row, static/live split | onSend(lat, lng, address, isLive, liveDurationSeconds) |
| `ContactShareSheet` (ContactShareSheet.kt) | M4c contact share: directory search + selected chips, contact card preview, send | directory users, onSend(User) |
| `MessageComposer` (MessageComposer.kt) | M4b full composer: `ComposerUiState` Idle/Typing/Reply/Edit/Recording/LockedRecording/Blocked/ReadOnly; reply & edit bars (3dp accent, close), growing 6-line field, hold-to-record with slide-to-cancel + lock (haptics, too-short guard), locked row (pause/resume, trash, send), group @mention popup + accent chip coloring, blocked (Unblock) / read-only status rows | state, value, onValueChange, mentionMembers, onSend/onAttachment/onCamera/onMicPress, onRecordCancel/Lock/Finish(ms,samples)/TooShort, onCloseBar, onMentionSelected, onUnblock |
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
`ComponentShowcases.kt` (audit gap-fill), **`ConversationPreviews.kt` (M4a + M4b):**
ChatHeader online/typing/group, MessageBubble outgoing run / incoming run + system row +
placeholders + reactions/star/edited/failed states, DateSeparator/UnreadDivider/TypingIndicator,
Composer empty/typing/multiline/**reply & edit bars/recording & locked/blocked & read-only**,
QuickReactionBar, MessageActionSheet, ReactionPillRow + ReactorsSheetContent, EmojiSheetContent,
PinnedBanner, MultiSelectTopBar, ForwardSheet, full `ConversationContent` (seeded history,
selection mode, read-only group). Not previewed: `CountryPickerSheet`,
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
| **`chat/{chatId}`** | **S23 conversation — M4a core + M4b messaging interactions** | ✅ REAL — stub replaced |
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

M4b shipped **on the unchanged M4a model** (messaging interactions + UI only — no migration).
Still deferred to M4c–M4e: attachment sheet/media picker/camera, real media bubbles
(Image/Video/File/Location/Contact/Poll/Sticker keep labelled placeholders), group admin UI
(S28–S30), message info / shared media / in-chat search / wallpaper / disappearing messages /
polls (S31–S40).

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
(archive/pin/mute/markRead/markAllRead/deleteChats/refresh), **M4b message actions**
(`editMessage`, `deleteMessage(messageId, forEveryone)`, `toggleReaction`, `setStarred`,
`pinMessage`/`unpinMessage` (cap 5), `forwardMessages(ids, targets, comment?)`, `sendVoice`,
`setBlocked`, `observeMentionCandidates(chatId)`). All bound via `@Binds` in
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
- M4b mock semantics: edit replaces text + sets `isEdited`; delete-for-everyone marks `isDeleted`
  (reactions/star cleared, pin removed) while delete-for-me drops the row from this user's
  history; reaction toggle merges "me" into `MessageReaction.userIds` (empty pill removed);
  forwards copy content with `forwardedFromUserId` kept for peer messages (own = no label) and
  append a non-blank comment as a follow-up text; voice sends ride the same Sending→…→Read
  pipeline (~5% fail, retryable); blocked chats skip the auto-reply/pipeline; mention
  candidates = group members minus "me".
- Session: persisted via DataStore `pulse_session` (splash routes LoggedIn → Main after restart).

## 7. Seed data (`data/mock/SeedData.kt`, M4a)

- **People:** 17 contacts (`u-aria`…`u-tara` + **`u-pulse` "Pulse Assistant"**, verified, always
  online) + `me` (Aarav Kapoor). Presence: `u-aria` and `u-pulse` online; everyone else carries a
  `lastSeenAtMillis`.
- **Chats:** 16 (12 direct + 4 groups) with roles (`members`), descriptions, createdAt. Flags:
  archived `c-zara`/`c-tara`, muted `c-kabir`/`c-lea`/`c-roadtrip`, list-pinned `c-noah`/
  `c-iva`, **in-chat pins** (`pinnedMessageIds`) on `c-aria` ×2 + `c-design`/`c-roadtrip`/
  `c-fam`/`c-run` ×1, **blocked demo toggled in-app** (header menu / composer). Unread: aria 3 /
  design 2 / roadtrip 5 / sam 1 / run 2 / kabir 4; draft on `c-noah`; typing sim on `c-aria`.
- **Messages: ~441 generated per chat (20–45 each)**; five chats (`c-noah`, `c-mira`, `c-fam`,
  `c-rohan`, `c-lea`) reach back **7–12 days** (multi-day pauses) so weekday + full-date
  separators render while recent chats keep Today/Yesterday. Deterministic per chat (fixed
  seed): grouped sender runs (2 min window), system rows, links, **M4b interaction seeds** —
  reply-to-text and reply-to-non-text chains, starred messages, voice notes with waveform
  samples + reactions, multi-user reaction sets (`u-aria`/`u-sam`/…) — plus edited/deleted
  examples and **M4c rich slots**: real content-type payloads (images incl. a 4-photo grid,
  sample videos, voice notes at the 7/15/28 s clip lengths, files incl. the openable
  `sample_route.gpx`, static + live locations, contact cards from the directory, polls with
  voter maps, stickers, system rows) instead of placeholders. Unread tails are peer-sent by
  construction; archived chats end days ago.
- **Pins (M4b):** every group chat carries ≥1 pinned message; `c-aria` carries two so the
  pinned banner cycles. **Read-only demo:** Morning Runners (`c-run`) sets
  `permissions.sendMessages = false` with "me" as Member → the composer renders its read-only
  state there.
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
| **M4a** | **Conversation core: domain model v2, message repo + mock behaviors + seed history, S23 chat screen (header/list/bubbles/composer basic)** | ✅ | |
| **M4b** | **Conversation messaging: full composer states (reply/edit/voice/blocked/read-only, mentions), long-press action sheet + quick reactions + reactors sheet, reaction rows, multi-select batch actions, reply send + quote bars, forward sheet with comment, delete-for-me/everyone, voice notes (send + simulated playback), pinned banner with cycling/jump, flash/jump targets** | ✅ | |
| **M4c** | **Media & pickers per plan: attachment tray (S24), photo/video picker + caption editor over bundled samples (S25), camera simulation (S26), real rich media bubbles + viewer (S33), create-poll (S39), location picker + live durations (S40), contact share sheet, SAF documents & audio sent as files + tap-to-open, real voice playback (`VoicePlaybackController`), seed sweep** | ✅ | this update |
| M4d | Groups & info: contact/group/message info, create/edit group, permissions UI, in-chat search | ⏳ | plan in `docs/M4_READINESS.md` |
| M4e | Chat personalization: starred/pinned lists, wallpaper & accent override, disappearing messages | ⏳ | |
| M5–M7 | Calls, People, Settings, Misc screens | ⏳ | |

**Known limitations (accepted):** recording remains simulated (hold-to-record produces a voice
message with a duration/waveform; the bundled clip closest to that duration plays back, and no
microphone permission is used — real mic recording stays Phase 2); media picked from the system
photo/video picker is sent as the real content uri (pickable once by design of the Android
picker contract) while camera & gallery tiles in the attach tray surface bundled sample stills
and videos so demos work without a camera roll; the media viewer's share opens the system share
sheet and Info is a coming-soon toast (screen is M4d); message info, group admin UI, in-chat
search, wallpaper/disappearing are M4d–M4e; mock state resets between launches; time labels &
preview strings are English-only; Calls/People/Settings tabs remain placeholders; composer
Enter = newline (Enter-sends is an M6 setting).

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
