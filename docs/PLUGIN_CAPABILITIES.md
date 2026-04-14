# WireGoblin IntelliJ Plugin Capabilities

This document describes what the WireGoblin IntelliJ plugin currently does.

The plugin adds dedicated support for WireGoblin configuration files in IntelliJ-based IDEs such as GoLand. It is focused on editing YAML configuration safely and quickly, with schema awareness, targeted completion, validation, and navigation support.

## Supported files

The plugin activates for these file name patterns:

- `wiregoblin.yaml`
- `wiregoblin.yml`
- `*.wiregoblin.yaml`
- `*.wiregoblin.yml`

## Core editing support

### Semantic syntax highlighting

The plugin adds WireGoblin-specific semantic highlighting on top of the base YAML editor support.

It can colorize:

- structural keys such as `workflows`, `blocks`, `retry_on`, `rules`, `assign`, `condition`, and `ai`
- metadata keys such as `id`, `name`, `type`, and `version`
- known WireGoblin field keys such as `method`, `url`, `provider`, `operator`, and other schema-backed block fields
- block `type` values, with different colors for different block families
- retry rule `type` values
- constrained enum-like values such as HTTP methods, condition operators, AI provider names, and log levels
- inline WireGoblin references including `@...`, `$...`, and `!...`, with separate colors per reference kind

These colors are exposed through IntelliJ color settings so users can adjust them in the editor color scheme.

### WireGoblin-aware completion

The plugin provides YAML completion that understands the WireGoblin document structure instead of offering only generic YAML suggestions.

It can suggest:

- top-level keys such as `id`, `name`, `version`, `ai`, `constants`, `secrets`, `variables`, `secret_variables`, and `workflows`
- workflow-level keys such as `id`, `name`, `disable_run`, `timeout_seconds`, `outputs`, `catch_error_blocks`, and `blocks`
- common block keys such as `id`, `name`, `type`, `condition`, `continue_on_error`, and `assign`
- block-specific keys based on the selected block `type`
- allowed values for constrained fields such as block `type`, retry rule `type`, AI `provider`, HTTP `method`, and condition `operator`

### Block type templates

When a block `type` is chosen from completion, the plugin can insert the recommended fields for that block. This reduces boilerplate and helps create valid blocks faster.

The inserted template respects keys that already exist in the block and avoids re-inserting fields that are already present.

### Automatic reference completion

Inside WireGoblin value contexts, the plugin can complete:

- `@...` references for constants and secrets
- `$...` references for variables and secret variables
- `!...` built-in runtime expressions
- helper templates such as `${NAME}` and `${NAME:=default}`

Reference completion works in plain and quoted scalar values, so suggestions can appear for inputs such as `url: @gr...` and `url: "@gr..."`.

Reference suggestions are scope-aware:

- top-level constants, secrets, variables, and secret variables are suggested
- workflow-local constants, secrets, variables, and secret variables are also suggested when editing inside a workflow

### Auto-popup behavior for references

The plugin includes a typed handler that can automatically trigger completion when the user starts entering WireGoblin reference prefixes. This makes `@`, `$`, and `!` based references faster to insert in value positions.

## Validation and inspections

### Block type validation

The plugin validates block `type` values inside block lists and reports unknown block types directly in the editor.

### Retry rule type validation

Inside retry rules, the plugin validates the retry rule `type` and reports unsupported values.

### Constrained field value validation

For fields with a known allowed value set, the plugin validates the actual value and highlights invalid entries. This applies to cases such as:

- AI provider values
- condition operators
- HTTP methods
- other schema-backed enumerated fields

### Reference validation

For WireGoblin value fields that support runtime insertion, the plugin validates inline WireGoblin references and highlights unknown entries directly in the editor. This applies to:

- `@...` constants and secrets
- `$...` variables and secret variables
- `!...` built-in runtime expressions

This validation is scope-aware in the same way as reference completion. Environment placeholders such as `${NAME}` are not treated as WireGoblin references and are not reported by this check.

### Workflow structure inspection

The plugin detects a common structural mistake where `block` is used instead of `blocks` in workflow scope and reports it as an error.

## Intentions and quick fixes

### Environment placeholder insertion

The plugin exposes an intention for inserting environment variable placeholders into valid WireGoblin value positions.

### Dashed key repair

The plugin includes an intention that fixes malformed dashed YAML key usage in WireGoblin files.

## Workflow execution

### Gutter run action

For runnable workflows, the plugin shows a green run arrow in the gutter next to the workflow `id` line.

Clicking the icon saves the current documents and launches the configured workflow through the WireGoblin CLI in IntelliJ's run tool window.

The plugin currently resolves the CLI from `PATH` and supports these executable names:

- `wiregoblin-cli`
- `wiregoblin`

The executed command shape is:

- `wiregoblin-cli run -p <current file> <workflow_id>`

If a workflow has `disable_run: true`, the run gutter action is not shown because the WireGoblin runtime does not allow direct execution of that workflow.

## JSON Schema support

The plugin bundles a generated JSON schema for WireGoblin and registers it through IntelliJ's JSON Schema provider integration.

This gives users:

- schema-backed structure awareness
- better completion support in supported IDE components
- additional validation help from IntelliJ's schema infrastructure

The bundled schema is stored in:

- `src/main/resources/schema/wiregoblin.schema.json`

## Supported WireGoblin block families

The plugin schema currently includes dedicated block specifications for these block types:

- `http`
- `grpc`
- `postgres`
- `redis`
- `openai`
- `smtp`
- `imap`
- `slack`
- `telegram`
- `container`
- `delay`
- `log`
- `set_vars`
- `assert`
- `goto`
- `transform`
- `retry`
- `foreach`
- `parallel`
- `workflow`

These specifications drive completion, templates, and value validation.

## Notifications

The plugin registers its own IntelliJ notification group for WireGoblin-related messages. This is used to report outcomes and errors from plugin operations such as workflow execution.

## Menu and IDE integration

The plugin integrates with IntelliJ through:

- completion contributor registration for YAML
- annotator registration for YAML
- line marker provider registration for YAML
- typed handler registration
- intention action registration
- JSON schema provider registration

## What the plugin is not trying to do

The plugin is still focused on IDE authoring support and delegates execution to an installed WireGoblin CLI. It does not embed or replace the WireGoblin runtime.

## Source of truth

This description reflects the current implementation in the repository, especially:

- `src/main/resources/META-INF/plugin.xml`
- `src/main/kotlin/io/wiregoblin/intellij/`
- `src/main/kotlin/io/wiregoblin/intellij/blocks/`
- `src/main/resources/schema/wiregoblin.schema.json`
