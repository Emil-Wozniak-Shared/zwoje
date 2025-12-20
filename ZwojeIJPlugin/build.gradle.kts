plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
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

    implementation("pl.ejdev.zwoje:core:1.0.0-SNAPSHOT")
    implementation(libs.gson)
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
    implementation(libs.jasper)

    implementation(libs.coroutines.core)
    implementation("commons-digester:commons-digester:2.1")
    implementation("org.apache.commons:commons-collections4:4.5.0")

    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.kluent)
    testImplementation("junit:junit:4.13.2")
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
    test {
        useJUnit() // JUnit 4 runner
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
