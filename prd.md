# Product Requirements Document (PRD)

## Project: **Pulse** — A Premium Android Messaging App
*(Rename as you like — placeholder name used throughout)*

**Version:** 1.0 (UI-First Phase)
**Platform:** Android (Kotlin, Jetpack Compose, Material 3)
**Phase Scope:** Complete UI with offline mock data. Supabase backend integration in Phase 2.

---

## 1. Overview

### 1.1 Purpose
Build a modern, premium messaging app for Android with a complete, production-quality UI. Phase 1 delivers every screen, component, and interaction using local mock data so the design can be evaluated and refined before backend work begins. Phase 2 replaces the mock data layer with Supabase (Auth, Postgres, Realtime, Storage) without touching the UI.

### 1.2 Goals
- Deliver a **best-in-class UI** comparable to Telegram, Signal, WhatsApp, and iMessage in polish — but with its own distinct, clean identity.
- Cover **every feature** expected of a modern chat app at the UI level.
- Architect the app so **backend can be plugged in later** with zero UI rewrite.

### 1.3 Non-Goals (Phase 1)
- Real networking, authentication, or message delivery.
- End-to-end encryption implementation (UI indicators only).
- Push notifications (UI settings only).
- Voice/video call functionality (UI screens only, simulated).

### 1.4 Success Criteria
- Every screen listed in Section 6 is implemented and navigable.
- Both light and dark themes are polished and intentional.
- App is fully usable with seeded mock data (send messages, create groups, edit profile, etc. — all persisted locally in-session or via Room).
- No hardcoded colors/text styles in screens; all via theme tokens.
- Zero gradients, zero neon colors, zero emojis used as UI icons (emojis are allowed as *user content* in messages and reactions).

---

## 2. Target Users

| Persona | Needs |
|---|---|
| Everyday user | Fast 1:1 and group chats, media sharing, clean interface |
| Privacy-conscious user | Clear privacy controls, disappearing messages, blocking, lock screen |
| Power user | Search, pinned chats, archived chats, folders, message forwarding, multi-select |

---

## 3. Design Principles

1. **Calm & confident** — generous whitespace, restrained color, precise alignment.
2. **Content first** — messages and people are the hero; chrome stays quiet.
3. **Consistent rhythm** — 4/8dp grid, defined spacing and radius tokens.
4. **Intentional depth** — subtle borders and soft shadows, no heavy elevation.
5. **Strict rules:**
   - No gradients.
   - No neon or oversaturated colors.
   - No emojis as UI elements; use vector icons (single consistent icon set).
   - No default Material purple.
   - Both light and dark themes are first-class.

---

## 4. Design System

### 4.1 Color Tokens

**Light Theme**
| Token | Value | Usage |
|---|---|---|
| background | #F7F7F8 | Screen background |
| surface | #FFFFFF | Cards, sheets, bars |
| surfaceVariant | #F0F0F3 | Input fields, incoming bubbles |
| border | #E6E6EA | Hairline dividers, card borders |
| textPrimary | #111114 | Titles, message text |
| textSecondary | #6B6B75 | Subtitles, timestamps |
| textTertiary | #9A9AA3 | Hints, placeholders |
| accent | #3B5BDB | Primary actions, outgoing bubbles, active states |
| accentContainer | accent @ 10% | Selected items, chips, icon containers |
| onAccent | #FFFFFF | Text/icons on accent |

**Dark Theme**
| Token | Value |
|---|---|
| background | #0F0F12 |
| surface | #1A1A1F |
| surfaceVariant | #24242B |
| border | #2A2A32 |
| textPrimary | #F2F2F5 |
| textSecondary | #A0A0AA |
| textTertiary | #6E6E78 |
| accent | #5C7CFA |
| accentContainer | accent @ 14% |
| onAccent | #FFFFFF |

**Semantic (both themes, muted)**
- success: #2E8B57 / #3FA372 (online dot, delivered ticks)
- warning: #C27C0E / #D4922E
- error: #C43D3D / #E05A5A (failed message, destructive actions)
- info: #2F6FB0 / #4A8AD0

