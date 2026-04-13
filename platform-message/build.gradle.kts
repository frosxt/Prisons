plugins {
    id("prisoncore.bukkit-conventions")
}

dependencies {
    compileOnly(project(":platform-api"))
    compileOnly(project(":platform-config"))
    implementation(project(":platform-commons"))
    implementation(project(":platform-placeholder"))
}
