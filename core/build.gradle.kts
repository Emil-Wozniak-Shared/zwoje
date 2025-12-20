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
    // Logging API
    implementation("org.slf4j:slf4j-api:2.0.16") { isTransitive = false }
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16") { isTransitive = false }

    api(libs.groovy.templates)
    api(libs.kotlinx.html) { isTransitive = false }
    api(libs.mustache) { isTransitive = false }
    api(libs.thymeleaf) { isTransitive = false }
    api(libs.freemarker) { isTransitive = false }
    api(libs.pebble) { isTransitive = false }
    api(libs.jasper) {
        isTransitive = false
    }

    // Engines dependencies
    implementation("org.unbescape:unbescape:1.1.6.RELEASE") { isTransitive = false }
    implementation("ognl:ognl:3.3.4") { isTransitive = false }
    implementation("org.attoparser:attoparser:2.0.5.RELEASE") { isTransitive = false }
    implementation("org.javassist:javassist:3.29.2-GA") { isTransitive = false }
    implementation("org.eclipse.jdt.core.compiler:ecj:4.6.1") { isTransitive = false }

    implementation(libs.arrow.core.jvm)
    runtimeOnly(libs.arrow.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.engine)
    testImplementation(libs.kluent)

    // Engines test dependencies
    testImplementation("org.unbescape:unbescape:1.1.6.RELEASE") { isTransitive = false }
    testImplementation("ognl:ognl:3.3.4") { isTransitive = false }
    testImplementation("org.attoparser:attoparser:2.0.5.RELEASE") { isTransitive = false }
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