**Avatar palette (muted, for initials avatars):** 8 desaturated tones — slate, sage, clay, sand, dusk, moss, plum, steel — each with matching foreground color.

### 4.2 Typography
Typeface: **Inter** (bundled). Tabular figures for timestamps/counters.

| Style | Size / Weight / Line height |
|---|---|
| displayLarge | 32sp / SemiBold / 40sp, letterSpacing -0.5 |
| headlineMedium | 24sp / SemiBold / 32sp |
| titleLarge | 20sp / SemiBold / 28sp |
| titleMedium | 16sp / Medium / 24sp |
| bodyLarge | 16sp / Regular / 24sp |
| bodyMedium | 14sp / Regular / 20sp |
| labelLarge | 14sp / Medium / 20sp |
| labelMedium | 12sp / Medium / 16sp, letterSpacing 0.2 |
| labelSmall | 11sp / Medium / 16sp |

### 4.3 Spacing & Shape
- Spacing tokens: 4, 8, 12, 16, 20, 24, 32, 40, 48dp
- Screen horizontal padding: 20dp
- Radius: xs 6dp (tags), sm 8dp (inputs), md 12dp (cards, buttons), lg 16dp (sheets), xl 20dp (hero cards), full (avatars, pills)
- Message bubble radius: 18dp with 4dp on the "tail" corner
- Min touch target: 48dp
- Button height: 52dp

### 4.4 Iconography
- Single set: **Material Symbols Rounded** (weight 400) or **Lucide**.
- Sizes: 20dp inline, 24dp default, 28dp feature.
- Default tint textSecondary; accent when active.

### 4.5 Motion
- Screen transitions: fade + 24dp slide, 250ms, FastOutSlowIn.
- Press feedback: ripple + scale 0.98.
- Message send: bubble slides up 8dp + fade in.
- Typing indicator: three dots, subtle opacity pulse.
- No bounce, no overshoot.

---

## 5. Architecture (Backend-Ready)

```
app/
├── ui/
│   ├── theme/          Color.kt, Type.kt, Shape.kt, Spacing.kt, Theme.kt
│   ├── components/     Shared UI kit
│   ├── navigation/     NavGraph, routes, transitions
│   └── screens/        One package per feature
├── domain/
│   ├── model/          User, Chat, Message, Attachment, Call, etc.
│   └── repository/     Interfaces only (AuthRepository, ChatRepository...)
├── data/
│   ├── mock/           MockAuthRepository, MockChatRepository, SeedData
│   ├── local/          Room (optional, for persistence across launches)
│   └── remote/         (Phase 2) SupabaseAuthRepository, etc.
└── di/                 Hilt modules — swap Mock → Supabase here
```

- **Pattern:** MVVM + Unidirectional Data Flow. Each screen has a `UiState` data class and a ViewModel.
- **Repositories are interfaces.** Phase 1 binds mock implementations; Phase 2 binds Supabase implementations. UI code never changes.
- **Mock layer behavior:** simulated delays (300–800ms), simulated "other user" replies, typing indicators, delivery status progression (sending → sent → delivered → read), occasional simulated failure for error-state testing.
- **Seed data:** ~25 contacts, ~15 chats (mix of 1:1, groups, archived, muted, pinned), ~400 messages with text, images, voice, files, replies, reactions, and system messages.

**Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt, Coroutines/Flow, Coil, Room (optional), DataStore (settings), Kotlinx Serialization.

---

## 6. Screen Inventory & Requirements

Navigation graph is split into: **Onboarding/Auth**, **Main (tabs)**, **Chat**, **Profile & Settings**, **Calls**, **Media**, **Misc**.

---

### 6.1 Launch & Onboarding

#### S01 — Splash Screen
- Full-screen background (theme color), centered app logo mark (vector), app name in titleLarge below.
- Thin indeterminate progress line at bottom or none; duration ~1.2s.
- Uses Android 12 SplashScreen API with a seamless handoff into Compose.
- Routes to: Welcome (not logged in) / Main (logged in) / App Lock (if enabled).

