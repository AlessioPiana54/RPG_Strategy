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
    mainClass = "it.unicam.cs.mpgc.rpg.Main"
}

tasks.named<ShadowJar>("shadowJar") {
    // Classifier "all" evita il conflitto di file con il jar standard (Gradle 9 strict validation)
    archiveClassifier = "all"
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "it.unicam.cs.mpgc.rpg.Main"
    }
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}
