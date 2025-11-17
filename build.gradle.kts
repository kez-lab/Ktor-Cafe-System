plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)

    // datetime
    implementation(libs.kotlinx.datetime)

    // database
    implementation("org.jetbrains.exposed:exposed-core:1.0.0-rc-3")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-rc-3")
    implementation("org.jetbrains.exposed:exposed-dao:1.0.0-rc-3")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0-rc-3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.4")

    // env
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

// Configure Ktor plugin to build Fat JAR
ktor {
    fatJar {
        archiveFileName.set("ktor-app-all.jar")
    }
}
