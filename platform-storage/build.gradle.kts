plugins {
    id("prisoncore.java-conventions")
}

dependencies {
    compileOnly(project(":platform-api"))
    compileOnly(project(":platform-spi"))
    implementation(project(":platform-commons"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
}