#### S02 — Welcome Screen
- Top 55%: minimal hero — abstract flat illustration made of shapes/icons (no gradients) OR a large logo mark with tagline.
- Headline: "Messaging, refined." (displayLarge)
- Subtext: one line, textSecondary.
- Actions (bottom, stacked, 12dp gap):
  1. **Continue with Google** — secondary button (surface + border), Google "G" vector icon leading.
  2. **Log in** — primary accent button.
  3. **Create account** — text button.
- Footer: "By continuing you agree to Terms & Privacy" in labelSmall with tappable links.
- Optional 3-page intro carousel before this screen (feature highlights with icons, page dots, Skip).

---

### 6.2 Authentication — Log In Flow (one question per screen)

All auth screens share **AuthStepScaffold**:
- Top bar: back arrow (left), thin step progress indicator (segmented bar, accent fill).
- Title (headlineMedium), supporting text (bodyMedium, textSecondary).
- One primary input focused automatically, keyboard open.
- Bottom-pinned primary **Continue** button above the IME (imePadding).
- Inline validation with error text below field; button disabled until valid.

#### S03 — Login: Email
- Field: Email (leading mail icon). Validates format.
- Link: "Use phone number instead" → S03b.
- Continue → S04.

#### S03b — Login: Phone (alternate)
- Country code picker (bottom sheet with search, flag rendered as text/ISO code badge, not emoji) + phone field.
- Continue → S06 (OTP).

#### S04 — Login: Password
- Shows entered email as a small chip with edit icon.
- Field: Password with visibility toggle icon.
- Link: "Forgot password?" → S07.
- Continue → loading state → Main. Mock: wrong password if "wrong" is typed (for error demo).

#### S05 — Continue with Google (Account Picker Sheet)
- Bottom sheet mimicking account chooser: list of 2 mock Google accounts (avatar, name, email) + "Use another account".
- Selecting → brief loading → if new user go to S12 (Profile Setup), else Main.

#### S06 — OTP Verification
- Six individual digit boxes (sm radius), auto-advance, paste support.
- Resend timer ("Resend code in 0:30"), then "Resend code" link.
- "Change number" link.
- Auto-submit on 6th digit.

#### S07 — Forgot Password: Email
- Email field → Continue → S08.

#### S08 — Check Your Inbox
- Large icon in accentContainer circle (mail icon), title, explanation with email shown.
- Buttons: "Open email app", "Resend", "Back to login".

#### S09 — Reset Password
- New password + Confirm password with strength meter (4 segments, muted colors: error/warning/success).
- Requirements checklist with check icons.
- Continue → S10.

#### S10 — Success Screen (reusable)
- Centered check icon in success-tinted circle, title, subtitle, single primary button.
- Reused for: password reset, account created, email verified.

---

### 6.3 Authentication — Create Account Flow

#### S11a — Sign Up: Email
- Email field; link "Already have an account? Log in".

#### S11b — Sign Up: Verify Email (OTP)
- Same component as S06.

#### S11c — Sign Up: Create Password
- Same as S09 layout.

#### S12 — Profile Setup: Name
- First name, last name fields.

#### S13 — Profile Setup: Username
- @username field with live availability check (mock): spinner → check icon / error.
- Suggestions row (chips).

#### S14 — Profile Setup: Photo
- Large circular avatar placeholder with camera icon overlay.
- Options sheet: Take photo, Choose from gallery, Choose an avatar (grid of initial-based colored avatars), Remove.
- "Skip for now" text button.

#### S15 — Profile Setup: Bio (optional)
- Multiline field, character counter (0/140).

#### S16 — Permissions Primer
- Cards for Contacts, Notifications, Microphone, Camera — each with icon, title, one-line reason, and "Allow" button (calls real system permission dialogs).
- "Continue" at bottom regardless.

#### S17 — Find Friends
- List of mock contacts "already on Pulse" with Add buttons; "Invite friends" section.
- Continue → S10 Success → Main.

---

### 6.4 Main Shell

#### S18 — Main Scaffold
- **Bottom navigation** (4 tabs): Chats, Calls, People, Settings. Icons + labels; accent on selected; hairline top border; unread badge on Chats (small pill with count, accent background).
- Edge-to-edge; content draws behind status bar with proper insets.
- FAB (accent, md radius, "compose" icon) on Chats and People tabs.

