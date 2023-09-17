@file:Suppress("INACCESSIBLE_TYPE")

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.4"
    id("org.beryx.runtime") version "1.13.0"
}

group = "com.koeltv"
version = "0.0.1"

application {
    mainClass.set("com.koeltv.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

val jreVersion: String by project
val downloadPage: String by project
val baseModules: String by project
val targets: String by project

runtime {
    options.set(listOf("--strip-debug", "--compress", "1", "--no-header-files", "--no-man-pages"))
    modules.set(baseModules.split(','))
    targets.split(',').run {
        filter { target -> project.hasProperty(target) || none { project.hasProperty(it) } }
            .forEach {
                val format = if (it == "windows") "zip" else "tar.gz"
                val encodedJreVersion = jreVersion.replace("_", "%2B")
                val link = "$downloadPage/jdk-$encodedJreVersion/OpenJDK17U-jdk_x64_${it}_hotspot_$jreVersion.$format"
                targetPlatform(it) { setJdkHome(jdkDownload(link)) }
            }
    }
}
