# Repository Guidelines

## Agent Workflow Rule
- Start every task by checking `AGENTS.md` first before scanning other files.
- Record durable project decisions here so future updates do not require re-reading the full codebase.
- When behavior/spec changes, update this file in the same task.

## Persistent Notes (Update When Needed)
- Product context: this app is schedule management (`/calendar`, `/api/schedules`), not a pure ToDo app.
- Primary local DB setting: `jdbc:h2:file:./data/schedule-db` (`spring.h2.console.path=/h2-console`).
- DB initialization baseline moved to Flyway (`src/main/resources/db/migration`); production/local PostgreSQL should not rely on `spring.sql.init`.
- Keep integration and docs wording aligned to schedule terminology (`予定`, `スケジュール`).
- Admin moderation page: `/admin` (admin-only via `AdminGuardService`), APIs under `/api/admin/moderation`.
- Admin can review all users' schedules/comments by selecting `全ユーザー` and can delete schedules, board posts, board comments, and DMs.
- Board posts on `/board` can be edited/deleted by the post author via post detail actions; edit opens an in-page form and updates via `更新` button (`PUT/DELETE /api/board/posts/{postId}`).
- Board posts on `/board` support participation flow similar to schedules: users send join requests, authors approve/reject, and Discord invite URL is visible only to author or approved participants.
- `/board` landing shows only menu cards (`スレッド作成`, `スレッド一覧`); thread creation form is on dedicated `/board/create` page.
- `/board/create` thread creation form supports optional Discord invite URL input; URL visibility remains limited to post author and approved participants.
- `/board/create` thread creation form includes `デバイス` selection (`PC` or `家庭用ゲーム機`), stored as `board_post.device_type`.
- `/board` post summary list is rendered as cards (friend-list-like style) showing `何月何日`, `何時`, `募集人数`; tapping a card opens post detail.
- Game title emoji badge mapping is shared via `/js/game-badges.js` and used across board/top pages for consistent game markers.
- Board recruitment posts are auto-cleaned daily: posts with `schedule_date` earlier than today are deleted, and threads with no remaining posts are also removed.
- Calendar schedule creation supports pre-registering participants for joinable schedules via friend selection and manual name input; `あと何人募集するか` is treated as the additional recruit count, not the total cap, and saved names are shown in `参加中` together with approved participants.
- Schedule share rule: joinable schedule re-share is allowed only after the requester is approved as participant (`schedule_participant`), and UI should explain approval-gated sharing to requester.
- Joinable schedule supports Discord invite URL (`discord_invite_url`); owner updates it on `/schedules/{id}/discord-invite`, and participants/owner can use it from schedule card.
- Join request supports optional game ID (`schedule_join_request.game_id`); requester can submit without it, and owner sees it with pending requests when provided.
- Registration no longer accepts display name input; initial `display_name` is automatically set to the same value as `username`.
- Friend list page supports deleting accepted friendships via `/api/friends/{friendUserId}` with two-step client confirmation.
- Friend profile page (`/friends/profile/{username}`) displays `X URL` and `配信URL` as clickable links (open in new tab) when set.
- Friend search/profile lookup image (`/api/users/{userId}/profile-image`) is viewable for authenticated users so searched users' selected profile images are reflected even before friendship.
- Friend list page (`/friends/list`) avatar uses each user's configured profile image when available; otherwise falls back to color-based default icon.
- Friend requests page (`/friends/requests`) incoming/outgoing cards use the same avatar/card visual style as friend list, including profile image fallback behavior.
- Friend requests page (`/friends/requests`) has a back button to return to `/friends`.
- Friend level ranking avatar (friends page/home widget) uses each user's configured profile image when available; otherwise falls back to color-based default icon.
- Main navigation (`.hero-nav`) uses an outer rounded container with pill-style active tab highlight (`.is-current`) across calendar/friends/board/profile pages.
- Calendar top hero shows `GameSchedule` branding and stats (`オンライン`, `イベント`) sourced from `/api/nav/badges`; current implementation defines `オンライン` as accepted friend count and `イベント` as this-month owned schedule count.
- UI-only local preview page (no DB required) is available at `/demo/nav-preview.html` to validate top hero/nav typography and colors against `calendar.css`.

## Project Structure & Module Organization
- Main backend app lives in `src/main/java/com/example/schedulemanager`.
- Web/API layers are split by package: `controller`, `service`, `mapper`, `model`, `dto`, `config`, `security`.
- Thymeleaf templates are in `src/main/resources/templates`, static assets in `src/main/resources/static` (`css`, `js`, `img`).
- DB bootstrap files are `src/main/resources/schema.sql` and `src/main/resources/data.sql`.
- Tests are under `src/test/java`.
- Supporting docs are in `docs/`; a separate DM platform prototype exists in `x-dm-platform/`.

## Build, Test, and Development Commands
- `./mvnw spring-boot:run`: start the app locally with dev defaults.
- `./mvnw test`: run unit/integration tests.
- `./mvnw clean package`: build a runnable JAR in `target/`.
- `./mvnw clean verify`: full verification lifecycle (compile + test + package checks).
- `docker build -t schedule-manager-api .`: build local Docker image.

## Coding Style & Naming Conventions
- Language: Java 21, Spring Boot + MyBatis.
- Use 4-space indentation; keep methods focused and small.
- Class names: `PascalCase` (`ScheduleApiController`), methods/fields: `camelCase`.
- Keep controller endpoints resource-oriented (e.g., `/api/schedules/...`).
- Mapper names should match domain entities (`ScheduleMapper`, `FriendshipMapper`).
- Frontend JS/CSS files should be feature-scoped (`calendar.js`, `friends.css` style).

## Testing Guidelines
- Framework: Spring Boot Test (`spring-boot-starter-test`) and MyBatis test starter.
- Place tests in mirrored package paths under `src/test/java`.
- Test class naming: `*Tests` (example: `ScheduleManagerApplicationTests`).
- Prefer service/controller behavior tests for new features and regression fixes.
- Run `./mvnw test` before opening a PR.

## Commit & Pull Request Guidelines
- Recent history favors short, purpose-first Japanese commit titles (e.g., `DMの削除`, `カレンダー周り`). Keep one clear topic per commit.
- Recommended commit format: `<対象>: <変更内容>` (example: `予定共有: 参加申請承認APIを追加`).
- PRs should include:
  - what changed and why,
  - impacted endpoints/pages,
  - DB/schema changes if any,
  - screenshots/GIFs for UI changes,
  - test evidence (`./mvnw test` output summary).

## Security & Configuration Tips
- Do not commit secrets; use environment variables for mail/VAPID settings.
- Default local DB is file-based H2 (`jdbc:h2:file:./data/schedule-db`).
- Confirm role/authorization behavior when changing controller/service logic.
