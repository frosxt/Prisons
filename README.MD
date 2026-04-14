# PrisonCore

PrisonCore is a modular platform for Minecraft prison servers, targeting Spigot and Paper 1.20+. The core is purposefully small: a kernel, a service container, a module system, and a handful of subsystems for the things every feature module ends up needing (commands, menus, messages, placeholders, scheduling, storage). Features live in separate modules that the kernel loads at boot.

If you're writing a feature for a prison server and you want it to compose with other features instead of fighting them, PrisonCore is the platform you build on.

## What's in here

The repo is a Gradle multi-module project.

- `platform-api` — stable consumer contracts (interfaces, value objects, enums). What your module compiles against.
- `platform-spi` — stable implementer contracts for backends and extension points.
- `platform-kernel` — lifecycle, service container, module manager, capabilities, storage registry.
- `platform-runtime-bukkit` — Bukkit-facing adapters and the `BukkitKernelBootstrap`.
- `platform-commons` — reusable primitives. Validation, cache, registry, cooldowns, item/color/PDC/skull adapters.
- `platform-config` — typed YAML config service.
- `platform-command`, `platform-menu`, `platform-message`, `platform-placeholder`, `platform-scheduler` — subsystems, each split into `api` and `bukkit`/`core` layers.
- `platform-player` — player profile, session, view, and profile repositories per backend.
- `platform-storage` — repository contracts, backend factories, JSON/SQLite/SQL/Mongo backends.
- `distribution-plugin` — the final shaded plugin jar and the default `core.yml` / `messages.yml` resources.

The jar you drop into `plugins/` is the one produced by `distribution-plugin`. Feature modules are separate jars that go in `plugins/PrisonCore/modules/`.

## Requirements

- Java 17
- Spigot or Paper 1.20.1 or later

## Using PrisonCore in your own module

PrisonCore is published through [JitPack](https://jitpack.io). Add the JitPack repository, then declare a `compileOnly` (Gradle) or `provided` (Maven) dependency on each platform module you need. Replace `<version>` with a Git tag, branch name, or commit hash.

Always use `compileOnly`/`provided`. The platform jars are loaded at runtime by the kernel, so shading them into your module's jar would duplicate every class and break the isolated module classloader.

Only pull in the subsystem jars you actually use. If your module never opens a menu, you don't need `platform-menu`.

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    compileOnly("com.github.frosxt.Prisons:platform-api:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-spi:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-commons:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-command:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-menu:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-message:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-placeholder:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-scheduler:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-storage:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-kernel:<version>")
    compileOnly("com.github.frosxt.Prisons:platform-config:<version>")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/' }
    maven { url 'https://repo.papermc.io/repository/maven-public/' }
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT'

    compileOnly 'com.github.frosxt.Prisons:platform-api:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-spi:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-commons:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-command:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-menu:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-message:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-placeholder:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-scheduler:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-storage:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-kernel:<version>'
    compileOnly 'com.github.frosxt.Prisons:platform-config:<version>'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-api</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-spi</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-commons</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-command</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-menu</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-message</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-placeholder</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-scheduler</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-storage</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-kernel</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.frosxt.Prisons</groupId>
        <artifactId>platform-config</artifactId>
        <version>VERSION</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Prefer a tagged release (for example `1.0.0`) over `main-SNAPSHOT`.

## Building from source

Clone the repo and run:

```
./gradlew build
```

The shaded plugin jar ends up in `distribution-plugin/build/libs/`. Drop that into your server's `plugins/` directory to run it.

## Documentation

The full API reference lives in the [GitHub wiki](https://github.com/frosxt/Prisons/wiki). Start there if you're writing a module. The wiki covers:

- Getting started with a new module, including project setup and the bootstrap class
- Module lifecycle, load phases, and what you can rely on in each hook
- Every subsystem the platform exposes (commands, menus, messages, placeholders, scheduler, storage, events, config, player profiles)
- Bukkit utility helpers for items, colors, and persistent data

If you only read one page first, make it [Getting Started](https://github.com/frosxt/Prisons/wiki/Getting-Started).

## Opening issues

If you hit a bug, have a feature request, or spot something wrong in the wiki, open an issue on the [GitHub tracker](https://github.com/frosxt/Prisons/issues). A few things that make issues easier to act on:

- Pick a title that describes the symptom, not the guessed cause. "Menu stays open after player quits" beats "MenuService bug."
- Include your platform version, PrisonCore version, and the server software you're running (Spigot, Paper, etc.).
- If you can reproduce the problem, list the steps. Three bullet points are better than a paragraph.
- Paste the relevant log lines inside a fenced code block. Attach the full log as a gist or file if it's longer than a screen.
- For crashes, include the stack trace. The full one, not just the top line.

If the issue is with a specific feature module rather than the core itself, open it on that module's repository instead. This repo is only for the platform.

## License

See [LICENSE](https://github.com/frosxt/Prisons/blob/main/LICENSE).
