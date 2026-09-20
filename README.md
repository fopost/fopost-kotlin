# fopost-kotlin

[![CI](https://github.com/fopost/fopost-kotlin/actions/workflows/ci.yml/badge.svg)](https://github.com/fopost/fopost-kotlin/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.fopost/fopost-kotlin.svg?label=maven%20central)](https://central.sonatype.com/artifact/com.fopost/fopost-kotlin)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Official Kotlin SDK for the [FoPost](https://fopost.com) API. Schedule and publish to +30 social
platforms from your code — on the JVM and on Android.

```kotlin
// build.gradle.kts
implementation("com.fopost:fopost-kotlin:0.3.0")
```

```xml
<dependency>
  <groupId>com.fopost</groupId>
  <artifactId>fopost-kotlin</artifactId>
  <version>0.3.0</version>
</dependency>
```

Targets JVM 11. Transport is OkHttp, JSON is kotlinx-serialization, and the public API is
coroutines all the way down.

> **0.x release.** The public API is still settling and minor versions may contain breaking
> changes. Pin an exact version if that matters to you.

## Quick start

```kotlin
import com.fopost.FoPost
import com.fopost.param.CreatePostParams
import com.fopost.param.contentOf

FoPost("fp_...").use { client ->          // or set FOPOST_API_KEY
    val workspace = client.workspaces.list().first()
    val accounts = client.accounts.list(workspace.id)

    val post = client.posts.create(
        CreatePostParams(
            workspaceId = workspace.id!!,
            accounts = accounts.mapNotNull { it.id },
            content = contentOf("Hello from Kotlin"),
        ),
    )

    client.posts.publish(post.id!!)
}
```

A post is one or more content blocks. One block is a plain update; several make a thread:

```kotlin
client.posts.create(
    CreatePostParams(
        workspaceId = workspaceId,
        accounts = listOf(accountId),
        content = listOf(
            ContentBlockInput("First post in the thread"),
            ContentBlockInput(
                "Second one, with an image",
                media = listOf(MediaItem(type = "image", name = "chart.png", url = "https://.../chart.png")),
            ),
        ),
    ),
)
```

## Coroutines

Every call is a `suspend` function, so nothing blocks the thread it was started on. Call them
from any coroutine scope:

```kotlin
val posts = coroutineScope {
    val drafts = async { client.posts.list(PostListParams(status = PostStatus.DRAFT)) }
    val scheduled = async { client.posts.list(PostListParams(status = PostStatus.SCHEDULED)) }
    drafts.await().data + scheduled.await().data
}
```

Cancelling the coroutine cancels the in-flight HTTP call, and a cancelled request is never
retried. From blocking code, wrap the call in `runBlocking`.

`FoPost` owns a connection pool, so `close()` it when you are done — `use { }` does that for you.
A client you pass your own `OkHttpClient` to leaves that client alone, because it is still yours
to shut down. Instances are safe to share across coroutines, so build one and keep it.

## Android

Works on API 26 and up. The SDK deliberately avoids `java.net.http`, which Android does not ship,
and its timestamps use `java.time`, which arrived in API 26.

```kotlin
// AndroidManifest.xml
<uses-permission android:name="android.permission.INTERNET" />
```

```kotlin
// build.gradle.kts
android {
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

Keep the API key off the device. An app that ships a key ships it to everyone who installs it —
call FoPost from your own backend and let the app talk to that.

## Scheduling

`status` is `draft` or `scheduled`, and a scheduled post needs a time. To send something out now,
create it and call `publish`.

```kotlin
client.posts.create(
    CreatePostParams(
        workspaceId = workspaceId,
        accounts = listOf(accountId),
        content = contentOf("Scheduled with the SDK"),
    ).scheduledAt(Instant.parse("2026-09-01T10:00:00Z")),
)
```

`publish` returns once delivery is **queued**, not once the post is live — watch the deliveries or
a webhook for that. Before publishing, `preflight` reports the per-account blockers and advisory
signals without sending anything, and `PublishParams(dryRun = true)` rehearses the whole thing:

```kotlin
val check = client.posts.preflight(post.id!!)
if (check.ready != true) {
    check.accounts.forEach { println("${it.platform}: ${it.issues}") }
}
```

## Pagination

`list` returns one page and iterates over its items. `listAll` is a `Flow` that walks every page,
fetching each one as it is collected:

```kotlin
val page = client.posts.list(PostListParams(workspaceId = workspaceId, perPage = 50))
println("${page.meta?.total} posts")

client.posts.listAll(PostListParams(workspaceId = workspaceId))
    .filter { it.status == PostStatus.FAILED }
    .collect { println(it.id) }
```

## Resources

| Namespace     | Methods                                                                                                                                                                                                                                                          |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `posts`       | `list`, `listAll`, `get`, `create`, `update`, `delete`, `duplicate`, `publish`, `retry`, `cancel`, `preflight`, `deliveries`, `publishRuns`, `analytics`, `bulkShift`, `bulkLabel`, `bulkDelete`, `validateImport`, `commitImport`, `rollbackImport` |
| `workspaces`  | `list`, `get`, `create`, `update`, `delete`, `analytics`                                                                                                                                                                                                          |
| `accounts`    | `list`, `get`, `create`, `rename`, `move`, `delete`, `healthSummary`, `health`, `togglePrimary`, `validate`, `refreshToken`, `analytics`, `createTelegramConnectCode`, `getTelegramConnectStatus`, `getTelegramBotCommands`, `setTelegramBotCommands`, `deleteTelegramBotCommands`, `listSlackChannels`, `listSlackMembers`, `getSlackIdentity`, `updateSlackIdentity`                                                                                                                                            |
| `communities` | `list`, `sync`, `search`, `add`, `remove`                                                                                                                                                                                                                         |
| `accountGroups` | `list`, `get`, `create`, `update`, `delete`, `setMembers`                                                                                                                                                                                                      |
| `labels`      | `list`, `get`, `create`, `update`, `delete`                                                                                                                                                                                                                       |
| `webhooks`    | `list`, `create`, `update`, `delete`, `test`                                                                                                                                                                                                                      |
| `analytics`   | `overview`, `timeSeries`, `topPosts`, `labels`, `postsTable`, `postingStreak`, `demographics`, `collect`                                                                                                                                                          |
| `automations` | `list`, `get`, `create`, `update`, `delete`, `toggle`, `runs`, `run`, `trigger`, `stats`                                                                                                                                                                          |
| `media`       | `list`, `upload`, `presign`, `complete`, `uploadDirect`, `delete`                                                                                                                                                                                                |
| `inbox`       | `list`, `threads`, `conversations`, `unreadCount`, `accounts`, `platforms`, `markThreadRead`, `refresh`, `update`, `editComment`, `reply`, `hide`, `unhide`, `delete`, `like`, `unlike`, `pin`, `unpin`, `react`, `startConversation`, `setTyping`, `listApprovals`, `approveReply`, `rejectReply` |
| `ads`         | `list`, `external`, `boostable`, `connections`, `sources`, `authorizeMeta`, `deleteConnection`, `boost`, `create`, `refresh`, `setStatus`, `delete`, `accountTree`, `createCampaign`, `campaign`, `updateCampaign`, `deleteCampaign`, `duplicateCampaign`, `createAdSet`, `adSet`, `updateAdSet`, `deleteAdSet`, `duplicateAdSet`, `createNetworkAd`, `networkAd`, `updateNetworkAd`, `deleteNetworkAd`, `duplicateNetworkAd`, `bulkSetStatus`, `creatives`, `createCreative`, `creative`, `deleteCreative`, `estimateReach`, `insights`, `adInsights`, `audiences`, `createAudience`, `audience`, `updateAudience`, `deleteAudience`, `addAudienceUsers`, `searchTargeting`, `leadForms`, `createLeadForm`, `leadForm`, `archiveLeadForm`, `leads`, `leadsFeed`, `leadPages`, `subscribeLeadPage`, `unsubscribeLeadPage` |
| `validate`    | `post`, `length`, `media`                                                                                                                                                                                                                                        |
| `activity`    | `list`                                                                                                                                                                                                                                                           |

For an endpoint the SDK does not wrap yet, `request` sends an authenticated call and hands back
the raw body; `requestAs` decodes the `data` payload into a type of yours:

```kotlin
val raw = client.request("GET", "/analytics/overview", query = mapOf("days" to 30))
val platforms: List<String> = client.requestAs("GET", "/platforms")
```

## Media

Upload once, then attach the returned file to a content block:

```kotlin
val file = client.media.upload("ws_1", File("chart.png")).first()

client.posts.create(
    CreatePostParams(
        workspaceId = workspaceId,
        accounts = listOf(accountId),
        content = listOf(ContentBlockInput("Numbers are in", media = listOf(file.toMediaItem()))),
    ),
)
```

For a large file, a direct upload sends the bytes straight to storage instead of through the
API. `uploadDirect` presigns, PUTs the bytes, and completes in one call; `presign` and `complete`
are the two halves for when you PUT the bytes yourself:

```kotlin
val file = client.media.uploadDirect("ws_1", "clip.mp4", "video/mp4", File("clip.mp4").readBytes())
```

## Validation

Check a draft, a text or a file against platform rules before it exists as a post. Nothing is
stored, and every method needs the `posts` scope.

```kotlin
val verdict = client.validate.post(
    ValidatePostParams(
        platforms = listOf("twitter", "linkedin"),
        content = "Numbers are in",
        media = listOf(ValidateMediaInput("https://example.com/chart.png", "image/png")),
    ),
)
verdict.platforms.filterNot { it.ready == true }.forEach { println("${it.platform}: ${it.issues}") }

val lengths = client.validate.length(ValidateLengthParams("A long caption…", listOf("twitter")))
val file = client.validate.media("https://example.com/chart.png")
```

## Webhooks

The signing secret is returned by the create call and never shown again — store it then.

```kotlin
val hook = client.webhooks.create(
    workspaceId,
    "https://example.com/hooks/fopost",
    listOf(WebhookEvents.POST_PUBLISHED, WebhookEvents.DELIVERY_FAILED),
)

println(hook.secret)
client.webhooks.test(hook.id!!)
```

## Configuration

```kotlin
val client = FoPost(
    apiKey = "fp_...",                       // or FOPOST_API_KEY
    baseUrl = "https://api.fopost.com/v1",   // or FOPOST_BASE_URL
    maxRetries = 3,                          // total attempts, so 3 means two retries
    timeout = 30.seconds,
    httpClient = myOkHttpClient,             // optional: your own transport
    userAgent = "acme/2.0",                  // optional: prefixed to the SDK's own
)
```

| Env var           | Used for                                     |
| ----------------- | -------------------------------------------- |
| `FOPOST_API_KEY`  | API key, when none is passed to the client   |
| `FOPOST_BASE_URL` | API root, when none is passed to the client  |

## Retries

Three attempts by default. A `429`, a `5xx`, and a connection failure are retried; a `4xx` other
than `429` is not, because retrying it would fail the same way. Backoff is exponential from 500 ms
and capped at 60 seconds, and a `429` waits for the interval the API asks for in `Retry-After`
(delta-seconds or an HTTP date) instead. A cancelled request is never retried.

## Error handling

Every non-2xx response throws a subclass of `FoPostException`, carrying the API's status, code,
message, and raw body.

```kotlin
try {
    client.posts.publish(postId)
} catch (e: PaymentRequiredException) {
    println("Out of credits — upgrade at ${e.upgradeUrl}")
} catch (e: RateLimitException) {
    println("Rate limited, retry in ${e.retryAfter}")
} catch (e: FoPostException) {
    println("API ${e.status} (${e.code}): ${e.message}")
}
```

| Status   | Exception                   |
| -------- | --------------------------- |
| 400, 422 | `ValidationException`       |
| 401      | `AuthenticationException`   |
| 402      | `PaymentRequiredException`  |
| 403      | `PermissionDeniedException` |
| 404      | `NotFoundException`         |
| 429      | `RateLimitException`        |
| 5xx      | `ServerException`           |
| other    | `ApiException`              |
| no reply | `TransportException`        |

`rateLimit` on any of them carries the `X-RateLimit-*` headers that came with the response, and
`field(name)` reads an extra field the error body carried.

## Scopes

An API key carries only the scopes granted when it was created, and every request is confined to
the workspaces that key can reach. `posts` also covers publishing, deliveries, and media; the rest
are `workspaces`, `accounts`, `labels`, `webhooks`, `analytics`, `automations`, `inbox`, and `ads`.
`ads.boost`, `ads.create`, `ads.setStatus` and `ads.delete` spend money and need `publish` as well
as `ads`; a boost or ad starts paused unless `paused` is `false`. So do creating, updating, deleting
and duplicating campaigns, ad sets and network ads, and `ads.bulkSetStatus`.
`inbox.editComment`, `like`, `unlike`, `pin`, `unpin`, `react`, `startConversation`, `setTyping`,
deleting our own reply, and a reply with `mediaIds` or `quickReplies` need `publish` as well as
`inbox`.

## Example

[`examples/`](examples) creates a post against a running API:

```bash
export FOPOST_API_KEY=fp_...
./gradlew :examples:run --args="Hello from Kotlin --publish"
```

## Contributing

Issues and pull requests are welcome at [fopost/fopost-kotlin](https://github.com/fopost/fopost-kotlin/issues).

```bash
./gradlew build
```

Tests run against a local mock server and never touch the network.

## License

MIT. Full documentation is at [fopost.com/docs](https://fopost.com/docs); questions or a problem,
[fopost.com/contact](https://fopost.com/contact).
