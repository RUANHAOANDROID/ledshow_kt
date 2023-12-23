import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.ledshow"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
                implementation ("joda-time:joda-time:2.10.14")
                implementation("org.jetbrains.exposed:exposed-core:0.34.1")
                implementation("org.jetbrains.exposed:exposed-dao:0.34.1")
                implementation("org.jetbrains.exposed:exposed-jdbc:0.34.1")
                implementation("org.xerial:sqlite-jdbc:3.36.0.2")
                implementation("io.ktor:ktor-server-netty:2.3.7")
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }

}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            fromFiles(project.fileTree("libs/") { include("**/*.jar") })
            packageName = "demo"
            packageVersion = "1.0.0"
        }
    }
}
