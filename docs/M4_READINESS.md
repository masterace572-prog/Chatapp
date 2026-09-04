# M4 Readiness — Pulse

_Prepared 2026-09-04 from the pre-M4 audit (files at repo head `575a11b` + audit commit)._
_Milestone status: M1 ✅ M2 ✅ M3 ✅ — M4 pending. Recommended split: M4a…M4e below._

Verdict: **READY WITH NOTES** — no blockers. One design decision required before M4a (see §6).

---

## 1. Why READY (what the audit verified)

- Clean build: `./gradlew clean :app:assembleDebug` BUILD SUCCESSFUL, zero warnings.
- All M3 checkable PRD items verified (S18 shell, S19 list + swipe actions + multi-select,
  S20 search, S21 archived, S22 folders editor). M4-prerequisite UI exists and is tested
  per-milestone (screenshots + user review of each ship).
- Route surface is complete up to the conversation entry: every chat row (Chats/Archived/Search
  results, folder chips drill-down) already navigates into `chat/{chatId}`.
- Architecture/tokens/components (see `docs/PROJECT_STATE.md`) give M4 all building blocks it
  needs; auth session persistence + back handling hardened in this audit.
- Persistence gap is contained and consciously scoped (see §6 decision D1).

## 2. PRD §6.5 gaps — M4 scope (S23–S40)

M4 ships the Conversation area: **S23 Chat Screen** (top bar w/ unread badge + call actions +
menu, message list w/ date separators, unread divider, system messages, bubble grouping,
message-type composables, failed-message retry, scroll-to-bottom FAB, long-press reactions +
action sheet, multi-select, pinned banner), **S24 Attachment Sheet**, **S25 Media Picker/Editor**,
**S26 Camera**, **S27 Contact Info**, **S28 Group Info**, **S29 Create/Edit Group**,
**S30 Group Permissions**, **S31 Message Info**, **S32 Shared Media**, **S33 Media Viewer**,
**S34 Forward Sheet**, **S35 Starred/Pinned**, **S36 In-Chat Search**, **S37 Wallpaper & Theme**,
**S38 Disappearing Messages**, **S39 Create Poll**, **S40 Location Picker**.

### Audit-found gaps in today's model/UI (need building in M4a/b)
- Message attachments (image/video/file/voice/location/contact/poll metadata), reply-quote
  anchor, forwarded+edited flags, reactions, system-message subtype. → `Message` model extension
  (M4a, see PROJECT_STATE §5 gaps).
- Chat extras: group participants beyond id list (role, addedBy, joinedAt), pinned messages,
  disappearing timer, wallpaper/accent override, blocked/read-only state, draft persistence.
  → `Chat`/new `GroupMember` model (M4a).
- Composer component does not exist at all yet (PRD §7 lists `Composer`, `VoiceWaveform`,
  `ReactionPill`, `DateSeparator`, `SystemMessage`, `UnreadDivider`, `MessageBubble` as library
  components to build first) — these are pure new component work (M4a/M4b).
- `User` lacks online/lastSeen/verified/blocked (needed for chat status line + Contact Info).
- Media assets: PRD §9 requires bundled sample images + short audio voice notes (resource batch).

## 3. Non-M4 gaps recorded (NOT in scope for M4; listed so they are not re-found)
- Calls tab / People tab / Settings tab = placeholders (M5/M6 per §6.6–6.8; S42+ S43+ S54+).
- S70–S74 system screens incl. debug menu (S74 powers PRD §9 mock behaviors) — schedule with M5+.
- Internet permission absent from manifest (fine Phase 1; add with Phase-2 networking only).
- No Room, no Supabase (Phase 1 §9 persistence = Room; decision D1).
- English-only strings + hardcoded time labels util (localization later).

## 4. Recommended sub-milestones

