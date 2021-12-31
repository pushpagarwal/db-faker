plugins {
    kotlin("jvm")
}

group = "io.github.pushpagarwal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("io.projectreactor:reactor-core:3.4.12")
}