# M4 Readiness & Milestone Plan — Pulse

_Prepared 2026-09-04 from the pre-M4 audit; updated 2026-09-04 after M4a._
_Milestone status: M1 ✅ M2 ✅ M3 ✅ Audit ✅ **M4a ✅** — M4b…M4e pending._

---

## 1. M4a delivered (what was built)

- **Domain model v2** (`domain/model/ChatMessage.kt`, `User.kt`): sealed `MessageContent`
  hierarchy with every PRD content type carrying realistic payload fields; Message flags
  (replyTo/forwarded/edit/delete/star/pin/reactions/linkPreview); `GroupMember`+`ChatRole`,
  group metadata, `ChatPermissions`, disappearing/wallpaper/block fields; `User` presence +
  verified + blocked. **No further model migration planned before Phase 2** — M4b–M4e build on
  this shape (see PROJECT_STATE §5).
- **Repository + mock behavior** (extended `ChatRepository` — justified in interface KDoc:
  messages/chats are one aggregate and one mock store): observeChat/observeMessages (hot)/
  observeTyping(set), sendText with Sending→Sent→Delivered→Read pipeline, ~5% failures +
  always-succeeding retry, auto-reply (≈40% of chats + **Pulse Assistant always**, D2), unread
  increments only when the chat is not on screen, mutable drafts, typing pulses extended to the
  conversation (D4).
- **Seed history** (~441 messages across 16 chats, 20–45 each, multi-day spans, runs, system
  rows, media placeholders, links, edited/deleted/reactions/unread tails; 17th contact
  Pulse Assistant).
- **S23 screen core**: `chat/{chatId}` stub replaced by ConversationScreen/ViewModel: ChatHeader
  (back + other-chats unread badge, 40dp avatar, online/last-seen/typing/N-members status,
  call icons, more menu w/ mute wired + placeholders), reverse LazyColumn with date pills,
  unread divider (captured once on open), system rows, bubble runs (2dp gaps, 18/4dp radii,
  group sender name + run avatar), text links, edited/deleted/failed-retry, content-type
  placeholder bubbles, typing bubble, scroll FAB with unseen pill, basic Composer
  (attachment/camera/mic placeholders, mic↔send crossfade, 6-line grow, drafts saved/restored).
- **Design-token debt**: PulseIconSizes tiny/small; PulseSizes additions; `PulseFontWeights`;
  `avatarInitialsFontSize`; bubble shape tokens + `PulseBubbleShape`; all component
  typography/icon literals from the audit now use tokens.
- Zero-warning clean build; docs updated (this file + PROJECT_STATE).

## 2. PRD §6.5 gaps — remaining M4 scope (S23–S40)

M4 = Conversation area. M4a covered the S23 **core**. Remaining:

- **S23 (rest, M4b)** — long-press quick reactions + action sheet (Reply/Forward/Copy/Pin/Star/
  Edit/Info/Delete/Select), multi-select mode, reply/forward rendering (reply quote bar),
  pinned-message banner, mention suggestions, blocked/read-only composer state, media bubbles
  (image grids, video, voice playback waveform, file cards, location card, contact card, poll
  UI, sticker panel) — attachments UI, camera, media picker (M4c).
- **S24 Attachment Sheet**, **S25 Media Picker/Editor**, **S26 Camera** (D5: bundled sample
  assets; real picker decisions pending), **S27 Contact Info**, **S28 Group Info**,
  **S29 Create/Edit Group**, **S30 Group Permissions UI**, **S31 Message Info**,
  **S32 Shared Media**, **S33 Media Viewer**, **S34 Forward Sheet**, **S35 Starred/Pinned**,
  **S36 In-Chat Search**, **S37 Wallpaper & Theme**, **S38 Disappearing Messages**,
  **S39 Create Poll**, **S40 Location Picker**.
- Model gaps remaining: none blocking (v2 covers payloads); UI-state gaps only (e.g. composer
  reply/edit/recording states, waveform rendering, reaction sheets, group admin surfaces).

