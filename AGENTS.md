# Development Rules

## Conversational Style

- Keep answers short and concise
- No emojis in commits, issues, PR comments, or code
- No fluff or cheerful filler text (e.g., "Thanks @user" not "Thanks so much @user!")
- Technical prose only, be direct
- When the user asks a question, answer it first before making edits or running implementation commands.
- When responding to user feedback or an analysis, explicitly say whether you agree or disagree before saying what you changed.

## Gradle

Always use `-q`/`--quiet` when running `./gradlew` commands to avoid noisy output.
Use fine-grained instructions rather than project-wide commands.

Prefer `./gradlew -q :module:test` over `./gradlew -q build`

### Searching dependency jars

Use the `jarSearch` Gradle task to inspect packages, types, and members in  resolved dependency jars.

Run it with `-q` to keep output clean:

```bash
./gradlew -q jarSearch --dependency <spec> [options]
```

### `--dependency` (required)
One of:
- `*` or a configuration name — search every jar on that configuration
- a Gradle coordinate `group:artifact[:version]`
- a version-catalog alias (e.g. `arrow-core`)
- a direct `.jar` path

### Options
- `--query <text>` — package, type, function, or method to look for
- `--kind <all|package|type|function|top_level_function|method>` — default `all`
  (`function`, `top_level_function`, and `method` require `--query`)
- `--configuration <name>` — configuration to search/resolve against (default `compileClasspath`)
- `--limit <n>` — max results per section (default `20`)
- `--include-non-public` — include non-public members
- `--include-synthetic` — include synthetic/compiler-generated members
- `--raw-signatures` — print raw JVM descriptors
- `--transitive <auto|true|false>` — transitive search mode (default `auto`)

### Examples
```bash
# List packages/types in a catalog dependency
./gradlew -q jarSearch --dependency arrow-core

# Find a type
./gradlew -q jarSearch --dependency arrow-core --kind type --query Either

# Find methods matching a name
./gradlew -q jarSearch --dependency "io.arrow-kt:arrow-core" --kind method --query fold
```
