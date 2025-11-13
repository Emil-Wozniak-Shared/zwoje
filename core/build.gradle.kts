plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.2.0"
    alias(libs.plugins.kotest)
}

group = "pl.ejdev.zwoje"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.pdfbox)
    implementation(libs.openhtmltopdf.core)
    implementation(libs.openhtmltopdf.pdfbox)

    implementation(libs.kotlinx.html) { isTransitive = false }
    implementation(libs.mustache) { isTransitive = false }
    implementation(libs.thymeleaf) { isTransitive = false }
    implementation(libs.freemarker) { isTransitive = false }
    implementation(libs.groovy.templates)
    implementation(libs.pebble) { isTransitive = false }

    implementation(libs.arrow.core.jvm)
    runtimeOnly(libs.arrow.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.engine)
    testImplementation(libs.kluent)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}