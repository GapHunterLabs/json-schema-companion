<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# JSON Schema Companion Changelog

## [Unreleased]

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real unresolved `$ref` findings, a
  one-time notification asks whether to rate the plugin on Marketplace,
  with a permanent "Don't ask again" option. Standard mechanism used
  catalog-wide since 2026-08-24 (`CONSTITUTION.md` §7.2), rolled out to
  this plugin now.

## [0.1.0]

### Added

- Go-to-definition for `$ref` values in JSON Schema files, resolved
  entirely locally (same-file JSON Pointers and cross-file references).
- Content-based JSON Schema file recognition (`"$schema"` property, not
  file extension).
- Warning annotation for `$ref` values that fail to resolve.

[Unreleased]: https://github.com/GapHunterLabs/json-schema-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/json-schema-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/json-schema-companion/commits/0.1.0