## 3. Non-M4 gaps recorded (NOT in scope)
- Calls tab / People tab / Settings tab placeholders (M5/M6 per §6.6–6.8).
- S70–S74 system screens incl. debug menu (S74 powers PRD §9 mock triggers).
- Internet permission absent (fine Phase 1; add with Phase-2 networking).
- No Room (D1 resolved: in-memory through M4; Room additive later via suspend/Flow seams).
- English-only strings + hardcoded time labels util (localization later).

## 4. Recommended sub-milestones (updated after M4a)

- **M4a ✅ Conversation core** — model v2, message repo + mock pipeline + auto-reply + seed
  history, S23 read-only conversation (header/list/chrome/bubbles/typing/FAB), basic composer
  + drafts. _Done._
- **M4b — Composer & messaging** (recommended next): full composer states (reply/edit bar,
  recording UI, mentions), long-press action sheet + quick reactions, reaction rows under
  bubbles, multi-select, reply send (already supported by the model: `replyToMessageId`),
  pinned-message banner, failed-message retry polish, voice waveform rendering for incoming
  Voice content.
- **M4c — Media & pickers**: S24 attachment sheet, S25 picker over bundled sample images
  (D5), S26 camera simulation, media bubbles (image grids/video/file cards) replacing
  placeholders, S32/S33 shared media + viewer skeletons, S34 forward sheet (repo:
  forward support flag already on the model).
- **M4d — Groups & info**: S27 contact info (presence already in the model), S28 group info,
  S29 create/edit group incl. member picker (reuse S17 row patterns), S30 permissions UI,
  S31 message info (delivered/read lists), S36 in-chat search (reuse S20 query approach over
  `observeMessages`).
- **M4e — Chat personalization**: S37 wallpaper & accent (per-chat override already in model),
  S38 disappearing messages, S39 poll create/render/vote, S40 location placeholder card,
  S35 starred/pinned lists.

## 5. Risks / watch-outs (updated)
1. Bubble geometry is done (radii/grouping); the next geometry risk is **reactions row + reply
   quote rendering inside the same bubble column** — keep MessageBubble stateless.
2. Reverse LazyColumn auto-scroll + FAB pill works; **animateItem** fade is in; placement slide
   (8dp) is approximated — polish in M4b if needed.
3. Long-press UX (reaction bar + action sheet from one press) needs gesture planning in M4b.
4. Don't regress `SwipeableRow`/selection interplay when adding multi-select in chat (M4b).
5. Mock determinism: keep per-chat auto-reply config and pools in the repository so tests and
   the S74 debug menu (later) can toggle them.
6. Media budget: bundled assets must stay tiny; placeholders already use `sample://` URIs.
7. Each stage gate: build warnings = 0, RTL/accessibility semantics on new components,
   light+dark previews, no literal strings/colors, no architecture leaks.

## 6. Decisions (D1–D5 from the pre-M4 audit) — **ALL RESOLVED**

| # | Decision | Resolution |
|---|---|---|
| D1 | Message/chat persistence | ✅ **Stay in-memory through M4** (no Room now). All repo methods suspend/Flow; Room adds additively later. |
| D2 | Auto-reply behavior | ✅ PRD default: ≈40% of chats auto-reply 1.5–4 s with typing 1–2 s first (chat set in `MockChatRepository.autoReplyChats`), **plus Pulse Assistant (`u-pulse`/`c-assistant`) always replies** for deterministic demos. |
| D3 | Group members/roles | ✅ Modeled now (`Chat.members`, roles OWNER/ADMIN/MEMBER, joinedAt; createdBy/createdAt, description). Role-management UI deferred to M4d. Group chats render sender name, per-sender muted color (avatar-palette-derived) and small avatar on the last bubble of a run. |
| D4 | Typing pulses in conversation | ✅ Typing is now per-chat `Set<String>`; idle pulses target `c-aria` and pre-reply typing targets the replier — both surface as the typing row + header "typing…" on the conversation screen. |
| D5 | Camera/media scope | ✅ Media sending is M4c. M4a only added the `MessageContent` attachment model (image uris/dimensions, video duration, voice waveform, file size/mime, location, contact, poll, sticker) so M4c can use the real Android Photo Picker (`PickVisualMedia`) plus bundled sample assets. No camera work done. |

_Verdict: M4a complete and clean. Proceed to M4b (composer & messaging) next — it builds
directly on the current head with no model or repository changes required._
