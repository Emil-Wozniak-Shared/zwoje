plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
    alias(libs.plugins.kotest)
}

group = "pl.ejdev"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2025.1.4.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        bundledPlugins("com.intellij.modules.json")
    }

    implementation(libs.gson)
    implementation("pl.ejdev.zwoje:core:1.0.0-SNAPSHOT")
    implementation(libs.pdf.renderer)
    implementation(libs.pdfbox)
    implementation(libs.swinky)

    // core requirements
    implementation(libs.kotlinx.html)
    implementation(libs.mustache)
    implementation(libs.thymeleaf)
    implementation(libs.freemarker)
    implementation(libs.groovy.templates)
    implementation(libs.pebble)

    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.kluent)
    testImplementation(kotlin("test"))
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