Suggested order (each independently buildable + shippable; gate each like M3):
- **M4a — Conversation core**: domain model v2 (Message content types, Chat groups v2, GroupMember),
  mock store rework (message sends with status progression + PRD §9 auto-replies w/ typing
  indicator), chat screen S23 read-only rendering: bubble system (all message kinds incl. media
  placeholders), date separators, system messages, unread divider, typing indicator, FAB + scroll.
  Persistence decision D1 lands here (Room or keep in-memory; PRD says Room).
- **M4b — Composer & messaging**: Composer all states (typing/reply/edit/recording/locked/blocked),
  send flow w/ mock delay + auto-reply, reply bar + long-press action sheet, reactions row,
  failed-message retry, pinned-message banner, draft persistence hookup, group typing/member avatars.
- **M4c — Media & pickers**: S24 attachment sheet, S25 gallery picker (bundled sample images),
  S26 camera screen (viewfinder placeholder + capture simulation), S27 Contact Info, S34 Forward
  sheet, S32 Shared Media tab skeleton, S33 Media Viewer.
- **M4d — Groups & info**: S28 Group Info, S29 create/edit group (participant picker = reuse S17/
  search row patterns), S30 permissions UI (mock storage), S31 Message Info (delivered/read lists),
  S36 in-chat search (reuse S20 query approach over chat messages).
- **M4e — Chat personalization**: S37 wallpaper & accent picker (swatch row component), S38
  disappearing messages, S39 poll (send + render + vote UI), S40 location placeholder card, S35
  starred/pinned lists, settings hooks (mute/notification toggles wired to chat row state).

## 5. Risks / watch-outs
1. **Bubble geometry + grouping** (corner radii per first/middle/last run, 2 dp tight gap) is the
   most fiddly pure-UI work in the app; prototype in a preview before wiring data.
2. **Reverse LazyColumn** message list with auto-scroll + FAB unread badge + reply-jump highlight
   needs `rememberLazyListState` experiments; keep list composables stateless w/ previews.
3. **Long-press UX**: PRD wants quick-reaction bar AND action sheet from one press — plan timing/
   gestures in M4b; consider `combinedClickable`.
4. **Swipe actions on chat rows** already exist (M3) — M4 contact info etc. must not regress the
   `SwipeableRow` interplay (selection vs swipe conflicts).
5. **Mock determinism**: PRD §9 auto-reply (40% chats, 1.5–4 s) must live in the repository with a
   deterministic seed for previews; S74 debug menu (later) will toggle it.
6. **Media budget**: sample images/videos must be tiny (mock assets, single drawable reuse);
   keep APK lean.
7. Each M4 stage re-verifies: build warnings = 0, RTL/accessibility semantics on new components,
   previews light+dark, no literal strings/colors, no architecture leaks.

## 6. Decisions needed before M4a (user)
- **D1 — Message/chat persistence (PRD §9 mandates Room)**: Options: (a) adopt Room in M4a for
  messages+chats and make mocks seed Room; (b) keep in-memory singleton store through M4 and defer
  Room to Phase 2 with the Supabase swap; (c) Room now but Supabase later replaces anyway.
  Recommendation: (b) keeps M4 velocity and the repository seam unchanged — Room adds no user
  value before Phase 2 unless relaunch persistence of messages is explicitly desired in demos.
- **D2 — Mock auto-reply cadence**: PRD says 40% of chats auto-reply 1.5–4 s w/ typing first. OK
  to ship that default, or prefer replies only from a specific “bot” contact for demo clarity?
- **D3 — M4a group support depth**: seed groups (4 exist) get real member lists/roles now, or
  groups stay shallow until M4d (info/create/edit)? Affects model-v2 size in M4a.
- **D4 — Typing indicator as simulated data** (already mock-pulsed in M3 rows): extend pulses to
  the conversation screen (recommended) — confirm.
- **D5 — Scope of camera/media pickers in M4c**: simulate with bundled assets vs. real photo
  picker API (real picker = device dependency + permissions; PRD Phase 1 says bundled samples).

_Verdict: proceed to M4a once D1–D5 are answered. No code changes required before starting; M4a
builds on the current head cleanly._
