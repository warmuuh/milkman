# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

This is a Java 21 + JavaFX 21 Maven multi-module project. Use the Maven wrapper:

```bash
# Full build
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21-tem && ./mvnw package

# Build excluding UI tests (mirrors CI)
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21-tem && ./mvnw -B package -DexcludedGroups=ui

# Run all tests
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21-tem && ./mvnw test

# Run tests in a specific module
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21-tem && ./mvnw -pl milkman-rest test

# Run a single test class
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21-tem && ./mvnw -pl milkman-rest test -Dtest=CurlImporterTest

# Skip UI tests (tagged @Tag("ui"), require a real JavaFX environment)
./mvnw test -DexcludedGroups=ui
```

There is no dedicated linter or formatter — no Checkstyle/Spotless/PMD configured.

## Architecture Overview

Milkman is a JavaFX desktop request/response workbench (think Postman but lightweight, not Electron). The core design principle is **everything is a plugin**: request types, response aspects, editors, importers, themes, and sync mechanisms are all loaded via Java SPI (`ServiceLoader`).

### Module Layout

- **`milkman/`** — core application: domain model, persistence, UI framework, plugin SPI interfaces, DI wiring
- **`milkman-rest/`** — primary plugin: HTTP/REST request execution using Jetty 12 HTTP client; includes Postman/OpenAPI/cURL/Insomnia importers
- **`milkman-dist/`** — distribution assembly; produces platform-specific zip/tgz archives with bundled JRE (Liberica JDK with JavaFX) via `mvn-jlink-wrapper`
- **`milkman-cli/`** — CLI interface using picocli + jline3
- **`milkman-mcp/`** — newest plugin, MCP protocol support via `io.modelcontextprotocol.sdk:mcp`
- Other `milkman-*/` directories are protocol/feature plugins (grpc, graphql, jdbc, websocket, cassandra, scripting, etc.)

### Core Layers (inside `milkman/`)

**Domain** (`milkman.domain`): Plain Lombok POJOs, Jackson-serialized. Key types:
- `Workspace` → contains `Collection`s and open requests
- `RequestContainer` (abstract, `@JsonTypeInfo(use = Id.CLASS)`) — base for all request types; unknown plugin types deserialize as `UnknownRequestContainer`
- `RequestAspect` / `ResponseContainer` / `ResponseAspect` — abstract bases for request/response extensions
- `Dirtyable` — dirty-state tracking base class used to prompt save dialogs

**Persistence** (`milkman.persistence`): Nitrite DB (embedded NoSQL). `PersistenceManager` stores `Workspace`, `OptionEntry`, and `WorkbenchState` using Nitrite's `ObjectRepository`.

**Controllers** (`milkman.ctrl`):
- `ApplicationController` — top-level lifecycle: workspace loading, environment management, sync, import/export
- `WorkspaceController` — workspace UI state and request execution dispatch
- `RequestExecutor` — extends JavaFX `Service` to run plugins on a background thread

**Plugin SPI** (`milkman.ui.plugin`): All extension interfaces. `UiPluginManager` loads them via `ServiceLoader`. Key interfaces: `RequestTypePlugin`, `RequestAspectsPlugin`, `ContentTypePlugin`, `ImporterPlugin`, `WorkspaceSynchronizer`, `UiThemePlugin`. Plugins receive capabilities through "aware" mixins: `ToasterAware`, `ActiveEnvironmentAware`, `AutoCompletionAware`, `ExecutionListenerAware`, etc.

**UI** (`milkman.ui.main`): All JavaFX, built **programmatically** using a `FxmlBuilder` DSL (no FXML XML files). Helper methods like `hbox()`, `vbox()`, `button()`, `icon()` are used everywhere. Many UI classes have inner `*Fxml` classes that extend layout nodes.

**Templating** (`milkman.templater`): Resolves `{{variable}}` mustache-style tags from the active environment. Supports nested resolution and plugin-provided resolvers.

### Dependency Injection

The project uses **hardwire** — a compile-time DI annotation processor (similar to Dagger, written by the same author). Uses `@Module`, `@Singleton`, `@Inject`, `@PostConstruct`. The wiring entry point is `MainModule` which extends generated `MainModuleBase`. No Spring, no CDI.

### Event System

UI commands propagate via lightweight `Event<T>` / `WeakEvent<T>` (not reactive streams). Application-level commands use `Event<AppCommand>`; workspace-level use `Event<UiCommand>`.

### Plugin Fat-Jar Pattern

Each plugin module packages itself as a fat jar (all dependencies included except `milkman` core, which is `provided`) via `maven-assembly-plugin`. At runtime, plugins are discovered either from the classpath (bundled distribution) or dropped as fat jars into the `/plugins` folder.

### Theming

CSS-based via SASS. The build uses `sass-cli-maven-plugin` to compile `.scss` → `.css`, then `maven-css2bin-plugin` to produce `.bss` (JavaFX binary CSS). Four built-in themes: milkman (light/dark), trump (light/dark).

### Key Conventions

- **Lombok everywhere**: `@Data`, `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`, `@SneakyThrows` — Lombok plugin is required in the IDE.
- **Jackson polymorphism**: `RequestContainer` uses class-name-based type info so plugins serialize correctly and missing-plugin types are handled gracefully.
- **JitPack**: Used as a Maven repository for `hardwire`, `pem-utils`, and for resolving milkman itself in external plugin projects.
- **CI**: Runs on `windows-latest` with Liberica JDK 21 (which includes JavaFX). Commits containing `[NOBUILD]` in the message skip CI. Use `[NOBUILD]` for doc-only or changelog commits.
