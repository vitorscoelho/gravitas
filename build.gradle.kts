plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
}

group = "vitorscoelho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotlinVersion: String by System.getProperties()
val jvmTargetVersion: String by System.getProperties()

dependencies {
    implementation(kotlin(module = "stdlib", version = kotlinVersion))

    testImplementation(kotlin(module = "test", version = kotlinVersion))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = jvmTargetVersion
    }
}