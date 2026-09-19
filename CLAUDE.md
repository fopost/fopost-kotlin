# CLAUDE.md

Guidance for Claude Code (claude.ai/code) when working in this repository.

## What This Is

`com.fopost:fopost-kotlin` — the official Kotlin SDK for the FoPost REST API, published to Maven
Central. It wraps `https://api.fopost.com/v1`: posts, workspaces, accounts, communities, labels,
webhooks, analytics, automations, media, inbox, and ads. Not wrapped: `/inbox/chat/*` (browser-
encrypted X Chat) and `/inbox/{id}/attachments/{index}` (a binary stream). The public API is `suspend` functions on coroutines;
transport is OkHttp and JSON is kotlinx-serialization.

Sibling SDKs wrap the same API and are the reference for behaviour: `fopost-go` has the widest
resource coverage, `fopost-java` is the JVM sibling whose model and parameter names this one
matches, and `fopost-api-collections/openapi.json` is the authoritative endpoint list.

## Brand Rules

- The product is **FoPost** (`fopost.com`). Never write "OwlStack" — retired Aug 2026.
- Never write an email address. Support is https://fopost.com/contact and GitHub issues.
- Never name AI providers/models, infrastructure vendors, or any person. The author is
  Porter Bridge, LLC.

## Architecture

```
src/main/kotlin/com/fopost/
  FoPost.kt              the client: config, resource namespaces, Closeable, escape hatch
  FoPostException.kt     the sealed error hierarchy and RateLimit
  internal/
    ApiClient.kt         headers, query encoding, envelope unwrap, retries, error mapping
    Await.kt             OkHttp Call -> suspend, cancelling the call on cancellation
    Json.kt              the one configured Json instance
    InstantSerializer.kt ISO 8601 <-> java.time.Instant
    Version.kt           SDK_VERSION, compiled into the User-Agent
  model/                 @Serializable response types
  param/                 request bodies and query filters
  resource/              one class per namespace, each holding an ApiClient
examples/                a runnable :examples:run main
```

A request flows `resource -> ApiClient.call(...) -> send(...) -> OkHttp`. `send` owns the retry
loop and returns a `JsonElement`; `call` peels the `{"data": ...}` envelope and decodes it with an
explicit `KSerializer`. `page` reads `data` and `meta` together for list endpoints.

Resources never build URLs or headers themselves, and never re-implement retry or error mapping.
A new endpoint is one method on a resource plus, if it needs one, a `@Serializable` model.

### Conventions that are load-bearing

- **The API is inconsistent about casing.** Posts and labels answer snake_case; accounts,
  webhooks and automations camelCase; workspaces a mix. Every multi-word model property therefore
  carries both spellings: `@SerialName("workspace_id") @JsonNames("workspaceId")`. Request
  parameters use the exact name openapi.json documents for that endpoint, and nothing else.
- **`explicitNulls = false`** in the `Json` config, so a parameter the caller never set is not
  sent and the server keeps its own default. Do not add `null` handling in the resources.
- **`ignoreUnknownKeys = true`**, so a field the server adds never breaks an older client. Every
  model property is nullable with a default for the same reason.
- **No `java.net.http`.** Android does not ship it. OkHttp is the transport, and the bytecode
  target stays at JVM 11 so Android API 26+ can consume the jar.
- The `sleeper` on `ApiClient` is the test seam for the retry loop. Tests replace it rather than
  waiting.

## API Contract

- Base URL `https://api.fopost.com/v1`, overridable per client or via `FOPOST_BASE_URL`.
- Auth is the header `X-API-Key`, **not** a bearer token. Falls back to `FOPOST_API_KEY`.
- Headers: `Accept: application/json`, `User-Agent: fopost-kotlin/<version>`.
- Retries: 3 total attempts, on 429, on >= 500, and on a connection failure. Backoff
  `500ms * 2^(attempt-1)` capped at 60s; a 429 honours `Retry-After`, capped the same way. A
  cancelled request is never retried.
- Success envelope: most endpoints answer `{"data": ...}`; a few (bulk actions, bulk import)
  answer bare, so the unwrap is conditional and those calls pass `unwrap = false`.
- Errors: `{"error": "<code>", "message": "<text>"}`, 402 may add `upgrade_url`. Every response
  carries `X-RateLimit-Limit`, `-Remaining`, `-Reset`.

## Commands

```bash
./gradlew build            # compile, test, assemble — what CI runs
./gradlew test             # tests only
./gradlew :examples:run --args="Hello --publish"
./gradlew publishToMavenLocal
```

Tests are JUnit 5 with OkHttp `MockWebServer` and run fully offline. Never add a test that reaches
the real API.

## Conventions

- Kotlin official code style, 4-space indent, trailing commas.
- Public API carries KDoc; internals get a short comment only where a "why" is non-obvious.
- Everything a caller touches is a `suspend` function; nothing blocks.
- Models are `data class` with nullable defaults; parameters are `data class` with `null`
  defaults so unset fields are omitted.
- Version `0.2.0` lives in `gradle.properties` **and** `internal/Version.kt`. Bump both — the
  release workflow fails if they disagree.

## Releasing

Tag `v<version>` matching `gradle.properties`; `.github/workflows/release.yml` publishes to Maven
Central through the Sonatype Central Portal via `com.vanniktech.maven.publish`.

Requires four secrets on the `central` environment:

| Secret                  | What                                             |
| ----------------------- | ------------------------------------------------ |
| `MAVEN_CENTRAL_USERNAME`| Central Portal user token name                   |
| `MAVEN_CENTRAL_PASSWORD`| Central Portal user token password                |
| `SIGNING_KEY`           | ASCII-armoured GPG private key                    |
| `SIGNING_PASSWORD`      | passphrase for that key                           |

They reach Gradle as `ORG_GRADLE_PROJECT_mavenCentralUsername`, `...mavenCentralPassword`,
`...signingInMemoryKey` and `...signingInMemoryKeyPassword`. Maven Central rejects a release with
an incomplete POM, so the `pom { }` block in `build.gradle.kts` (name, description, url, MIT
license, developer, scm) is not optional.

## Git

Conventional Commits, atomic — one logical change per commit. Branch `feature/<description>`,
merge to `main` via PR. Never run `gh pr create`: push the branch and hand over the compare link.
