plugins {
    id("prisoncore.bukkit-conventions")
}

dependencies {
    compileOnly(project(":platform-api"))
    implementation(project(":platform-commons"))
    implementation(project(":platform-placeholder"))
}
