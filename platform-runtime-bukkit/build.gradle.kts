plugins {
    id("prisoncore.bukkit-conventions")
}

dependencies {
    implementation(project(":platform-api"))
    implementation(project(":platform-spi"))
    implementation(project(":platform-kernel"))
    implementation(project(":platform-command"))
    implementation(project(":platform-commons"))
    implementation(project(":platform-config"))
    implementation(project(":platform-menu"))
    implementation(project(":platform-message"))
    implementation(project(":platform-scheduler"))
    implementation(project(":platform-player"))
    implementation(project(":platform-storage"))
    implementation(project(":platform-placeholder"))
}
