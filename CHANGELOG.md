# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Removed

- **Breaking:** `BlogSettings.autoPublish` and `UpdateBlogSettingsParams.autoPublish`. The API no
  longer auto-publishes; an article publishes only after `blog.submitArticle` moves it to review.

### Added

- `blog.suggestSeeds(workspaceId, params)`: about 10 search terms from the blog goal and topics as
  `BlogSeedSuggestions`, needing the `ai` scope. `SuggestBlogSeedsParams` takes the current `seeds`
  to refine and a `prompt` to steer.
- `blog.submitArticle(workspaceId, articleId)`: moves a draft, failed or rejected article to the new
  `BlogArticleStatus.IN_REVIEW`, needing the `posts` scope. Publishing and setting `publishAt` now
  need an article in review; a draft answers `409`.
- `BlogSettings.goal` (`BlogGoal`), `focusTopics`, `avoidTopics` and `planRun` (`BlogPlanRun`,
  `BlogPlanRunStatus`), with `goal`, `focusTopics` and `avoidTopics` on `UpdateBlogSettingsParams`.
  `BlogKeyword.fitReason` says why a keyword serves the goal.

- `media.limits(workspaceId)`: the account's upload ceilings as `UploadLimits`, with the per-file
  maximum by any route, the multipart maximum, the file count per request, and the next tier.
- `posts.create(params, files)` and `posts.update(id, params, files)` take up to five `UploadFile`s
  and attach them to the first content block. Files under the multipart ceiling travel in the post
  request; larger ones are uploaded directly first and attached as media items.
- `UploadFile` streams from a `File` or an `InputStream` of known size.
- `FileTooLargeException` for a `413 file_too_large`, carrying `maxFileBytes`, `fileBytes`, and
  `upgradeUrl`.
- `PresignUploadParams` takes `folderId`, `origin`, `brandRole`, `brandVoiceId`, and
  `personConsent`; `PresignedUpload` carries `maxFileBytes`.

### Changed

- The blog resource follows the live wire format: `workspace_id` on the query string, snake_case
  response fields, and bare (unwrapped) single objects. `BlogWebsiteTest.status` is now `httpStatus`,
  read from `http_status`; a failed test carries the API's message in `error`.
- `blog.planKeywords` no longer fills the calendar: results land as `suggested`, and setting a
  keyword to `planned` picks it. `reviewHoldDays` is now the days before its planned date that a
  picked keyword starts being written.
- `uploadDirect` streams the file instead of holding it in a `ByteArray`, and `presign` accepts a
  `size` up to the account's `maxFileBytes` (5 GB at the top).
