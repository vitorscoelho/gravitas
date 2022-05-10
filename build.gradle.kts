import org.jetbrains.compose.compose

plugins {
    val kotlinVersion: String by System.getProperties()
    val jetpackComposeVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.compose") version jetpackComposeVersion
}

group = "vitorscoelho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val kotlinVersion: String by System.getProperties()
val jvmTargetVersion: String by System.getProperties()

dependencies {
    implementation(kotlin(module = "stdlib", version = kotlinVersion))

    implementation(dependencyNotation = "org.ojalgo:ojalgo:49.2.1")
    implementation(group = "org.ejml", name = "ejml-all", version = "0.41")
    implementation(dependencyNotation = "org.apache.commons:commons-math3:3.6.1")
    implementation (dependencyNotation= "org.locationtech.jts:jts-core:1.18.2")

    implementation(compose.desktop.currentOs)

    testImplementation(kotlin(module = "test", version = kotlinVersion))
}

compose.desktop {
    application {
        mainClass = "vitorscoelho.gravitas.gui.MainKt"
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = jvmTargetVersion
    }
}