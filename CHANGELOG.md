# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

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

- `uploadDirect` streams the file instead of holding it in a `ByteArray`, and `presign` accepts a
  `size` up to the account's `maxFileBytes` (5 GB at the top).
