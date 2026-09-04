# M4 Readiness & Milestone Plan — Pulse

_Prepared 2026-09-04 from the pre-M4 audit; updated after M4a, after M4b, and again after M4c._
_Milestone status: M1 ✅ M2 ✅ M3 ✅ Audit ✅ M4a ✅ M4b ✅ **M4c ✅** — M4d…M4e pending._

---

## 1. M4c delivered (what was built)

Scope per the M4c plan: **S24 attachment sheet, S25 media picker/editor (bundled samples),
S26 camera simulation, S33 media viewer, S39 create poll, S40 location picker**, plus the
extras agreed for this milestone: contact share sheet, SAF documents/audio sent as files with
tap-to-open, real voice playback, and the seed sweep. No model migration was needed —
`MessageContent` v2 already carried every payload. Compile gate green before every commit;
light+dark preview pairs for every new surface; no new runtime permissions beyond what the
system pickers grant by contract (no storage/mic/camera).

- **Attachment tray (S24)**: paperclip opens the in-chat tray above the composer with tiles
  Camera / Photos & videos / Document / Location / Poll / Contact share / Audio; a
  "recently sent" strip surfaces this session's picked media for instant re-send.
- **Photo/video picking & caption editor (S25, D5)**: the Photos tile opens the system photo
  picker (multiple visual media, no storage permission). Every picked/recorded/sample draft
  lands in the caption editor: horizontal thumbnails, count, caption field, Send — images send
  as one multi-image message, videos individually with duration. Because a demo device may have
  no camera roll, the tray's gallery fallback shows bundled sample stills/videos; the picker
  contract keeps sent photos playable for the current run.
- **Camera simulation (S26)**: full-screen camera chrome over bundled stills; shutter takes a
  photo into the caption editor; hold-to-record shoots a sample video (≤10 s) with a timer and
  the same slide/lock affordances as voice recording; flash toggle is visual.
- **Real rich media bubbles**: image grids (1 / 2 / 3–4 layouts with caption/quote/meta chrome),
  video cards with play → viewer, file cards with type icon + size + name, static/live location
  cards, contact cards, poll cards with votes/percentages, stickers — all seeded and all
  interactive (long-press actions, reactions; quotes of non-text render type labels).
- **Media viewer (S33)**: fullscreen black viewer over the chat's chronological media (tap
  media in chat to open at that page); tap toggles chrome (close, sender/date, share → system
  sheet, forward → ForwardSheet, delete → existing delete flow, info → coming-soon toast; the
  info screen is M4d); pinch-zoom/double-tap on images; sample videos play inline with
  play/pause + thin progress; caption overlay.
- **Create poll (S39)**: composer poll sheet — question, editable options (2–10, add/remove),
  toggles for multiple answers / anonymous / quiz with correct-answer picker; poll bubbles
  vote/retract against the mock repository tally.
- **Location picker (S40)**: sheet with mock "current" position + searchable places, map-pin
  visuals, static send or live-location duration (15 min / 1 h / 8 h); location cards show
  static vs live-with-timer chrome (live seeds included).
- **Contact share sheet**: directory search + selected chips + contact-card preview; sends a
  contact card message reusing the existing contacts directory (no new repository surface).
- **SAF documents & audio (send as FILE)**: Document tile opens SAF `OpenDocument` (`*/*`),
  Audio tile `OpenDocument` (`audio/*`); both take persistable read permission, query
  name/size/mime and send as File messages; file bubbles open with `ACTION_VIEW` (bundled
  samples exported to cache via FileProvider; picked files use their persisted uri; failures
  toast, never crash). Zero storage permissions.
- **Real voice playback**: `VoicePlaybackController` (Hilt singleton in `media/`) plays the
  bundled raw clips (short ≈7 s / medium ≈15 s / long ≈28 s, chosen from the message's
  simulated duration so audio length tracks the metadata; the prepared clip duration then
  drives the bubble). One active player, play/pause/resume, waveform-tap seek, 1×/1.5×/2× via
  `PlaybackParams`, audio focus (pause on loss), stop when leaving the chat or backgrounding.
  Recording stays **simulated** (no mic permission): hold-to-record produces a message whose
  duration/waveform metadata is real and whose playback uses the closest bundled clip.
- **Seed sweep**: every seed payload routes through a real rich bubble and resolves to a
  bundled asset; the one broken seed (gpx file with no source) gained a bundled `sample_route.gpx`.

