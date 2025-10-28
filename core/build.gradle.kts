plugins {
    `maven-publish`
    kotlin("jvm") version "2.2.20"
    id("io.kotest") version "6.0.4"
}

group = "pl.ejdev.zwoje"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.21.2")
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
    implementation("com.openhtmltopdf:openhtmltopdf-core:1.0.10")
    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")

    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
    implementation("org.freemarker:freemarker:2.3.33")
    implementation("org.codehaus.groovy:groovy-templates:3.0.22")
    implementation("io.pebbletemplates:pebble:3.2.4")

    implementation("io.arrow-kt:arrow-core-jvm:2.1.2")
    runtimeOnly("io.arrow-kt:arrow-core:2.1.2")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-framework-engine-jvm:6.0.4")
    testImplementation("org.amshove.kluent:kluent:1.73")
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