#### S19 — Chats Tab
- Top app bar: title "Chats" (headlineMedium), trailing icons: search, more (menu: New group, New broadcast, Linked devices, Starred messages, Settings).
- Collapsible **search bar** below title (surfaceVariant, sm radius) — on tap expands to S20.
- Optional **Folders/filters row** (chips): All, Unread, Groups, Personal, + Edit folders.
- **Archived** row at top (when archived chats exist): archive icon, "Archived", count.
- **Chat list items** (LazyColumn, keyed):
  - Avatar 52dp (photo or initials; small online dot; group avatar composite).
  - Title (titleMedium) + verified/pin/mute icons; timestamp (labelMedium, tabular) right-aligned.
  - Second line: sender prefix for groups, last message preview with type icon (photo/mic/file), typing indicator ("typing…" in accent), draft prefix in error color.
  - Right column: unread pill (accent; grey if muted), or delivery ticks (sent/delivered/read) for own last message, or pinned icon.
  - Hairline divider inset to text start.
- **Swipe actions**: right → Archive; left → Pin/Mute (icon + label revealed on tinted background, muted colors).
- **Long press** → multi-select mode: top bar transforms (count, close, actions: pin, mute, archive, delete, mark read).
- **Empty state**: icon in tinted circle, "No conversations yet", CTA "Start a chat".
- Pull-to-refresh (mock).

#### S20 — Global Search
- Full-screen: search field with back, clear.
- Recent searches (chips with remove).
- Result sections: Chats, Contacts, Messages (with highlighted matched text), Media, Links, Files.
- Filter chips: All, Chats, People, Messages, Media.

#### S21 — Archived Chats
- Same list item component; top bar "Archived"; swipe to unarchive.

#### S22 — Chat Folders Editor
- List of folders with drag handles; create/edit folder screen (name, include chats, include types).

---

### 6.5 Conversation

#### S23 — Chat Screen (1:1 and Group)
**Top bar**
- Back with unread count badge, avatar (40dp), name (titleMedium), status line (online / last seen / "typing…" / member count for groups) in labelMedium.
- Trailing: voice call, video call, more (menu: View profile, Search, Mute, Wallpaper, Clear chat, Block, Report, Export).
- Tap on header → S27/S28 profile.

**Message list** (reverse LazyColumn)
- Date separators (pill, surfaceVariant, labelMedium).
- Unread divider ("3 unread messages").
- System messages (centered, textTertiary): "You created the group", "Alex joined".
- **Outgoing bubble**: accent background, onAccent text, right-aligned, timestamp + ticks inside bottom-right (small, semi-transparent).
- **Incoming bubble**: surfaceVariant, textPrimary, left-aligned; in groups: sender name in a per-user muted color + small avatar on last bubble of a run.
- Bubble grouping: consecutive messages from same sender tighten spacing (2dp) and adjust corner radii (first/middle/last).
- **Message types** (each with dedicated composable):
  - Text (with link detection, tappable, link preview card).
  - Image (single with rounded corners; 2–4 images in grid; "+N" overlay).
  - Video (thumbnail, play icon, duration).
  - Voice note (play/pause button, waveform bars, duration, playback speed chip 1x/1.5x/2x).
  - File/document (file type icon in tinted box, name, size).
  - Location (static map placeholder card, address).
  - Contact card (avatar, name, "Message"/"Add" actions).
  - Poll (question, options with progress bars, vote counts).
  - Sticker/GIF placeholder.
  - Reply quote (thin accent bar + sender + excerpt above content; tap scrolls to original with highlight flash).
  - Forwarded label ("Forwarded" with icon).
  - Edited label.
  - Deleted message ("This message was deleted" italic with icon).
  - Reactions row under bubble (pill with emoji content + count; own reaction highlighted with accent border).
