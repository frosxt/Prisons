rootProject.name = "PrisonCore"

pluginManagement {
    includeBuild("build-logic")
}

// Core
include("platform-api")
include("platform-spi")
include("platform-kernel")
include("platform-runtime-bukkit")
include("distribution-plugin")

// Subsystems
include("platform-commons")
include("platform-config")
include("platform-command")
include("platform-menu")
include("platform-placeholder")
include("platform-message")
include("platform-scheduler")
include("platform-player")
include("platform-storage")
