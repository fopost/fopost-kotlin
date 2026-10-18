# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Changed

- **Breaking:** `ads.uploadConversions` answers a `ConversionUploadResult` (`id`, `accepted`,
  `eventCount`, `replayed`) instead of the accepted count alone, and takes an optional
  `idempotencyKey` sent as the `Idempotency-Key` header.
- `ConversionEvent` gained `eventId`, `actionSource` (`ConversionActionSources`), `sourceUrl`,
  `externalId`, `clientIpAddress`, `clientUserAgent`, `clickId`, `browserId` and `contentIds`.

### Added

- `inbox.repost`, `unrepost`, `bookmark` and `unbookmark` for X items, with `reposted`,
  `bookmarked`, `canRepost` and `canBookmark` on `InboxItem`.
- `accounts.followXUser(accountId, userId)` and `unfollowXUser`, answering an `XFollowResult`.
- `accounts.getXPost(postId, accountId)` and `accounts.searchXPosts(XSearchParams)`, pass-through
  reads of one X post and of recent X posts as `XPost`, paged by `XSearchPage.nextToken`.
- Every X engagement call is metered to the workspace; a spent X balance answers a
  `402 platform_balance_spent`, and an X account connected before this release answers
  `409 reconnect_required` for repost, bookmark and follow until it is reconnected.
- `ads.sendPixelConversions(params, idempotencyKey)`: the Conversions API send with a required
  `Idempotency-Key`; a replay answers `replayed = true` and sends nothing. `ads.conversionBatches`
  lists the stored batches as `ConversionBatch`, identifiers hashed.
- Pixels: `ads.pixels`, `createPixel`, `pixel`, `updatePixel`, `sharePixel`, `pixelStats` and
  `eventMatchQuality`, with `AdPixel`, `AdPixelDetail`, `AdPixelStats`, `EventMatchQuality` and
  `PixelDataUseSettings`.
- Custom conversions: `ads.customConversions`, `createCustomConversion`, `customConversion`,
  `updateCustomConversion` and `deleteCustomConversion`, with `CustomConversion`,
  `CustomConversionRule` and `CustomConversionEventTypes`.
- All of these need the `ads` scope only.

## [0.4.0] - 2026-10-18

### Added

- `queue`: the posting queue. `queue.slots(workspaceId)`, `createSlot`, `updateSlot`, `deleteSlot`,
  `next` and `preview` as `QueueSlot`, `QueueSlotDeleted` and `QueueNext`, with
  `CreateQueueSlotParams` and `UpdateQueueSlotParams`. `CreatePostParams` and `UpdatePostParams`
  take `schedule = PostSchedule.QUEUE` to drop a post into the next free slot.
- `posts.unpublish(postId)`: takes a live post down from every network, answering
  `UnpublishResult` with what each one did. `PostStatus.UNPUBLISHED` is new.
- `connectSessions`: hosted connect links. `connectSessions.create(params)` and `get(id)` as
  `ConnectSession`, with `CreateConnectSessionParams` and `ConnectSessionBrandingParams`.
- `webhooks.deliveries(webhookId, status, limit)`, `delivery(webhookId, deliveryId)` and
  `redeliver(webhookId, deliveryId)` as `WebhookDelivery`.
- `automations.versions(automationId, page, perPage)`, `version(automationId, version)` and
  `restoreVersion(automationId, version)` as `AutomationVersion`, `AutomationVersionRestore` and
  the `WorkflowGraph` node and edge types. `AutomationStep.nodeId` is new, and `PageMeta.lastPage`
  also reads `total_pages`.
- `logs.list(params)`: the API request log as `RequestLogPage`, with `LogListParams` filters and
  the period's `RequestLogSummary`.
- `analytics.inbox(params)`: inbox volume, response times and the busiest hours as
  `InboxAnalytics`, scoped by `InboxAnalyticsParams`.
- `inbox.deleteConversation(conversationId, accountId)` and
  `inbox.attachment(itemId, index, variant)`, which downloads the attachment bytes.
- Every method that builds its path from fragments now names the API operation it wraps in its
  KDoc: WhatsApp, Google Business Profile, Discord, remote blog articles, and the ad duplicate,
  reach-and-frequency and Meta authorize calls.

## [0.3.0] - 2026-10-18

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
- `accounts.listRedditSubreddits(accountId)`, `listRedditSubredditRules(accountId, subreddit)`,
  `listRedditFlairs(accountId, subreddit)` and `setRedditDefaultSubreddit(accountId, subreddit)` as
  `RedditSubreddit`, `RedditSubredditRule` and `RedditFlair`; a flair id is valid only in its
  subreddit. `validate.subreddit(accountId, name)` answers the same existence check as
  `SubredditValidation` before a post exists, and `inbox.vote(itemId, direction)` votes an item
  `up` or `down` or takes the vote back with `none`. `Account.reconnectRequired` and `InboxItem.vote`
  and `canVote` are new fields.
- `blogs`: the blogs, articles and products that already live on a connected site, addressed by the
  platform's own ids. `blogs.listBlogs(accountId)`, `listArticles`, `getArticle`, `createArticle`,
  `updateArticle`, `deleteArticle`, `listProducts` and `updateProduct` as `RemoteBlog`,
  `RemoteArticle` and `RemoteProduct`, with `ArticleParams` and `ProductParams` bodies.
  `updateArticle` edits the live article in place and sends only the fields named. Reads need the
  `posts` scope; writes need `publish` too.

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
