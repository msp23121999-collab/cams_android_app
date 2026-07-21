# Suggestions

Product improvement ideas noticed during the production audit. **None of these are bugs
and none are implemented** — they are here for review.

---

## 1. Delete the dead "Misc ViewModels"

`AdminMiscViewModels.kt` and `PrincipalMiscViewModels.kt` contain 8 ViewModel classes.
7 of them are referenced by nothing at all — the real screens use different, properly
wired ViewModels. They take a repository and never call it, so they read as
"unfinished work" to anyone reviewing the code, when in fact the features work fine.

Removing them would make the Admin and Principal portals look as complete as they
actually are. (The 8th, `PrincipalStudyMaterialsViewModel`, **is** used and is a real
defect — that one gets fixed, not deleted.)

## 2. Two unreferenced JSON files

`app/attendance_corrections_db.json` and `app/teaching_logs_db.json` are read by no code.
They are leftovers from before those features moved to the database. Deleting them would
remove a source of confusion about where data lives.

## 3. The `local_json_db` table is a staging post, not a destination

Several features store a JSON document in one database row rather than proper tables:
student papers, student council, parent inquiries, legal events, online meetings,
per-student study-material state.

This was the right immediate fix — it stopped data being lost on every redeploy. But a
JSON blob cannot be queried, joined, or indexed, and two users writing at once still
overwrite each other's whole document. As these features grow, converting them to real
tables would be worth the effort. Start with legal events, which is already the largest.

## 4. Push notifications are half-built

The app ships Firebase messaging and can display notifications, but the backend never
registers device tokens and has no Firebase Admin SDK integration. Server-originated push
therefore does not work at all. Either finish the server side or remove the client
scaffolding, so it stops reading as a working feature.

## 5. Parent role is narrower than the others

Parents have dashboard, attendance, marks, fees, timetable, circulars and profile — but no
grievances, leave requests, faculty meeting requests or chatbot, all of which exist for
other roles. This may well be deliberate; worth making it an explicit product decision
rather than an accident of what got built first.

## 6. No CI pipeline

The test suite exists, passes, and has been verified to fail when the fixes it guards are
reverted. But nothing runs it automatically, so a bad commit reaches the main branch
unchecked. A GitHub Actions workflow running the suite against PostgreSQL on every push
would make every guarantee in this audit durable rather than a snapshot.

This is the highest-value item on the list.

## 7. Smaller items

- **Budget & Grants has no PDF export** while comparable modules do.
- **Settings screens** show permanently-disabled Language / Theme rows because no locale or
  theme infrastructure exists. Either build them or remove the rows.
- **`AdminInventoryViewModel2` / `Library2` / `Transport2`** — the `2` suffix suggests a
  rewrite where the original was never removed. Renaming them once the dead originals are
  deleted would remove a "which one is real?" question for the next developer.