_Clean builds at each commit; docs updated. Forward sheet had already shipped in M4b. M4c
commits `1a30e54`…`90e4a8a` sit on top of the M4b head._

## 2. M4b delivered (what was built)

Built on the **unchanged M4a model** (interactions + UI only, no migration). Clean build:
0 errors / 0 warnings; light+dark preview pairs added for every new surface.

- **Full composer state machine** (`MessageComposer`, `ComposerUiState`): Idle / Typing /
  Reply / Edit / Recording (hold) / LockedRecording (hands-free) / Blocked / ReadOnly.
  Reply & edit bars (3dp accent bar, icon, title, excerpt, close), growing 6-line field,
  hold-to-record with slide-to-cancel + lock gestures (haptics, too-short guard), locked row
  (pause/resume, delete, send), blocked row with Unblock, read-only note row (group
  `permissions.sendMessages = false`), group **@mention popup** with accent chip coloring.
- **Long-press interactions**: overlay freezes the list, dims it (`LongPressScrim`), lifts a
  replica of the pressed bubble at its anchored position with the **quick-reaction bar**
  (6 core emoji + emoji sheet with 24), and shows the **action sheet** — Reply / Forward /
  Copy / Pin–Unpin / Star–Unstar / Edit (own text <15 min) / Info / Delete / Select — with
  per-message gating (deleted/system excluded; media types get type labels).
- **Reactions**: pills under bubbles (own reaction = accent border/container), tap toggles,
  long-press opens the **reactors sheet** (avatar + name per user); seeded multi-user sets.
- **Multi-select**: circular check on the outer edge, accentContainer row tint, top bar with
  count + Copy (text only) / Star (batch flip) / Forward / Delete; row taps toggle.
- **Reply & forward rendering**: quote bar inside bubbles (sender + excerpt or type label for
  non-text), tap scrolls + flashes the target message; outgoing replies send with
  `replyToMessageId`.
- **Forward sheet**: preview bar (+N), optional comment field, search + Recent picks,
  selected-target chips, reuses `ChatListItem`, Send (N) pill; forwards keep
  `forwardedFromUserId` for peer messages (no label on own) and append the comment.
- **Delete**: per-message dialog — Delete for everyone (own) / Delete for me; multi-delete
  dialog; repo marks `isDeleted` (clears reactions/star/pin) vs removes the row.
- **Voice notes**: hold/record → `sendVoice(durationMs, samples)` rides the send pipeline;
  voice bubbles render waveform bars with **simulated playback** (timer + progress tint +
  1×/1.5×/2× speed) until real audio in M4c.
- **Pinned banner** below the header: excerpt + vertical segment indicator for multiple pins,
  tap cycles pins and jumps/flashes to the message, close asks to unpin (confirm dialog).
  Seeded: every group ≥1 pin; `c-aria` has two for cycling.
- **Repository additions** (`ChatRepository` → mock): `editMessage`, `deleteMessage(…,
  forEveryone)`, `toggleReaction`, `setStarred`, `pinMessage`/`unpinMessage` (cap 5),
  `forwardMessages`, `sendVoice`, `setBlocked`, `observeMentionCandidates`; blocked chats are
  excluded from the auto-reply pipeline. Seeds: deep histories (7–12 days for five chats),
  reply-to-text/voice chains, starred + reaction sets, read-only Morning Runners.
- **S23 additions**: jump/flash scroll targets, unread-divider dismissal on scroll-past,
  scroll-pinned-to-newest behavior with the jump FAB + unseen pill kept from M4a.

## 3. Remaining M4 scope (after M4c)

Remaining after M4c:

- **M4d — Groups & info**: S27 contact info, S28 group info, S29 create/edit group,
  S30 permissions UI, S31 message info (delivered/read lists), S36 in-chat search, plus the
  S32 shared-media grid (its fullscreen viewer S33 already shipped in M4c).
- **M4e — Personalization**: S35 starred/pinned message lists, S37 wallpaper & theme
  (per-chat override already in model), S38 disappearing messages.
- Real recording & capture via the microphone/camera stay Phase 2 by decision D5 — the M4c
  camera is a simulation and voice "recording" produces real playback of the closest bundled
  clip; both are documented in-app and in the project state.
- Model gaps remaining: none blocking (v2 covers every payload); UI-state gaps listed above.