- **Failed message**: error icon + "Tap to retry".
- **Scroll-to-bottom FAB** with unread count, appears when scrolled up.
- **Message long-press**: dimmed background, bubble lifts, quick reaction bar above (6 emoji as content + "+" icon), action sheet below: Reply, Forward, Copy, Pin, Star, Edit (own), Info, Delete, Select.
- **Multi-select** mode: checkboxes, top bar actions (forward, delete, copy, star).
- **Pinned message banner** below top bar (pin icon, excerpt, close, tap to jump; cycles through multiple).

**Composer (bottom)**
- Attachment button (plus icon) → S24 attachment sheet.
- Text field (surfaceVariant, pill radius, grows to 6 lines) with placeholder "Message".
- Inline emoji/sticker toggle icon inside field (opens system keyboard emoji or custom sticker panel).
- Trailing: mic button when empty; send button (accent circle) when text present — animated swap.
- Camera icon inside field when empty.
- **Reply/Edit bar** above composer (accent bar, label, excerpt, close icon).
- **Voice recording state**: red dot (error color, subtle pulse), timer, "Slide to cancel" with chevron, lock icon to switch to hands-free; locked state shows waveform + delete/send.
- **Mention suggestions** popup in groups when typing "@".
- **Typing indicator** shown from other side (bubble with animated dots).
- **Blocked / read-only states**: composer replaced by "You blocked this contact. Unblock" or "Only admins can send messages".

#### S24 — Attachment Sheet
- Bottom sheet, top: horizontal recent gallery strip (with camera tile first, multi-select counters).
- Grid of options (icon in tinted square + label): Gallery, Camera, Document, Location, Contact, Poll, Audio.

#### S25 — Media Picker / Editor
- Full gallery grid with albums dropdown, multi-select badges.
- Preview/edit screen: swipe between selected, crop/rotate, draw, text, caption field, "Send as file" toggle, send button with count.

#### S26 — Camera Screen
- Viewfinder, shutter (hold for video), flip, flash toggle, gallery thumbnail.
- Post-capture preview with caption and send.

#### S27 — Contact Info (1:1)
- Large avatar hero (tap → full screen), name, @username, status.
- Action row (icon buttons in tinted circles): Message, Call, Video, Mute, Search.
- Sections (cards): Bio, Phone/Email, Username; Media/Links/Files/Voice (horizontal previews + "See all" → S32); Common groups; Settings (Notifications, Disappearing messages, Chat wallpaper, Encryption info); Destructive: Block, Report, Delete chat.

#### S28 — Group Info
- Group avatar, name, description, member count, created by/date.
- Actions: Add members, Mute, Search, Leave.
- Invite link row (copy/share/revoke).
- Members list with roles (Admin badge chip), long-press → Make admin, Remove.
- Media/links/files; settings (permissions, disappearing messages, wallpaper).
- Admin-only: Edit group (S29), Group permissions (S30).

#### S29 — Create / Edit Group
- Step 1: select members (search + contact list with checkboxes; selected shown as removable avatar chips at top).
- Step 2: group photo, name, description, "Disappearing messages" default.

#### S30 — Group Permissions
- Toggles: Send messages, Send media, Add members, Pin messages, Edit info, Approve new members.

#### S31 — Message Info
- Message preview, Delivered to / Read by lists with timestamps and avatars.

#### S32 — Shared Media
- Tabs: Media, Files, Links, Voice, Music. Grid/list, month section headers, multi-select.

#### S33 — Media Viewer
- Full-screen black (this is the only intentionally black surface), pinch zoom, swipe between, top bar (sender, date, back), bottom bar (share, save, forward, delete, info), caption overlay.

#### S34 — Forward Sheet
- Search + recent chats grid/list with multi-select; message preview bar; optional comment field; send.

#### S35 — Starred Messages / Pinned Messages
- List of bubbles with chat context header; tap to jump.

#### S36 — In-Chat Search
- Search field in top bar; result count; up/down arrows; calendar icon → jump to date.

#### S37 — Chat Wallpaper & Theme
- Grid of solid muted wallpapers + subtle pattern options + custom image; per-chat accent color picker (muted swatches); live preview of bubbles.

#### S38 — Disappearing Messages
- Radio options: Off, 24h, 7d, 90d, Custom; explanation text.

