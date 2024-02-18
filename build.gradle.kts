import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.5.10"
}

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
                implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("io.ktor:ktor-server-cors-jvm:2.3.7")
                implementation("io.ktor:ktor-server-netty:2.3.7")
                implementation ("ch.qos.logback:logback-classic:1.2.6")
                implementation("io.ktor:ktor-server-core-jvm:2.3.7")
                implementation("io.ktor:ktor-server-host-common-jvm:2.3.7")
                implementation("io.ktor:ktor-server-status-pages-jvm:2.3.7")
                implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation("org.jetbrains.compose.material3:material3-desktop:1.2.1")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
//                implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
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
            packageName = "ledshow"
            packageVersion = "1.3.20"
            modules("java.sql")
            //jvmArgs += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
        }
    }
}
