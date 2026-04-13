plugins {
    id("prisoncore.bukkit-conventions")
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

dependencies {
    implementation(project(":platform-api"))
    implementation(project(":platform-spi"))
    implementation(project(":platform-kernel"))
    implementation(project(":platform-runtime-bukkit"))
    implementation(project(":platform-commons"))
    implementation(project(":platform-config"))
    implementation(project(":platform-command"))
    implementation(project(":platform-menu"))
    implementation(project(":platform-placeholder"))
    implementation(project(":platform-message"))
    implementation(project(":platform-scheduler"))
    implementation(project(":platform-storage"))
    implementation(project(":platform-player"))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }
    runServer {
        minecraftVersion("1.20.1")
    }
}

tasks.named("processResources", ProcessResources::class) {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
