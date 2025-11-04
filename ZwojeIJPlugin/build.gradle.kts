plugins {

    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "pl.ejdev"
version = "1.0-SNAPSHOT"

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
//         bundledPlugin("com.intellij.java")
        bundledPlugins("com.intellij.modules.json")
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("pl.ejdev.zwoje:core:1.0-SNAPSHOT")
        implementation("org.swinglabs:pdf-renderer:1.0.5")
        implementation("org.apache.pdfbox:pdfbox:2.0.30")
        implementation("ca.weblite:swinky-main:0.0.24")
    }
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

tasks.named("runIde") {
    dependsOn(":core:publishToMavenLocal")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

