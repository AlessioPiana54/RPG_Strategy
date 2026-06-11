import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.4.2"
}

group = "it.unicam.cs.mpgc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    runtimeOnly("com.sun.xml.bind:jaxb-impl:4.0.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass = "it.unicam.cs.mpgc.rpg125627.Main"
}

tasks.named<ShadowJar>("shadowJar") {
    // Classifier "all" evita il conflitto di file con il jar standard (Gradle 9 strict validation)
    archiveClassifier = "all"
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "it.unicam.cs.mpgc.rpg125627.Main"
    }
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}
