# M4 Readiness & Milestone Plan — Pulse

_Prepared 2026-09-04 from the pre-M4 audit; updated 2026-09-04 after M4a, again after M4b._
_Milestone status: M1 ✅ M2 ✅ M3 ✅ Audit ✅ M4a ✅ **M4b ✅** — M4c…M4e pending._

---

## 1. M4b delivered (what was built)

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

## 2. PRD §6.5 gaps — remaining M4 scope (S23–S40)

Remaining after M4b:

- **M4c — Media & pickers**: S24 attachment sheet, S25 media picker/editor over bundled
  samples (D5), S26 camera simulation, real media bubbles replacing placeholders (image
  grids, video player, file cards), S32 shared media, S33 media viewer, S40 location picker.
- **M4d — Groups & info**: S27 contact info, S28 group info, S29 create/edit group,
  S30 permissions UI, S31 message info (delivered/read lists), S36 in-chat search.
- **M4e — Personalization**: S35 starred/pinned message lists, S37 wallpaper & theme,
  S38 disappearing messages, S39 create poll (voting data already modeled).
- Voice playback with real audio + recording via the microphone (S23 voice; simulated in M4b).
- Model gaps remaining: none blocking (v2 covers every payload); UI-state gaps listed above.

## 3. Non-M4 gaps recorded (NOT in scope)
- Calls tab / People tab / Settings tab placeholders (M5/M6 per PRD §6.6–6.8).
- S70–S74 system screens incl. debug menu (S74 powers PRD §9 mock triggers).
- Internet permission absent (fine Phase 1; add with Phase-2 networking).
- No Room (D1 resolved: in-memory through M4; Room additive later via suspend/Flow seams).
- English-only strings + hardcoded time-label util (localization later).

## 4. Recommended sub-milestones (updated after M4b)

- **M4a ✅ Conversation core** — model v2, message repo + mock pipeline + auto-reply + seed
  history, S23 read-only conversation (header/list/chrome/bubbles/typing/FAB), basic composer.
- **M4b ✅ Composer & messaging** — composer states (reply/edit/voice/blocked/read-only +
  mentions), long-press overlay + quick reactions + action sheet, reaction pills + reactors
  sheet, multi-select batch actions, reply send + quote bars, forward sheet (S34 pulled in;
  now delivered), delete-for-me/everyone, voice send + simulated playback, pinned banner with
  cycling + jump/flash. _Done — this update._
- **M4c — Media & pickers** (recommended next): S24 attachment sheet, S25 picker over bundled
  sample images (D5), S26 camera simulation, media bubbles (image grids/video/file cards)
  replacing placeholders, voice playback with real audio, S32/S33 shared media + viewer
  skeletons, S40 location card.
- **M4d — Groups & info**: S27 contact info (presence already in model), S28 group info,
  S29 create/edit group incl. member picker (reuse S17 row patterns), S30 permissions UI,
  S31 message info (delivered/read lists), S36 in-chat search (reuse S20 query approach).
- **M4e — Chat personalization**: S35 starred/pinned lists, S37 wallpaper & accent (per-chat
  override already in model), S38 disappearing messages, S39 poll create/render/vote.

## 5. Risks / watch-outs (updated)

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

## 6. Decisions (D1–D5 from the pre-M4 audit) — **ALL RESOLVED**

| # | Decision | Resolution |
|---|---|---|
| D1 | Message/chat persistence | ✅ **Stay in-memory through M4** (no Room now). All repo methods suspend/Flow; Room adds additively later. |
| D2 | Auto-reply behavior | ✅ PRD default: ≈40% of chats auto-reply 1.5–4 s with typing 1–2 s first (chat set in `MockChatRepository.autoReplyChats`), **plus Pulse Assistant (`u-pulse`/`c-assistant`) always replies** for deterministic demos. Blocked chats skip replies. |
| D3 | Group members/roles | ✅ Modeled in M4a (roles, joinedAt, createdBy…). M4b renders group reads-only state from `Chat.permissions`; role-management UI stays M4d. |
| D4 | Typing pulses in conversation | ✅ Per-chat `Set<String>` typing; idle pulses on `c-aria`, pre-reply typing on the replier — typing row + header "typing…". |
| D5 | Camera/media scope | ✅ Media sending is M4c; M4b simulated voice playback keeps the `MessageContent.Voice` waveform model usable by M4c real audio. No camera work done. |

_Verdict: M4b complete and clean (0 errors / 0 warnings, previews in both themes, docs
updated). Proceed to M4c (attachment sheet, media pickers + camera, real media bubbles) next.
Forward sheet (S34) already shipped in M4b; remaining screens are M4c–M4e._
