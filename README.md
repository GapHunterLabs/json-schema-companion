# JSON Schema Companion

Go-to-definition (Ctrl+Click) for `$ref` values inside JSON Schema files,
resolved entirely against local files.

## Why it exists

**JSON Schema Visualizer/Editor** (JetBrains Marketplace id 23554), 3,340
downloads, paid, vendor Syncro Soft -- confirmed independently by two
separate investigations. Real, verbatim reviewer complaints:

- *"the plugin seems not to be able to resolve the references via an
  internal database but tries to download them from the URI used as ID.
  This makes references unusable in local development."* (2024-09-05,
  with a link to a JetBrains YouTrack issue confirming the underlying bug
  on the platform side too)
- *"Activates if you figure out the file extension it triggers on... the
  plugin randomly recognizes JSON schemas"* -- broken file detection
- *"when the schema holds a reference to another type or element, this
  element is just referenced... not appended to the graph. This makes
  this plugin more of a JSON viewer, not a full JSON schema
  visualizer."*

## Why built this way

- **Not the full visual diagram editor** -- out of scope for a v1 (the
  competitor doesn't reliably deliver it either, per the third complaint
  above). Instead: real local `$ref` resolution and go-to-definition
  navigation, which attacks the two central complaints directly.
- **Never touches the network.** `JsonRefReference.resolveLocalFile` only
  ever walks `VfsUtilCore.findRelativeFile` relative to the referencing
  file's own directory. An `http(s)://` `$ref` simply shows as
  unresolved -- there is no HTTP client anywhere in this plugin's code,
  so there's nothing that could "try to download" a reference.
- **Detects JSON Schema files by real content** -- a top-level
  `"$schema"` property referencing an actual json-schema.org
  meta-schema URI -- never by file extension/name guessing.
- **Built on the bundled JSON plugin's own PSI** (`JsonFile`/
  `JsonObject`/`JsonProperty`/`JsonStringLiteral`), not a custom JSON
  parser -- there's no reason to reimplement JSON parsing when the
  platform already ships one.
- **RFC 6901 JSON Pointer resolution**, hand-rolled (`JsonPointer.kt`) --
  the pointer syntax is small and stable, same "hand-roll over new
  dependency" call already made elsewhere in this workspace.

## Usage

Open a JSON file with a `"$schema"` property pointing at a real
json-schema.org URI. Ctrl+Click (or Ctrl+B / Go to Declaration) on any
`"$ref"` value -- same-file pointers (`"#/definitions/Foo"`) and
cross-file references (`"other.json#/definitions/Foo"`) both navigate to
the real definition. A `$ref` that can't be resolved is flagged with a
warning.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