#### S39 — Create Poll
- Question field, up to 10 options (add/remove), toggles: Multiple answers, Anonymous, Quiz mode.

#### S40 — Location Picker
- Map placeholder, current location card, search field, nearby places list, "Share live location" option with duration selector.

---

### 6.6 Calls

#### S41 — Calls Tab
- Top bar "Calls", trailing: search, new call icon.
- Filter chips: All, Missed.
- "Create call link" row.
- Call history list: avatar, name, call-type/direction icon (incoming/outgoing/missed with muted color), time; trailing call/video icon button.
- Swipe to delete; long-press multi-select.

#### S42 — New Call
- Search contacts; list with voice/video icon buttons; "New group call" option.

#### S43 — Outgoing / Incoming Call (Voice)
- Blurred/dimmed avatar background (dark surface, not gradient), large avatar, name, status ("Calling…", "Ringing…", timer).
- Incoming: swipe/tap Answer (success circle) / Decline (error circle), "Message" and "Remind me" shortcuts.
- In-call controls (bottom row in tinted circles): Mute, Speaker, Video, Add person, End (error).
- Encryption badge (lock icon, "End-to-end encrypted").
- Minimize → floating PiP-style bubble in app.

#### S44 — Video Call
- Remote full-screen placeholder, local preview draggable card, controls auto-hide, switch camera, effects toggle.

#### S45 — Group Call
- Grid of participant tiles (2–8) with mute indicators, active speaker outline (accent), participants sheet.

#### S46 — Call Details
- Contact header, list of calls with duration, actions.

---

### 6.7 People / Contacts

#### S47 — People Tab
- Top bar "People", trailing: search, add contact.
- Quick actions: New group, New contact, Invite friends.
- Stories/Status row (optional feature): horizontal avatars with ring (accent ring = unseen, border ring = seen), "My status" first → S48.
- Alphabetical contact list with sticky section headers and fast-scroll index.
- Contact item: avatar, name, bio/status line, online dot.

#### S48 — Status / Stories (optional but included for completeness)
- Viewer: full-screen, segmented progress bars top, tap left/right, hold to pause, reply field bottom, viewers list for own.
- Create status: text (solid color backgrounds, font choice) or media with caption; privacy selector.
- Status privacy screen.

#### S49 — New Contact
- Form: photo, first name, last name, phone (country picker), email; Save.

#### S50 — Invite Friends
- Share link card, list of device contacts not on app with "Invite" buttons.

#### S51 — Blocked Contacts
- List with Unblock action; "Add" button.

#### S52 — Contact Requests (if using request model)
- Incoming/Outgoing tabs with Accept/Decline.

---

### 6.8 Profile & Settings

#### S53 — Settings Tab
- Profile header card: avatar, name, @username, phone, chevron → S54. QR code icon.
- Grouped list (cards with SectionHeader):
  - **Account**: Account, Privacy, Security, Devices
  - **Preferences**: Appearance, Chats, Notifications, Storage & Data, Language
  - **Support**: Help, Report a problem, Terms & Privacy, About
  - **Log out** (error-colored text row)
- Each item: icon in tinted rounded square (consistent per section color), title, optional value on right, chevron.

#### S54 — My Profile / Edit Profile
- Large avatar with edit; fields: First name, Last name, Username, Bio, Phone, Email, Birthday; Save button in top bar (enabled on change).

#### S55 — My QR Code
- Tabs: My code / Scan code. QR card on surface, share button, camera scanner view.

#### S56 — Account Settings
- Change phone, change email, change password, two-step verification, delete account (confirmation flow with reason picker + password).

#### S57 — Privacy
- Rows with current value: Last seen & online, Profile photo, About, Status, Read receipts (switch), Groups (who can add), Calls, Blocked contacts, Disappearing default, Screen lock, Advanced (link previews, IP protection).
- Each opens a radio-option screen (Everyone / My contacts / Nobody / Except…).

#### S58 — Security
- Two-step verification setup wizard (PIN, confirm, recovery email → multiple steps).
- Security notifications toggle, Passkeys, Active sessions.

