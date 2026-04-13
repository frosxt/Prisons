plugins {
    id("prisoncore.java-conventions")
}

dependencies {
    compileOnly(project(":platform-api"))
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    implementation(project(":platform-commons"))
}
