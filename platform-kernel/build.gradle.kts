plugins {
    id("prisoncore.java-conventions")
}

dependencies {
    implementation(project(":platform-api"))
    implementation(project(":platform-spi"))
    implementation(project(":platform-commons"))
    implementation(project(":platform-placeholder"))
}