#### S59 — Devices / Linked Devices
- Current device card; list of sessions with device icon, name, location, last active; "Log out all"; "Link a device" → QR scanner.

#### S60 — Appearance
- Theme: System / Light / Dark (segmented control with previews).
- Accent color: row of muted swatches.
- Chat wallpaper (→ S37 global).
- Message text size slider with live preview bubble.
- Corner style, Bubble style (optional), App icon picker.

#### S61 — Chats Settings
- Enter is send, Media visibility, Font size, Archive behavior, Chat backup (→ S62), Chat history (Export, Archive all, Clear all, Delete all).

#### S62 — Chat Backup
- Last backup card, Back up now button, frequency, account, include videos toggle, encryption toggle.

#### S63 — Notifications
- Sections: Messages, Groups, Calls — each with Tone, Vibrate, Popup, Light. Toggles: Show previews, Reaction notifications, In-app sounds; "Reset settings".

#### S64 — Storage & Data
- Storage usage bar (segmented muted colors: photos, videos, files, other) + "Manage storage" → per-chat sizes list with clear actions.
- Network usage stats.
- Auto-download: Mobile / Wi‑Fi / Roaming (checkbox sheets).
- Media quality selector; Proxy settings.

#### S65 — Language
- Searchable list with radio, "System default" first.

#### S66 — Help Center
- Search, FAQ accordion list (animateContentSize), Contact us, Report a problem form.

#### S67 — About
- Logo, version, build, licenses, credits, changelog.

#### S68 — Log Out Confirmation
- Dialog / sheet with warning icon, text, Cancel + Log out (destructive).

---

### 6.9 Misc / System Screens

#### S69 — App Lock
- PIN pad (custom numeric keypad, large tappable circles) or biometric prompt; error shake (subtle, 4dp); "Forgot PIN".

#### S70 — Notification Deep-Link Landing
- Handles opening chat from notification (mock via debug menu).

#### S71 — Share Target Screen
- When app receives shared content: pick chats (like Forward Sheet) + caption.

#### S72 — Offline / No Connection Banner
- Thin banner under top bar: "Waiting for network…" with icon; mock toggle in debug settings.

#### S73 — Global Empty / Error / Loading States
- Shared components: Skeleton loaders (chat list, message list, contact list), empty state, error state with Retry.

#### S74 — Debug Menu (Phase 1 only, hidden behind long-press on About version)
- Toggles: simulate offline, simulate incoming message, simulate incoming call, reset seed data, force theme, slow network, show layout bounds.

---

## 7. Component Library (must be built first)

| Component | Notes |
|---|---|
| AppTopBar | Variants: large title, small title, search, selection mode, chat header |
| AppButton | Primary, Secondary, Tertiary, Destructive, Icon; loading + disabled states |
| AppTextField | Label, placeholder, helper/error, leading/trailing icons, password toggle |
| OtpInput | 6 boxes, auto-advance, paste |
| AuthStepScaffold | Back, step progress, title, subtitle, content, pinned CTA |
| Avatar | Sizes 24–120dp; photo/initials/group composite; online dot; story ring |
| ChatListItem | With all badges, typing state, swipe actions |
| MessageBubble + subtypes | As listed in S23 |
| ReactionPill | Emoji content + count |
| Composer | All states: idle, typing, reply, edit, recording, locked recording, blocked |
| VoiceWaveform | Static + playing |
| DateSeparator, SystemMessage, UnreadDivider | |
| SectionHeader, SettingsItem, SettingsSwitchItem, SettingsRadioItem | |
| AppCard, AppDivider, AppChip, Badge, Tag | |
| AppBottomSheet, AppDialog, ConfirmDialog | Drag handle, lg radius |
| SegmentedControl, AppSwitch, AppSlider, AppCheckbox, AppRadio | Themed |
| EmptyState, ErrorState, SkeletonLoader | |
| SearchBar | Collapsed + expanded |
| CountryPicker | Sheet with search |
| ColorSwatchRow | For accent/wallpaper |
| CallControlButton | Tinted circle with icon + label |
| AppSnackbar | Themed, with action |

Every component ships with `@Preview` in light and dark.

