plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.4.20"
    id("org.sonarqube") version "6.0.1.5171"
}

group = "social.marylieh.secureproxy"
version = "1.0-SNAPSHOT"

val kotlinVersion: String by project

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.charleskorn.kaml:kaml:0.61.0")
    implementation("com.mysql:mysql-connector-j:9.1.0")

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

sonar {
    properties {
        property("sonar.projectKey", "SPRXY_secure-proxy_3693614f-ab15-4ec0-9877-c331cfce8257")
        property("sonar.projectName", "secure-proxy")
        property("sonar.qualitygate.wait", true)
    }
}


kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveClassifier.set("")
    configurations = listOf(project.configurations.runtimeClasspath.get())
    mergeServiceFiles()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}