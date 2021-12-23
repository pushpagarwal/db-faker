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
    testImplementation(kotlin("test"))
    testImplementation("com.azure:azure-cosmos:4.23.0")
    testImplementation("junit:junit:4.13.2")
}