---

## 8. Navigation Map (high level)

```
Splash
 ├─ AppLock ─┐
 ├─ Welcome  │
 │   ├─ Google Picker ─┬─ Profile Setup (S12–S17) ─ Success ─ Main
 │   │                 └─ Main
 │   ├─ Login Email ─ Login Password ─ Main
 │   │      ├─ Phone ─ OTP ─ Main
 │   │      └─ Forgot ─ Inbox ─ Reset ─ Success ─ Login
 │   └─ SignUp Email ─ OTP ─ Password ─ Profile Setup ─ Success ─ Main
 └─ Main (Bottom tabs)
     ├─ Chats ─ Chat ─ ContactInfo/GroupInfo ─ SharedMedia ─ MediaViewer
     │            ├─ Attachments/Media Picker/Camera/Location/Poll
     │            ├─ Forward, MessageInfo, Search, Wallpaper, Disappearing
     │            └─ Calls (voice/video)
     │        ├─ Global Search, Archived, Folders, New Group
     ├─ Calls ─ New Call, Call Details, Call screens
     ├─ People ─ Contact Info, New Contact, Invite, Status
     └─ Settings ─ all S54–S68
```

---

## 9. Mock Data & Behaviors (Phase 1)

- **Auth:** any valid email + any password ≥ 8 chars succeeds; password "wrong" fails. OTP "123456" succeeds; others fail. Google picker always succeeds.
- **Messaging:** sending a message shows status progression; 40% of chats auto-reply after 1.5–4s with a typing indicator first.
- **Incoming events:** debug menu can trigger incoming message/call to test banners and call UI.
- **Persistence:** Room stores messages/chats so state survives restart; DataStore stores settings and theme.
- **Media:** bundled sample images; voice notes use bundled short audio; files are placeholders.

---

## 10. Non-Functional Requirements

- **Performance:** 60fps scrolling in chat list and message list with 1,000+ items; stable keys; image loading via Coil with memory/disk cache; no jank on keyboard open.
- **Accessibility:** contentDescriptions on all icons; TalkBack order logical; contrast AA; supports font scaling up to 130% without clipping.
- **Responsiveness:** works on 360dp–430dp phones; foldables/tablets get a two-pane Chats + Conversation layout (stretch goal).
- **Theming:** system theme + manual override; instant switch without restart.
- **Edge-to-edge** on Android 10+; predictive back gestures supported.
- **Min SDK:** 26. **Target:** latest stable.

---

## 11. Phase 2 Preview (Supabase — not in scope now)

- Supabase Auth (email/password, OTP, Google OAuth) → `AuthRepository`
- Postgres tables: profiles, chats, chat_members, messages, reactions, calls, contacts, blocks, settings
- Realtime channels for messages, typing, presence
- Storage buckets for avatars and media
- Row Level Security policies
- Only `data/remote` + DI bindings change; UI untouched.

---

## 12. Milestones (UI Phase)

| # | Milestone | Deliverable |
|---|---|---|
| M1 | Foundation | Theme system, component library, navigation skeleton, mock data layer |
| M2 | Onboarding & Auth | S01–S17 complete, all steps and states |
| M3 | Main shell & Chats list | S18–S22 |
| M4 | Conversation | S23–S40 (largest milestone; split into bubbles → composer → sheets) |
| M5 | Calls & People | S41–S52 |
| M6 | Settings | S53–S68 |
| M7 | Polish | Misc screens S69–S74, motion pass, accessibility pass, dark/light QA, performance pass |

---

## 13. Acceptance Checklist

- [ ] Every screen in Section 6 exists and is reachable.
- [ ] All auth flows are multi-step, one question per screen, with validation and error states.
- [ ] Chat screen supports every message type and composer state listed.
- [ ] No gradients, no neon colors, no emojis used as UI icons.
- [ ] No hardcoded colors/typography in screen composables.
- [ ] Light and dark themes reviewed screen-by-screen.
- [ ] All components have light + dark previews.
- [ ] Mock repositories implement the same interfaces Supabase will use.
- [ ] App runs fully offline with seeded data and survives restart.

---