## 4. Non-M4 gaps recorded (NOT in scope)
- Calls tab / People tab / Settings tab placeholders (M5/M6 per PRD §6.6–6.8).
- S70–S74 system screens incl. debug menu (S74 powers PRD §9 mock triggers).
- Internet permission absent (fine Phase 1; add with Phase-2 networking).
- No Room (D1 resolved: in-memory through M4; Room additive later via suspend/Flow seams).
- English-only strings + hardcoded time-label util (localization later).

## 5. Recommended sub-milestones (updated after M4c)

- **M4a ✅ Conversation core** — model v2, message repo + mock pipeline + auto-reply + seed
  history, S23 read-only conversation (header/list/chrome/bubbles/typing/FAB), basic composer.
- **M4b ✅ Composer & messaging** — composer states (reply/edit/voice/blocked/read-only +
  mentions), long-press overlay + quick reactions + action sheet, reaction pills + reactors
  sheet, multi-select batch actions, reply send + quote bars, forward sheet (S34 pulled in;
  now delivered), delete-for-me/everyone, voice send + simulated playback, pinned banner with
  cycling + jump/flash. _Done — this update._
- **M4c ✅ Media & pickers** — delivered (see section 1): S24 attachment tray, S25 picker +
  caption editor, S26 camera simulation, real media bubbles, S33 media viewer, S39 create
  poll, S40 location picker, contact share sheet, SAF documents/audio-as-file, real voice
  playback, seed sweep. _Done — this update._
- **M4d — Groups & info** (next): S27 contact info (presence already in model), S28 group info,
  S29 create/edit group incl. member picker (reuse S17 row patterns), S30 permissions UI,
  S31 message info (delivered/read lists), S36 in-chat search (reuse S20 query approach),
  S32 shared-media grid (viewer exists).
- **M4e — Chat personalization**: S35 starred/pinned lists, S37 wallpaper & accent (per-chat
  override already in model), S38 disappearing messages (poll create shipped in M4c).

## 6. Risks / watch-outs (updated after M4c)

1. **Overlay anchoring**: the lifted bubble is anchored from `LazyListState` at open time and
   the list is frozen while open; incoming messages while an overlay is open can shift rows —
   accepted, mitigated by freeze + anchor recompute on each open.
2. Reverse LazyColumn auto-scroll + FAB pill works; new rows while pinned re-anchor to index 0
   via the bottom-key effect (no visual jump).
3. Recording gesture thresholds (cancel ≈ 140dp left, lock ≈ 110dp up) are fixed dp and tuned
   for a phone; re-check on tablets/landscape when they get real mic audio (M4c).
4. Don't regress `SwipeableRow`/selection interplay: conversation multi-select coexists with
   chat-list selection, but the chat list swipe actions are untouched by M4b.
5. Mock determinism: per-chat auto-reply config + pools live in the repository so tests and
   the S74 debug menu (later) can toggle them.
6. Media budget: bundled assets must stay tiny; placeholders already use `sample://` URIs.
7. Each stage gate: build warnings = 0, RTL/accessibility semantics on new components,
   light+dark previews, no literal strings/colors, no architecture leaks.

## 7. Decisions (D1–D5 from the pre-M4 audit) — **ALL RESOLVED**

| # | Decision | Resolution |
|---|---|---|
| D1 | Message/chat persistence | ✅ **Stay in-memory through M4** (no Room now). All repo methods suspend/Flow; Room adds additively later. |
| D2 | Auto-reply behavior | ✅ PRD default: ≈40% of chats auto-reply 1.5–4 s with typing 1–2 s first (chat set in `MockChatRepository.autoReplyChats`), **plus Pulse Assistant (`u-pulse`/`c-assistant`) always replies** for deterministic demos. Blocked chats skip replies. |
| D3 | Group members/roles | ✅ Modeled in M4a (roles, joinedAt, createdBy…). M4b renders group reads-only state from `Chat.permissions`; role-management UI stays M4d. |
| D4 | Typing pulses in conversation | ✅ Per-chat `Set<String>` typing; idle pulses on `c-aria`, pre-reply typing on the replier — typing row + header "typing…". |
| D5 | Camera/media scope | ✅ M4c delivered: media picking/editing over bundled samples + system photo picker, camera simulation, real voice playback over bundled raw clips. Real camera/mic capture stays Phase 2 by design (no new permissions); recording/capture are simulated and documented. |

_Verdict: M4c complete and clean (0 errors / 0 warnings, previews in both themes, docs
updated). Proceed to M4d (groups & info screens) next; M4e follows. Poll create (S39), the
media-viewer half of S33 and the location picker (S40) were pulled into M4c per the plan._
