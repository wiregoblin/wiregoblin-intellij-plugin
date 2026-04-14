# WireGoblin IntelliJ Plugin

An IntelliJ Platform plugin for WireGoblin configuration files in GoLand.

Main WireGoblin project: [wiregoblin/wiregoblin](https://github.com/wiregoblin/wiregoblin)

The plugin currently targets these file names:
- `wiregoblin.yaml`
- `wiregoblin.yml`
- `*.wiregoblin.yaml`
- `*.wiregoblin.yml`

## Current features

- YAML-aware completion for top-level, workflow, and block keys
- Block type completion and required-field templates
- Block-type validation and retry-rule-type validation
- Reference helpers for `@`, `$`, `!`, and `${VAR:=default}`
- JSON schema bundled with the plugin

## Project structure

```text
src/
  main/
    kotlin/io/wiregoblin/intellij/
    resources/
      META-INF/plugin.xml
      schema/wiregoblin.schema.json
  test/
    kotlin/io/wiregoblin/intellij/
```

## Build and test

The project is built through Docker so you do not need a local Gradle installation.

```bash
make docker-image
make build
make dist
make release
make test
make verify
```

What this does:
- `Dockerfile` pins the environment to `Gradle 9.0.0 + JDK 21`
- `Makefile` runs Gradle inside Docker
- Gradle cache is stored in `.gradle-user-home/` inside the repository
- `make dist` builds an installable plugin ZIP in `build/distributions/`
- `make release` is an explicit alias for the distributable build

## Local sandbox IDE

If you want to run `runIde` locally, generate the Gradle wrapper first:

```bash
make wrapper
make run-ide-local
```

This still requires a local Java runtime because `./gradlew runIde` is a local JVM process.

## Installing the plugin manually

Build the distributable ZIP:

```bash
make release
```

Then install the ZIP from:

```text
build/distributions/
```

In GoLand:
1. Open `Settings`
2. Open `Plugins`
3. Click the gear icon
4. Choose `Install Plugin from Disk...`
5. Select the ZIP from `build/distributions`

## Notes

- The plugin follows the current WireGoblin docs and skill references, including `catch_error_blocks` and `openai_compatible`.
- The automated test suite currently covers plugin contract drift, file matching, schema drift, and reference-token parsing.
- Full IntelliJ fixture-based completion integration tests were explored but are not yet stable enough to keep in the default test suite.
